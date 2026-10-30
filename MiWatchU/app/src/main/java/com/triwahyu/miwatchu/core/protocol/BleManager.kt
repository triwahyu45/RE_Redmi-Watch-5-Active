package com.triwahyu.miwatchu.core.protocol

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import java.util.UUID

object BleManager {
    private const val TAG = "BleManager"

    // Model & Known device params from Reverse Engineering
    const val DEFAULT_MODEL = "lchz.watch.n65agl"
    const val DEFAULT_DID = "854548646"

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val _connectionState = MutableStateFlow(BluetoothProfile.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> = _connectionState

    private val _statusMessage = MutableStateFlow("Siap terhubung ke Redmi Watch 5 Active")
    val statusMessage: StateFlow<String> = _statusMessage

    private var currentSeq: Byte = 0

    fun getNextSeq(): Byte {
        currentSeq = ((currentSeq + 1) and 0xFF).toByte()
        return currentSeq
    }

    fun connect(context: Context, macAddress: String) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val device = adapter.getRemoteDevice(macAddress)

        _statusMessage.value = "Menghubungkan ke $macAddress..."
        Log.i(TAG, "Connecting to $macAddress")

        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                _connectionState.value = newState
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _statusMessage.value = "Terkoneksi! Mengatur MTU & Service Discovery..."
                    gatt?.requestMtu(512)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    _statusMessage.value = "Terputus dari jam."
                }
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                Log.i(TAG, "MTU changed to $mtu")
                gatt?.discoverServices()
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    _statusMessage.value = "Koneksi Aktif! Siap kirim data."
                    // Cari SPP Write Characteristic
                    gatt?.services?.forEach { service ->
                        service.characteristics.forEach { char ->
                            if ((char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                                (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                                writeCharacteristic = char
                            }
                        }
                    }
                }
            }
        })
    }

    fun sendNotification(packageName: String, title: String, message: String, isIncomingCall: Boolean = false) {
        val payloadStr = "PKG:$packageName|TITLE:$title|MSG:$message|CALL:$isIncomingCall"
        val packet = L1Packet(
            type = if (isIncomingCall) L1Packet.TYPE_CMD else L1Packet.TYPE_DATA,
            seq = getNextSeq(),
            payload = payloadStr.toByteArray(Charsets.UTF_8)
        )
        sendL1Packet(packet)
    }

    fun sendWatchfaceBinary(binData: ByteArray, onProgress: (Float) -> Unit) {
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
        }
    }

    fun sendL1Packet(packet: L1Packet) {
        val rawBytes = packet.toByteArray()
        val gatt = bluetoothGatt ?: return
        val char = writeCharacteristic ?: return

        char.value = rawBytes
        gatt.writeCharacteristic(char)
    }
}
