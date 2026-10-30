package com.triwahyu.miwatchu.core.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * L1 Packet Protocol Implementation untuk Xiaomi Redmi Watch 5 Active (SF32LB551)
 * Header format: Magic(2B) + Type(1B) + Seq(1B) + Len(2B LE) + CRC(2B LE) = 8 bytes
 */
data class L1Packet(
    val type: Byte,
    val seq: Byte,
    val payload: ByteArray
) {
    companion object {
        const val MAGIC: Short = 0xA5A5.toShort()
        const val TYPE_NAK: Byte = 0
        const val TYPE_ACK: Byte = 1
        const val TYPE_CMD: Byte = 2
        const val TYPE_DATA: Byte = 3

        fun calculateCrc16(data: ByteArray, offset: Int, length: Int): Short {
            var crc = 0xFFFF
            for (i in offset until offset + length) {
                crc = crc xor ((data[i].toInt() and 0xFF) shl 8)
                for (j in 0 until 8) {
                    crc = if ((crc and 0x8000) != 0) {
                        (crc shl 1) xor 0x1021
                    } else {
                        crc shl 1
                    }
                }
            }
            return (crc and 0xFFFF).toShort()
        }

        fun parse(bytes: ByteArray): L1Packet? {
            if (bytes.size < 8) return null
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val magic = buffer.short
            if (magic != MAGIC) return null

            val type = buffer.get()
            val seq = buffer.get()
            val len = buffer.short.toInt() and 0xFFFF
            val receivedCrc = buffer.short

            if (bytes.size < 8 + len) return null
            val computedCrc = calculateCrc16(bytes, 8, len)
            if (computedCrc != receivedCrc) return null

            val payload = ByteArray(len)
            buffer.get(payload)
            return L1Packet(type, seq, payload)
        }
    }

    fun toByteArray(): ByteArray {
        val totalSize = 8 + payload.size
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(MAGIC)
        buffer.put(type)
        buffer.put(seq)
        buffer.putShort(payload.size.toShort())

        val crc = calculateCrc16(payload, 0, payload.size)
        buffer.putShort(crc)
        buffer.put(payload)
        return buffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as L1Packet
        if (type != other.type) return false
        if (seq != other.seq) return false
        if (!payload.contentEquals(other.payload)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.toInt()
        result = 31 * result + seq.toInt()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
