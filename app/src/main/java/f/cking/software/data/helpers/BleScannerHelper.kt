package f.cking.software.data.helpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import f.cking.software.domain.model.BleScanDevice
import f.cking.software.toBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BleScannerHelper(
    private val bleFiltersProvider: BleFiltersProvider,
    private val appContext: Context,
    private val powerModeHelper: PowerModeHelper,
) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothScanner: BluetoothLeScanner? = null
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val batch = hashMapOf<String, BleScanDevice>()
    private var currentScanTimeMs: Long = System.currentTimeMillis()
    private val connections: MutableMap<String, BluetoothGatt> = ConcurrentHashMap()

    var inProgress = MutableStateFlow(false)

    private var scanListener: ScanListener? = null

    init {
        tryToInitBluetoothScanner()
    }

    private val callback = object : ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            result.scanRecord?.serviceUuids?.map { bleFiltersProvider.previouslyNoticedServicesUUIDs.add(it.uuid.toString()) }
            val addressType: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                result.device.addressType
            } else {
                null
            }
            val isPaired = result.device.bondState == BluetoothDevice.BOND_BONDED

            val device = BleScanDevice(
                address = result.device.address,
                name = result.device.name ?: result.scanRecord?.deviceName,
                scanTimeMs = currentScanTimeMs,
                scanRecordRaw = result.scanRecord?.bytes,
                rssi = result.rssi,
                addressType = addressType,
                deviceClass = result.device.bluetoothClass.deviceClass,
                isPaired = isPaired,
                serviceUuids = result.scanRecord?.serviceUuids?.map { it.uuid.toString() }.orEmpty(),
                isConnectable = result.isConnectable,
            )

            batch.put(device.address, device)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Timber.tag(TAG).e("BLE Scan failed with error: $errorCode")
            cancelScanning(ScanResultInternal.Failure(errorCode))
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String): Flow<DeviceConnectResult> {
        return callbackFlow {
            val services = mutableSetOf<BluetoothGattService>()
            val device = requireAdapter().getRemoteDevice(address)

            val callback = object : BluetoothGattCallback() {
                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    connections.put(gatt.device.address, gatt)

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Timber.tag(TAG_CONNECT).d("Services discovered. ${gatt.services.size} services for device $address")
                        services.addAll(gatt.services.orEmpty())
                        trySend(DeviceConnectResult.AvailableServices(gatt, services.toList()))
                    } else {
                        Timber.tag(TAG_CONNECT).e("Error while discovering services for device $address. Gatt is null")
                    }
                }

                override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
                    super.onCharacteristicRead(gatt, characteristic, value, status)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Timber.tag(TAG_CONNECT).d("Characteristic read. ${characteristic.uuid}, value: ${value.decodeToString()}")
                        trySend(DeviceConnectResult.CharacteristicRead(gatt, characteristic, value.toBase64()))
                    } else {
                        Timber.tag(TAG_CONNECT).e("Error while reading characteristic ${characteristic.uuid}. Error code: $status")
                        trySend(DeviceConnectResult.FailedReadCharacteristic(gatt, characteristic))
                    }
                }

                override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray) {
                    super.onDescriptorRead(gatt, descriptor, status, value)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Timber.tag(TAG_CONNECT).d("Descriptor read. ${descriptor.uuid}, value: ${value.decodeToString()}")
                        trySend(DeviceConnectResult.DescriptorRead(gatt, descriptor, value.toBase64()))
                    } else {
                        Timber.tag(TAG_CONNECT).e("Error while reading descriptor ${descriptor.uuid}. Error code: $status")
                    }
                }

                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    checkStatus(newState, gatt, status)
                }

                private fun checkStatus(newState: Int, gatt: BluetoothGatt, status: Int) {
                    connections[address] = gatt
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTING -> {
                            Timber.tag(TAG_CONNECT).d("Connecting to device $address")
                            trySend(DeviceConnectResult.Connecting)
                        }
                        BluetoothProfile.STATE_CONNECTED -> {
                            Timber.tag(TAG_CONNECT).d("Connected to device $address")
                            trySend(DeviceConnectResult.Connected(gatt))
                        }
                        BluetoothProfile.STATE_DISCONNECTING -> {
                            Timber.tag(TAG_CONNECT).d("Disconnecting from device $address")
                            trySend(DeviceConnectResult.Disconnecting)
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            Timber.tag(TAG_CONNECT).d("Disconnected from device $address")
                            handleDisconnect(status)
                        }
                        else -> {
                            Timber.tag(TAG_CONNECT).e("Error while connecting to device $address. Error code: $status")
                            trySend(DeviceConnectResult.DisconnectedWithError.UnspecifiedConnectionError(status))
                        }
                    }
                }

                private fun handleDisconnect(status: Int) {
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> {
                            trySend(DeviceConnectResult.Disconnected)
                        }
                        CONNECTION_FAILED_TO_ESTABLISH -> {
                            Timber.tag(TAG_CONNECT).e("Error while connecting to device $address. Error code: $status")
                            trySend(DeviceConnectResult.DisconnectedWithError.ConnectionFailedToEstablish(status))
                        }
                        CONNECTION_FAILED_BEFORE_INITIALIZING -> {
                            Timber.tag(TAG_CONNECT).e("Error while connecting to device $address. Error code: $status")
                            trySend(DeviceConnectResult.DisconnectedWithError.ConnectionFailedBeforeInitializing(status))
                        }
                        CONNECTION_TERMINATED -> {
                            Timber.tag(TAG_CONNECT).e("Error while connecting to device $address. Error code: $status")
                            trySend(DeviceConnectResult.DisconnectedWithError.ConnectionTerminated(status))
                        }
                        BluetoothGatt.GATT_CONNECTION_TIMEOUT -> {
                            Timber.tag(TAG_CONNECT).e("Error while connecting to device $address. Error code: $status")
                            trySend(DeviceConnectResult.DisconnectedWithError.ConnectionTimeout(status))
                        }
                        else -> {
                            Timber.tag(TAG_CONNECT).e("Error while connecting to device $address. Error code: $status")
                            trySend(DeviceConnectResult.DisconnectedWithError.UnspecifiedConnectionError(status))
                        }
                    }
                }
            }

            Timber.tag(TAG_CONNECT).d("Connecting to device $address")
            connections[address] = device.connectGatt(appContext, false, callback, BluetoothDevice.TRANSPORT_LE)

            awaitClose {
                Timber.tag(TAG_CONNECT).d("Closing connection to device $address")
                closeDeviceConnection(address)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun discoverServices(gatt: BluetoothGatt) {
        Timber.tag(TAG_CONNECT).d("Discovering services for device ${gatt.device.address}")
        gatt.discoverServices()
    }

    @SuppressLint("MissingPermission")
    fun disconnect(gatt: BluetoothGatt) {
        Timber.tag(TAG_CONNECT).d("Disconnecting from device ${gatt.device.address}")
        gatt.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun close(gatt: BluetoothGatt) {
        Timber.tag(TAG_CONNECT).d("Closing connection to device ${gatt.device.address}")
        gatt.close()
    }

    fun closeDeviceConnection(address: String) {
        connections[address]?.let(::close)
        connections.remove(address)
    }

    fun closeAllConnections() {
        connections.values.forEach(::close)
        connections.clear()
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        Timber.tag(TAG_CONNECT).d("Reading characteristic ${characteristic.uuid}")
        val isSuccess = gatt.readCharacteristic(characteristic)
        if (!isSuccess) {
            Timber.tag(TAG_CONNECT).e("Error while reading characteristic ${characteristic.uuid}")
        }
    }

    @SuppressLint("MissingPermission")
    fun readDescriptor(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, descriptorUuid: UUID) {
        Timber.tag(TAG_CONNECT).d("Reading descriptor $descriptorUuid for characteristic ${characteristic.uuid}")
        val descriptor = characteristic.getDescriptor(descriptorUuid)
        gatt.readDescriptor(descriptor)
    }

    sealed interface DeviceConnectResult {
        data class AvailableServices(val gatt: BluetoothGatt, val services: List<BluetoothGattService>) : DeviceConnectResult
        data class CharacteristicRead(val gatt: BluetoothGatt, val characteristic: BluetoothGattCharacteristic, val valueEncoded64: String) : DeviceConnectResult
        data class FailedReadCharacteristic(val gatt: BluetoothGatt, val characteristic: BluetoothGattCharacteristic) : DeviceConnectResult
        data class DescriptorRead(val gatt: BluetoothGatt, val descriptor: BluetoothGattDescriptor, val valueEncoded64: String) : DeviceConnectResult
        data object Connecting : DeviceConnectResult
        data class Connected(val gatt: BluetoothGatt) : DeviceConnectResult
        data object Disconnecting : DeviceConnectResult
        data object Disconnected : DeviceConnectResult
        sealed interface DisconnectedWithError : DeviceConnectResult {
            val errorCode: Int

            class UnspecifiedConnectionError(override val errorCode: Int) : DisconnectedWithError
            class ConnectionTimeout(override val errorCode: Int) : DisconnectedWithError
            class ConnectionTerminated(override val errorCode: Int) : DisconnectedWithError
            class ConnectionFailedToEstablish(override val errorCode: Int) : DisconnectedWithError
            class ConnectionFailedBeforeInitializing(override val errorCode: Int) : DisconnectedWithError
        }
    }

    fun isBluetoothEnabled(): Boolean {
        tryToInitBluetoothScanner()
        return bluetoothAdapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    suspend fun scan(
        scanListener: ScanListener,
    ) {
        Timber.tag(TAG).d("Start BLE Scan. Restricted mode: ${powerModeHelper.powerMode().useRestrictedBleConfig}")

        if (!isBluetoothEnabled()) {
            throw BluetoothIsNotInitialized()
        }

        if (inProgress.value) {
            Timber.tag(TAG).e("BLE Scan failed because previous scan is not finished")
        } else {
            this@BleScannerHelper.scanListener = scanListener
            batch.clear()

            inProgress.tryEmit(true)
            currentScanTimeMs = System.currentTimeMillis()

            val powerMode = powerModeHelper.powerMode()
            val scanFilters = if (powerMode.useRestrictedBleConfig) {
                bleFiltersProvider.getBackgroundFilters()
            } else {
                listOf(ScanFilter.Builder().build())
            }

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            withContext(Dispatchers.IO) {
                requireScanner().startScan(scanFilters, scanSettings, callback)
                handler.postDelayed({ cancelScanning(ScanResultInternal.Success) }, powerModeHelper.powerMode().scanDuration)
            }
        }
    }

    fun stopScanning() {
        cancelScanning(ScanResultInternal.Canceled)
    }

    @SuppressLint("MissingPermission")
    private fun cancelScanning(scanResult: ScanResultInternal) {
        inProgress.tryEmit(false)

        if (bluetoothAdapter?.state == BluetoothAdapter.STATE_ON) {
            bluetoothScanner?.stopScan(callback)
            requireAdapter().cancelDiscovery()
        }

        when (scanResult) {
            is ScanResultInternal.Success -> {
                Timber.tag(TAG).d("BLE Scan finished ${batch.count()} devices found")
                scanListener?.onSuccess(batch.values.toList())
            }

            is ScanResultInternal.Failure -> {
                scanListener?.onFailure(BLEScanFailure(scanResult.errorCode, BleScanErrorMapper.map(scanResult.errorCode)))
            }

            is ScanResultInternal.Canceled -> {
                // do nothing
            }
        }
        scanListener = null
    }

    private fun tryToInitBluetoothScanner() {
        bluetoothAdapter = appContext.getSystemService(BluetoothManager::class.java).adapter
        bluetoothScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    private fun requireScanner(): BluetoothLeScanner {
        if (bluetoothScanner == null) {
            tryToInitBluetoothScanner()
        }
        return bluetoothScanner ?: throw BluetoothIsNotInitialized()
    }

    private fun requireAdapter(): BluetoothAdapter {
        if (bluetoothAdapter == null) {
            tryToInitBluetoothScanner()
        }
        return bluetoothAdapter ?: throw BluetoothIsNotInitialized()
    }

    interface ScanListener {
        fun onSuccess(batch: List<BleScanDevice>)
        fun onFailure(exception: Exception)
    }

    private sealed interface ScanResultInternal {

        object Success : ScanResultInternal

        data class Failure(val errorCode: Int) : ScanResultInternal

        object Canceled : ScanResultInternal
    }

    class BLEScanFailure(errorCode: Int, errorDescription: String) :
        RuntimeException("BLE Scan failed with error code: $errorCode (${errorDescription})")

    class BluetoothIsNotInitialized : RuntimeException("Bluetooth is turned off or not available on this device")

    companion object {
        private const val TAG = "BleScannerHelper"
        private const val TAG_CONNECT = "BleScannerHelperConnect"
        private const val CONNECTION_FAILED_BEFORE_INITIALIZING = 0x85
        private const val CONNECTION_FAILED_TO_ESTABLISH = 0x3E
        private const val CONNECTION_TERMINATED = 0x16
    }
}