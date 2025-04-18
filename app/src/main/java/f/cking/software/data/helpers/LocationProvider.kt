package f.cking.software.data.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.core.location.LocationListenerCompat
import f.cking.software.data.repo.SettingsRepository
import f.cking.software.domain.interactor.SaveReportInteractor
import f.cking.software.domain.model.JournalEntry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.lang.Runnable
import java.util.function.Consumer

class LocationProvider(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val saveReportInteractor: SaveReportInteractor,
    private val powerModeHelper: PowerModeHelper,
) {

    private val locationState = MutableStateFlow<LocationHandle?>(null)

    private val locationManager: LocationManager? = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val consumer = Consumer<Location?> { newLocation ->

        if (isActive) {
            scheduleNextRequest()
        }

        val provider = provider()

        if (newLocation == null) {
            Timber.d("Empty location emitted  (provider: $provider)")
            return@Consumer
        }

        if (!newLocation.isRelevant(locationState.value?.location)) {
            Timber.d("Irrelevant location has emitted (provider: $provider)")
            return@Consumer
        }

        if (!powerModeHelper.powerMode().useLocation) {
            Timber.d("Location is turned of for such power mode (${powerModeHelper.powerMode().name})")
            return@Consumer
        }

        Timber.d("New location: lat=${newLocation.latitude}, lng=${newLocation.longitude} (provider: $provider)")

        locationState.tryEmit(LocationHandle(newLocation, System.currentTimeMillis()))
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val locationListener = LocationListenerCompat {
        consumer.accept(it)
    }

    private var isActive: Boolean = false
    private var cancellationSignal: CancellationSignal = CancellationSignal()

    private val handler = Handler(Looper.getMainLooper())
    private val nextLocationRequest = Runnable {
        try {
            fetchLocation(withRestartSchedule = true)
        } catch (error: Throwable) {
            reportError(error)
            scheduleNextRequest()
        }
    }

    private val restartServiceRunnable = Runnable {
        stopLocationListening()
        startLocationFetching()
    }

    fun isLocationAvailable(): Boolean {
        return (locationManager?.isProviderEnabled(provider()) == true)
                && locationManager.isLocationEnabled
    }

    fun isActive(): Boolean {
        return isActive
    }

    fun observeLocation(): Flow<LocationHandle?> {
        return locationState
    }

    suspend fun getFreshLocation(): Location? {
        return observeLocation()
            .firstOrNull()
            ?.takeIf { it.isFresh() }
            ?.location
    }

    @SuppressLint("MissingPermission")
    fun startLocationFetching() {
        fetchLocation(withRestartSchedule = true)
        isActive = true
    }

    fun stopLocationListening() {
        locationManager?.removeUpdates(locationListener)
        cancellationSignal.cancel()
        handler.removeCallbacks(nextLocationRequest)
        handler.removeCallbacks(restartServiceRunnable)
        isActive = false
        scope.cancel()
    }

    fun fetchOnce() {
        if (isActive) {
            stopLocationListening()
            startLocationFetching()
        } else {
            fetchLocation(withRestartSchedule = false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation(withRestartSchedule: Boolean) {
        if (!cancellationSignal.isCanceled) {
            cancellationSignal.cancel()
        }
        cancellationSignal = CancellationSignal()

        if (!powerModeHelper.powerMode().useLocation) {
            // don't call the location manager update, just schedule next request
            scheduleNextRequest()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationManager?.getCurrentLocation(
                provider(),
                LocationRequest.Builder(powerModeHelper.powerMode().locationUpdateInterval)
                    .setDurationMillis(LOCATION_REQUEST_MAX_DURATION_MILLS)
                    .setMaxUpdateDelayMillis(LOCATION_REQUEST_MAX_DURATION_MILLS)
                    .setQuality(LocationRequest.QUALITY_HIGH_ACCURACY)
                    .build(),
                cancellationSignal,
                context.mainExecutor,
                consumer
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager?.getCurrentLocation(
                provider(),
                cancellationSignal,
                context.mainExecutor,
                consumer
            )
        } else {
            locationManager?.requestSingleUpdate(
                provider(),
                locationListener,
                context.mainLooper,
            )
        }

        if (withRestartSchedule) {
            scheduleServiceRestart()
        }
    }

    private fun provider(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !settingsRepository.getUseGpsLocationOnly()) {
            LocationManager.FUSED_PROVIDER
        } else {
            LocationManager.GPS_PROVIDER
        }
    }

    private fun scheduleNextRequest() {
        handler.postDelayed(nextLocationRequest, powerModeHelper.powerMode().locationUpdateInterval)
    }

    /**
     * Schedule location fetching restart
     * In case if LocationManager doesn't respond for a long time
     * It's better to reschedule location request manually
     */
    private fun scheduleServiceRestart() {
        handler.removeCallbacks(restartServiceRunnable)
        handler.postDelayed(restartServiceRunnable, RESTART_SERVICE_TIMER)
    }

    private fun reportError(error: Throwable) {
        Timber.e(error)
        scope.launch {
            val report = JournalEntry.Report.Error(
                error.message ?: error::class.java.name,
                error.stackTraceToString()
            )
            saveReportInteractor.execute(report)
        }
    }

    private fun LocationHandle.isFresh(): Boolean {
        return System.currentTimeMillis() - this.emitTime < ALLOWED_LOCATION_LIVETIME_MS
    }

    private fun Location.isRelevant(oldLocation: Location?): Boolean {
        return oldLocation == null
                || ((latitude != oldLocation.longitude || longitude != oldLocation.longitude)
                && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && elapsedRealtimeAgeMillis <= ALLOWED_LOCATION_LIVETIME_MS)
                && accuracy <= MAX_ALLOWED_ACCURACY_METERS)
    }

    data class LocationHandle(
        val location: Location,
        val emitTime: Long,
    )

    companion object {
        private const val LOCATION_REQUEST_MAX_DURATION_MILLS = 30_000L
        private const val MAX_ALLOWED_ACCURACY_METERS = 100F
        private const val ALLOWED_LOCATION_LIVETIME_MS = 2L * 60L * 1000L // 2 min
        private const val RESTART_SERVICE_TIMER = 10L * 60L * 1000L // 10 min
    }
}