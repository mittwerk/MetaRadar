package f.cking.software.data.repo

import android.content.SharedPreferences
import androidx.core.content.edit
import f.cking.software.BuildConfig
import f.cking.software.TheAppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SettingsRepository(
    private val sharedPreferences: SharedPreferences,
) {

    private val silentModeState = MutableStateFlow(getSilentMode())
    private val hideBackgroundLocationWarning = MutableStateFlow(getHideBackgroundLocationWarning())

    fun setGarbagingTime(time: Long) {
        sharedPreferences.edit().putLong(KEY_GARBAGING_TIME, time).apply()
    }

    fun getGarbagingTime(): Long {
        return sharedPreferences.getLong(KEY_GARBAGING_TIME, TheAppConfig.DEVICE_GARBAGING_TIME)
    }

    fun setUseGpsLocationOnly(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_USE_GPS_ONLY, value).apply()
    }

    fun getUseGpsLocationOnly(): Boolean {
        return sharedPreferences.getBoolean(KEY_USE_GPS_ONLY, TheAppConfig.USE_GPS_LOCATION_ONLY)
    }

    fun getPermissionsIntroWasShown(): Boolean {
        return sharedPreferences.getBoolean(KEY_PERMISSIONS_INTRO_WAS_SHOWN, false)
    }

    fun setPermissionsIntroWasShown(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PERMISSIONS_INTRO_WAS_SHOWN, value).apply()
    }

    fun getRunOnStartup(): Boolean {
        return sharedPreferences.getBoolean(KEY_RUN_ON_STARTUP, false)
    }

    fun setRunOnStartup(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_RUN_ON_STARTUP, value).apply()
    }

    fun getFirstAppLaunchTime(): Long {
        return sharedPreferences.getLong(KEY_FIRST_APP_LAUNCH_TIME, NO_APP_LAUNCH_TIME)
    }

    fun setFirstAppLaunchTime(value: Long) {
        sharedPreferences.edit().putLong(KEY_FIRST_APP_LAUNCH_TIME, value).apply()
    }

    fun getEnjoyTheAppAnswered(): Boolean {
        return sharedPreferences.getBoolean(KEY_ENJOY_THE_APP_ANSWERED, false)
    }

    fun setEnjoyTheAppAnswered(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ENJOY_THE_APP_ANSWERED, value).apply()
    }

    fun setHideBackgroundLocationWarning(value: Long) {
        sharedPreferences.edit { putLong(KEY_HIDE_BACKGROUND_LOCATION_WARNING, value) }
        hideBackgroundLocationWarning.tryEmit(value)
    }

    fun getHideBackgroundLocationWarning(): Long {
        return sharedPreferences.getLong(KEY_HIDE_BACKGROUND_LOCATION_WARNING, 0L)
    }

    fun observeHideBackgroundLocationWarning(): Flow<Long> {
        return hideBackgroundLocationWarning
    }

    fun getEnjoyTheAppStartingPoint(): Long {
        return sharedPreferences.getLong(KEY_ENJOY_THE_APP_STARTING_POINT, NO_ENJOY_THE_APP_STARTING_POINT)
    }

    fun setEnjoyTheAppStartingPoint(value: Long) {
        sharedPreferences.edit { putLong(KEY_ENJOY_THE_APP_STARTING_POINT, value) }
    }

    fun setSilentMode(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_SILENT_NETWORK_MODE, enabled) }
        silentModeState.tryEmit(getSilentMode())
    }

    fun getSilentMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_SILENT_NETWORK_MODE, BuildConfig.OFFLINE_MODE_DEFAULT_STATE)
    }

    fun observeSilentMode(): Flow<Boolean> {
        return silentModeState
    }

    fun getCurrentBatchSortingStrategyId(): Int {
        return sharedPreferences.getInt(KEY_CURRENT_BATCH_SORTING_STRATEGY_ID, 0)
    }

    fun setCurrentBatchSortingStrategyId(value: Int) {
        sharedPreferences.edit { putInt(KEY_CURRENT_BATCH_SORTING_STRATEGY_ID, value) }
    }

    fun setEnableDeepAnalysis(value: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_ENABLE_DEEP_ANALYSIS, value) }
    }

    fun getEnableDeepAnalysis(): Boolean {
        return sharedPreferences.getBoolean(KEY_ENABLE_DEEP_ANALYSIS, false)
    }

    fun setDisclaimerWasAccepted(value: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_DISCLAIMER_WAS_ACCEPTED, value) }
    }

    fun getDisclaimerWasAccepted(): Boolean {
        return sharedPreferences.getBoolean(KEY_DISCLAIMER_WAS_ACCEPTED, false)
    }

    companion object {
        private const val KEY_GARBAGING_TIME = "key_garbaging_time"
        private const val KEY_USE_GPS_ONLY = "key_use_gps_location_only"
        private const val KEY_PERMISSIONS_INTRO_WAS_SHOWN = "key_permissions_intro_was_shown"
        private const val KEY_RUN_ON_STARTUP = "key_run_on_startup"
        private const val KEY_FIRST_APP_LAUNCH_TIME = "key_first_app_launch_time"
        private const val KEY_ENJOY_THE_APP_ANSWERED = "key_enjoy_the_app_answered_v1"
        private const val KEY_ENJOY_THE_APP_STARTING_POINT = "key_enjoy_the_app_starting_point"
        private const val KEY_SILENT_NETWORK_MODE = "silent_network_mode"
        private const val KEY_CURRENT_BATCH_SORTING_STRATEGY_ID = "key_current_batch_sorting_strategy_id"
        private const val KEY_HIDE_BACKGROUND_LOCATION_WARNING = "key_hide_background_location_warning"
        private const val KEY_ENABLE_DEEP_ANALYSIS = "key_enable_deep_analysis"
        private const val KEY_DISCLAIMER_WAS_ACCEPTED = "key_disclaimer_was_accepted"

        const val NO_APP_LAUNCH_TIME = -1L
        const val NO_ENJOY_THE_APP_STARTING_POINT = -1L
    }
}