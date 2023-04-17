package f.cking.software.domain.interactor.filterchecker

import android.util.Log
import f.cking.software.domain.model.DeviceData
import f.cking.software.domain.model.RadarProfile

abstract class FilterChecker<T : RadarProfile.Filter> {

    private val cache: MutableMap<String, CacheValue> = mutableMapOf()

    suspend fun check(deviceData: DeviceData, filter: T): Boolean {
        val key = "${deviceData.address}_${filter.hashCode()}_${filter::class.simpleName}"
        val cacheValue = cache[key]
        if (useCache() && cacheValue != null && System.currentTimeMillis() - cacheValue.time < EXPIRATION_TIME) {
            Log.d("FilterChecker", "Cache hit for $key")
            return cacheValue.value
        }
        val result = checkInternal(deviceData, filter)
        cache[key] = CacheValue(System.currentTimeMillis(), result)
        return result
    }

    abstract suspend fun checkInternal(deviceData: DeviceData, filter: T): Boolean

    protected open fun useCache(): Boolean = true

    open fun clearCache() {
        cache.clear()
    }

    private data class CacheValue(
        val time: Long,
        val value: Boolean,
    )

    companion object {
        private const val EXPIRATION_TIME = 5 * 60 * 1000L // 5 minutes
    }
}