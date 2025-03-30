package f.cking.software.domain.interactor

import f.cking.software.data.helpers.LocationProvider
import f.cking.software.data.repo.RadarProfilesRepository
import f.cking.software.domain.interactor.filterchecker.FilterCheckerImpl
import f.cking.software.domain.model.DeviceData
import f.cking.software.domain.model.JournalEntry
import f.cking.software.domain.model.RadarProfile
import f.cking.software.domain.model.SavedDeviceHandle
import f.cking.software.domain.toDomain
import f.cking.software.mapParallel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class CheckBatchForRadarMatchesInteractor(
    private val radarProfilesRepository: RadarProfilesRepository,
    private val filterChecker: FilterCheckerImpl,
    private val saveReportInteractor: SaveReportInteractor,
    private val locationProvider: LocationProvider,
) {

    suspend fun execute(batch: List<SavedDeviceHandle>): List<ProfileResult> {
        return withContext(Dispatchers.Default) {
            val checkStartTime = System.currentTimeMillis()

            // Use original previous detection time for device and it's airdrop info
            // This is needed to correctly process radar profiles that are based on last detection time
            val adjustedDevices = batch.map { handle ->
                handle.device.copy(
                    lastDetectTimeMs = handle.previouslySeenAtTime,
                    manufacturerInfo = handle.device.manufacturerInfo?.let { manufacturerInfo ->
                        manufacturerInfo.copy(
                            airdrop = manufacturerInfo.airdrop?.let { airdrop ->
                                airdrop.copy(
                                    contacts = airdrop.contacts.map { contact ->
                                        val originalLastDetectionTime = handle.airdrop?.contactShaToPreviouslySeenAtTime[contact.sha256]
                                        contact.copy(lastDetectionTimeMs = originalLastDetectionTime ?: handle.previouslySeenAtTime)
                                    }
                                )
                            }
                        )
                    }
                )
            }

            val allProfiles = radarProfilesRepository.getAllProfiles()

            val result = allProfiles.mapParallel { profile ->
                checkProfile(profile, adjustedDevices)
            }.filterNotNull()

            result.forEach { saveReport(it) }

            val totalDuration = System.currentTimeMillis() - checkStartTime
            Timber.tag(TAG).i("Radar detection check: ${result.size} profiles detected. Duration $totalDuration ms")
            result
        }
    }

    private suspend fun checkProfile(profile: RadarProfile, devices: List<DeviceData>): ProfileResult? {
        return profile.takeIf { it.isActive }
            ?.let {
                devices.mapParallel { device ->
                    device.takeIf { profile.detectFilter?.let { filterChecker.check(device, it) } == true }
                }.filterNotNull()
            }
            ?.takeIf { matched -> matched.isNotEmpty() }
            ?.let { matched -> ProfileResult(profile, matched) }
    }

    private suspend fun saveReport(result: ProfileResult) {
        val locationModel = locationProvider.getFreshLocation()

        val report = JournalEntry.Report.ProfileReport(
            profileId = result.profile.id ?: return,
            deviceAddresses = result.matched.map { it.address },
            locationModel = locationModel?.toDomain(System.currentTimeMillis()),
        )

        saveReportInteractor.execute(report)
    }

    data class ProfileResult(
        val profile: RadarProfile,
        val matched: List<DeviceData>,
    )

    companion object {
        private const val TAG = "CheckBatchForRadarMatchesInteractor"
    }
}