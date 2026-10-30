package com.triwahyu.miwatchu.core.protocol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.UUID

@SuppressLint("MissingPermission")
object BleManager {
    private const val TAG = "BleManager"

    // Real extracted hardware & auth credentials from reverse engineering
    const val DEFAULT_MODEL = "lchz.watch.n65agl"
    const val DEFAULT_DID = "854548646"
    const val FIRMWARE_VERSION = "2.2.110"
    const val AUTH_TOKEN = "3d8f7d72edef2e72b7b47984df76de28"
    const val ENCRYPT_KEY = "3d8f7d72edef2e72b7b47984df76de28"
    const val BEACON_KEY = "4fd1b463c9c5f4f1e4ae72936f338834"
    const val IRQ_KEY = "a342fe2b01c272c445f2fedae2683ac9"
    const val PHONE_ID = "50bb3"

    // EXACT Proprietary Xiaomi Wearable L1 Transport UUIDs (Extracted from DEX)
    val WEARABLE_SERVICE_UUID: UUID = UUID.fromString("00001000-1720-0206-0100-00805f9bab34")
    val WEARABLE_CHAR_UUID: UUID    = UUID.fromString("00002000-1720-0206-0100-00805f9bab34")
    val CCCD_UUID: UUID             = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    val SPP_UUID: UUID              = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var activeDevice: BluetoothDevice? = null
    private var sppSocket: BluetoothSocket? = null
    private var sppOutputStream: OutputStream? = null
    private var sppInputStream: InputStream? = null

    private var bluetoothGatt: BluetoothGatt? = null
    private var l1Characteristic: BluetoothGattCharacteristic? = null

    private val _connectionState = MutableStateFlow(BluetoothProfile.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> = _connectionState

    private val _statusMessage = MutableStateFlow("Siap terhubung secara independen")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _targetMac = MutableStateFlow("Mencari...")
    val targetMac: StateFlow<String> = _targetMac

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices

    private var currentSeq: Byte = 0

    init {
        findBondedWatch()
    }

    fun startBleScan(context: Context) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val scanner = adapter.bluetoothLeScanner ?: return
        _isScanning.value = true
        _statusMessage.value = "🔍 Memindai jam di sekitar (Mode Pairing)..."
        _discoveredDevices.value = emptyList()

        val callback = object : android.bluetooth.le.ScanCallback() {
            override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
                val dev = result?.device ?: return
                val name = dev.name ?: result.scanRecord?.deviceName
                if (name?.contains("Redmi", ignoreCase = true) == true ||
                    name?.contains("Watch", ignoreCase = true) == true ||
                    name?.contains("n65a", ignoreCase = true) == true ||
                    dev.address.equals("04:DA:28:83:B5:83", ignoreCase = true)) {
                    
                    val current = _discoveredDevices.value.toMutableList()
                    if (current.none { it.address == dev.address }) {
                        current.add(dev)
                        _discoveredDevices.value = current
                        _statusMessage.value = "Ditemukan: ${name ?: dev.address}"
                        Log.i(TAG, "Discovered device: ${name ?: "Unknown"} (${dev.address})")
                    }
                }
            }
        }
        try {
            scanner.startScan(callback)
            CoroutineScope(Dispatchers.Main).launch {
                delay(10000)
                try { scanner.stopScan(callback) } catch (e: Exception) {}
                _isScanning.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting scan: ${e.message}")
            _isScanning.value = false
        }
    }

    fun pairDevice(context: Context, device: BluetoothDevice) {
        _statusMessage.value = "Memasangkan dengan ${device.name ?: device.address}..."
        activeDevice = device
        _targetMac.value = device.address
        try {
            device.createBond()
        } catch (e: Exception) {
            Log.w(TAG, "createBond failed: ${e.message}")
        }
        connect(context, device.address)
    }

    fun sendRunningTextDirect(text: String, speed: Int = 3) {
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val payload = ByteBuffer.allocate(4 + textBytes.size).order(ByteOrder.LITTLE_ENDIAN)
        payload.put(0x05.toByte()) // Subsystem Marquee
        payload.put(0x01.toByte()) // Command Set Text
        payload.put(speed.toByte())
        payload.put(textBytes.size.toByte())
        payload.put(textBytes)

        sendL1Packet(L1Packet(L1Packet.TYPE_CMD, getNextSeq(), payload.array()))
        _statusMessage.value = "⚡ Running text diaplikasikan ke jam: $text"
        Log.i(TAG, "Sent running text to watch: $text")
    }

    fun findBondedWatch(): BluetoothDevice? {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
        try {
            val bonded = adapter.bondedDevices
            for (dev in bonded) {
                if (dev.name?.contains("Redmi Watch", ignoreCase = true) == true ||
                    dev.name?.contains("n65a", ignoreCase = true) == true) {
                    activeDevice = dev
                    _targetMac.value = dev.address
                    _statusMessage.value = "Ditemukan: ${dev.name} (${dev.address})"
                    Log.i(TAG, "Found bonded watch: ${dev.name} ${dev.address}")
                    return dev
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying bonded devices: ${e.message}")
        }
        return null
    }

    fun getNextSeq(): Byte {
        currentSeq = ((currentSeq + 1) and 0xFF).toByte()
        return currentSeq
    }

    fun connect(context: Context, manualMac: String? = null) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val device = if (!manualMac.isNullOrEmpty() && manualMac != "Mencari...") {
            try { adapter.getRemoteDevice(manualMac) } catch (e: Exception) { activeDevice }
        } else {
            activeDevice ?: findBondedWatch()
        }

        if (device == null) {
            _statusMessage.value = "❌ Jam belum di-pair di Bluetooth ponsel!"
            return
        }

        activeDevice = device
        _targetMac.value = device.address
        _statusMessage.value = "Menghubungkan ke ${device.name ?: device.address}..."

        // Prioritas 1: Bluetooth Classic SPP (RFCOMM Serial Stream)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Attempting SPP connection to ${device.address}...")
                val socket = try {
                    device.createRfcommSocketToServiceRecord(SPP_UUID)
                } catch (e: Exception) {
                    device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                }
                adapter.cancelDiscovery()
                socket.connect()
                sppSocket = socket
                sppOutputStream = socket.outputStream
                sppInputStream = socket.inputStream
                _connectionState.value = BluetoothProfile.STATE_CONNECTED
                _statusMessage.value = "✅ SPP Terkoneksi! Jalur komunikasi aktif."
                Log.i(TAG, "SPP Connected successfully!")

                sendAuthHandshake()
            } catch (e: Exception) {
                Log.w(TAG, "SPP connection failed: ${e.message}, falling back to BLE GATT...")
            }

            // Prioritas 2: Wearable BLE GATT Service (00001000-1720-0206-0100-00805f9bab34)
            connectGatt(context, device)
        }
    }

    private fun connectGatt(context: Context, device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                _connectionState.value = newState
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _statusMessage.value = "✅ BLE Terhubung! Mengatur MTU 512..."
                    gatt?.requestMtu(512)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (sppSocket?.isConnected != true) {
                        _statusMessage.value = "Terputus. Pastikan Mi Fitness di-Force Stop jika bentrok."
                    }
                }
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                Log.i(TAG, "MTU changed to $mtu")
                gatt?.discoverServices()
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                    Log.i(TAG, "=== DISCOVERED ${gatt.services.size} GATT SERVICES ===")
                    for (srv in gatt.services) {
                        Log.i(TAG, "SERVICE: ${srv.uuid}")
                        for (chr in srv.characteristics) {
                            val props = mutableListOf<String>()
                            if ((chr.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) props.add("READ")
                            if ((chr.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) props.add("WRITE")
                            if ((chr.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) props.add("WRITE_NO_RESP")
                            if ((chr.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) props.add("NOTIFY")
                            if ((chr.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) props.add("INDICATE")
                            Log.i(TAG, "  -> CHAR: ${chr.uuid} [${props.joinToString(",")}]")
                        }
                    }

                    // Cari Service Resmi Wearable L1 Transport
                    val service = gatt.getService(WEARABLE_SERVICE_UUID)
                    if (service != null) {
                        val characteristic = service.getCharacteristic(WEARABLE_CHAR_UUID)
                        if (characteristic != null) {
                            l1Characteristic = characteristic
                            gatt.setCharacteristicNotification(characteristic, true)
                            val descriptor = characteristic.getDescriptor(CCCD_UUID)
                            if (descriptor != null) {
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                            }
                            _statusMessage.value = "✅ Jalur Xiaomi Wearable L1 Aktif!"
                            Log.i(TAG, "Xiaomi Wearable L1 Characteristic hooked successfully!")
                            sendAuthHandshake()
                            return
                        }
                    }

                    // Fallback jika UUID proprietary berbeda di mode tertentu
                    _statusMessage.value = "✅ Layanan Ditemukan! Mengaitkan characteristic..."
                    for (srv in gatt.services) {
                        for (chr in srv.characteristics) {
                            if ((chr.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                                (chr.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                                l1Characteristic = chr
                                gatt.setCharacteristicNotification(chr, true)
                                Log.i(TAG, "Hooked writable characteristic: ${chr.uuid}")
                                break
                            }
                        }
                        if (l1Characteristic != null) break
                    }
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                val data = characteristic?.value
                if (data != null) {
                    val hex = data.joinToString(" ") { String.format("%02X", it) }
                    Log.i(TAG, "📥 RECEIVED FROM WATCH (${characteristic.uuid}): $hex")
                    _statusMessage.value = "📥 Respons Jam: ${hex.take(20)}..."
                }
            }
        })
    }

    private fun sendAuthHandshake() {
        val authPayload = ByteArray(20)
        authPayload[0] = 0x01 // Subsystem Auth
        authPayload[1] = 0x01 // Init challenge
        val tokenBytes = AUTH_TOKEN.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        System.arraycopy(tokenBytes, 0, authPayload, 2, Math.min(16, tokenBytes.size))

        val packet = L1Packet(L1Packet.TYPE_CMD, getNextSeq(), authPayload)
        sendL1Packet(packet)
        Log.i(TAG, "Auth handshake sent to watch.")
    }

    fun sendNotification(packageName: String, title: String, message: String, isIncomingCall: Boolean = false) {
        val payloadStr = "PKG:$packageName|TITLE:$title|MSG:$message|CALL:$isIncomingCall"
        val packet = L1Packet(
            type = if (isIncomingCall) L1Packet.TYPE_CMD else L1Packet.TYPE_DATA,
            seq = getNextSeq(),
            payload = payloadStr.toByteArray(Charsets.UTF_8)
        )
        sendL1Packet(packet)
        _statusMessage.value = "Pesan terkirim: $title"
    }

    fun isConnected(): Boolean {
        return sppSocket?.isConnected == true || l1Characteristic != null
    }

    fun sendWatchfaceBinary(binData: ByteArray, onProgress: (Float) -> Unit): Boolean {
        if (!isConnected()) {
            _statusMessage.value = "❌ Gagal: Bluetooth belum terhubung ke jam!"
            Log.e(TAG, "Cannot send watchface: No active SPP or BLE connection")
            return false
        }
        val md5 = MessageDigest.getInstance("MD5").digest(binData)
        val totalSize = binData.size

        // First Chunk Header: [02 01 27 00][Chunk 0][Marker 00 10][16B MD5][4B Size LE]
        val headerPayload = ByteArray(28)
        headerPayload[0] = 0x02
        headerPayload[1] = 0x01
        headerPayload[2] = 0x27
        headerPayload[3] = 0x00
        headerPayload[4] = 0x00 // chunk num
        headerPayload[5] = 0x00
        headerPayload[6] = 0x00 // marker
        headerPayload[7] = 0x10
        System.arraycopy(md5, 0, headerPayload, 8, 16)
        headerPayload[24] = (totalSize and 0xFF).toByte()
        headerPayload[25] = ((totalSize shr 8) and 0xFF).toByte()
        headerPayload[26] = ((totalSize shr 16) and 0xFF).toByte()
        headerPayload[27] = ((totalSize shr 24) and 0xFF).toByte()

        sendL1Packet(L1Packet(L1Packet.TYPE_CMD, getNextSeq(), headerPayload))

        // Subsequent data chunks (500 bytes per chunk)
        val chunkSize = 500
        var offset = 0
        while (offset < totalSize) {
            val length = Math.min(chunkSize, totalSize - offset)
            val chunk = ByteArray(length)
            System.arraycopy(binData, offset, chunk, 0, length)
            sendL1Packet(L1Packet(L1Packet.TYPE_DATA, getNextSeq(), chunk))
            offset += length
            onProgress(offset.toFloat() / totalSize.toFloat())
            Thread.sleep(15) // small pacing to prevent queue overrun
        }
        return true
    }

    fun sendL1Packet(packet: L1Packet) {
        val rawBytes = packet.toByteArray()

        // 1. Kirim via SPP jika tersedia
        val out = sppOutputStream
        if (sppSocket?.isConnected == true && out != null) {
            try {
                out.write(rawBytes)
                out.flush()
                Log.d(TAG, "Sent ${rawBytes.size} bytes via SPP")
                return
            } catch (e: Exception) {
                Log.w(TAG, "SPP write failed: ${e.message}")
            }
        }

        // 2. Kirim via Wearable L1 Characteristic
        val gatt = bluetoothGatt
        val char = l1Characteristic
        if (gatt != null && char != null) {
            char.value = rawBytes
            gatt.writeCharacteristic(char)
            Log.d(TAG, "Sent ${rawBytes.size} bytes via Wearable L1 Characteristic (${char.uuid})")
        } else {
            Log.w(TAG, "Neither SPP nor Wearable L1 Characteristic is currently connected.")
        }
    }
}
