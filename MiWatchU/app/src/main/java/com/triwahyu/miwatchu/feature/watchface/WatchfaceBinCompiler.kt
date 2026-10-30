package com.triwahyu.miwatchu.feature.watchface

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Watchface Bin Compiler untuk Redmi Watch 5 Active
 * Magic: 0x1234A55A, Resource table di 0x1C0, LVGL format 0x06 (RGB565)
 */
data class ClockConfig(
    val posX: Float = 40f,
    val posY: Float = 80f,
    val showTime: Boolean = true,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val showBattery: Boolean = true,
    val textColor: Int = Color.CYAN,
    val textSize: Float = 64f
)

object WatchfaceBinCompiler {
    private val MAGIC = byteArrayOf(0x5A, 0xA5.toByte(), 0x34, 0x12)
    private const val RESOURCE_TABLE_START = 0x1C0
    private const val HEADER_SIZE = 0x3A0
    const val WATCH_WIDTH = 320
    const val WATCH_HEIGHT = 385

    fun encodeLvglImage(bitmap: Bitmap, formatType: Byte = 0x06): ByteArray {
        val w = bitmap.width
        val h = bitmap.height
        val output = ByteArrayOutputStream()

        // LVGL 8-byte Header: Magic 0x28, Format, Flags, Width (LE), Height (LE)
        val header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        header.put(0x28.toByte())
        header.put(formatType)
        header.putShort(0.toShort())
        header.putShort(w.toShort())
        header.putShort(h.toShort())
        output.write(header.array())

        // RGB565 Little Endian pixel payload
        val pixelBuffer = ByteBuffer.allocate(w * h * 2).order(ByteOrder.LITTLE_ENDIAN)
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        for (color in pixels) {
            val r = (Color.red(color) shr 3) and 0x1F
            val g = (Color.green(color) shr 2) and 0x3F
            val b = (Color.blue(color) shr 3) and 0x1F
            val rgb565 = ((r shl 11) or (g shl 5) or b).toShort()
            pixelBuffer.putShort(rgb565)
        }

        output.write(pixelBuffer.array())
        return output.toByteArray()
    }

    fun renderFrameWithClock(
        baseFrame: Bitmap,
        clockConfig: ClockConfig,
        mockTime: String = "10:30",
        mockDate: String = "JUMAT, 30 OKT"
    ): Bitmap {
        val composite = Bitmap.createScaledBitmap(baseFrame, WATCH_WIDTH, WATCH_HEIGHT, true).copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(composite)

        val paintTime = Paint().apply {
            color = clockConfig.textColor
            textSize = clockConfig.textSize
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }

        val paintSub = Paint().apply {
            color = Color.WHITE
            textSize = clockConfig.textSize * 0.35f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        var currentY = clockConfig.posY
        if (clockConfig.showTime) {
            val displayTime = if (clockConfig.showSeconds) "$mockTime:45" else mockTime
            canvas.drawText(displayTime, clockConfig.posX, currentY, paintTime)
            currentY += clockConfig.textSize * 0.5f
        }

        if (clockConfig.showDate) {
            canvas.drawText(mockDate, clockConfig.posX, currentY, paintSub)
            currentY += clockConfig.textSize * 0.4f
        }

        if (clockConfig.showBattery) {
            canvas.drawText("🔋 98%", clockConfig.posX, currentY, paintSub)
        }

        return composite
    }

    fun generateThemedFrames(
        theme: String,
        frameCount: Int
    ): List<Bitmap> {
        val list = mutableListOf<Bitmap>()
        val count = frameCount.coerceAtLeast(1)

        for (i in 0 until count) {
            val bmp = Bitmap.createBitmap(WATCH_WIDTH, WATCH_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val phase = (i.toFloat() / count.toFloat()) * (2f * Math.PI.toFloat())

            when (theme) {
                "Cyber Neon" -> {
                    // Dark background with pulsating cyan-purple ring
                    canvas.drawColor(android.graphics.Color.rgb(10, 12, 18))
                    val paintRing = Paint().apply {
                        color = android.graphics.Color.argb((140 + 60 * Math.sin(phase.toDouble())).toInt(), 0, 229, 255)
                        style = Paint.Style.STROKE
                        strokeWidth = 6f
                        isAntiAlias = true
                    }
                    canvas.drawCircle(WATCH_WIDTH / 2f, WATCH_HEIGHT / 2f, 110f + 8f * Math.sin(phase.toDouble()).toFloat(), paintRing)

                    val paintGrid = Paint().apply {
                        color = android.graphics.Color.argb(35, 189, 0, 255)
                        strokeWidth = 1f
                    }
                    for (x in 0..WATCH_WIDTH step 40) {
                        canvas.drawLine(x.toFloat(), 0f, x.toFloat(), WATCH_HEIGHT.toFloat(), paintGrid)
                    }
                    for (y in 0..WATCH_HEIGHT step 40) {
                        canvas.drawLine(0f, y.toFloat(), WATCH_WIDTH.toFloat(), y.toFloat(), paintGrid)
                    }
                }
                "Titanium Luxury" -> {
                    // Brushed slate with warm gold geometric accent
                    canvas.drawColor(android.graphics.Color.rgb(22, 24, 29))
                    val paintAccent = Paint().apply {
                        color = android.graphics.Color.rgb(255, 184, 0)
                        style = Paint.Style.STROKE
                        strokeWidth = 4f
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(20f, 20f, WATCH_WIDTH - 20f, WATCH_HEIGHT - 20f, 24f, 24f, paintAccent)
                }
                "Emerald Matrix" -> {
                    // Pure OLED black with dynamic green telemetry bars
                    canvas.drawColor(android.graphics.Color.BLACK)
                    val paintBar = Paint().apply {
                        color = android.graphics.Color.rgb(0, 255, 157)
                        strokeWidth = 3f
                    }
                    val barHeight = 60f + 30f * Math.sin(phase.toDouble()).toFloat()
                    canvas.drawLine(24f, WATCH_HEIGHT - 40f, 24f, WATCH_HEIGHT - 40f - barHeight, paintBar)
                    canvas.drawLine(34f, WATCH_HEIGHT - 40f, 34f, WATCH_HEIGHT - 40f - (barHeight * 0.7f), paintBar)
                }
                else -> {
                    // Minimalist OLED Slate
                    canvas.drawColor(android.graphics.Color.rgb(14, 15, 18))
                }
            }
            list.add(bmp)
        }
        return list
    }

    fun compile(
        frames: List<Bitmap>,
        clockConfig: ClockConfig,
        id: String = "120917344162",
        name: String = "Mi WatchU Live"
    ): ByteArray {
        val header = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        header.put(MAGIC)
        header.putInt(0x00010000)
        header.position(0x14)
        header.putInt(HEADER_SIZE)
        header.putInt(frames.size)

        // Strings ID & Name
        header.position(0x28)
        val idBytes = id.toByteArray(Charsets.US_ASCII).take(63).toByteArray()
        header.put(idBytes)
        header.put(0.toByte())

        header.position(0x68)
        val nameBytes = name.toByteArray(Charsets.US_ASCII).take(63).toByteArray()
        header.put(nameBytes)
        header.put(0.toByte())

        // Build resources
        var currentOffset = HEADER_SIZE
        val resourceTable = ByteArrayOutputStream()
        val payload = ByteArrayOutputStream()

        frames.forEach { rawFrame ->
            val composed = renderFrameWithClock(rawFrame, clockConfig)
            val encoded = encodeLvglImage(composed)

            val entry = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
            entry.putInt(0x02000001) // Type Image
            entry.putInt(0)          // Flags
            entry.putInt(currentOffset)
            entry.putInt(encoded.size)

            resourceTable.write(entry.array())
            payload.write(encoded)
            currentOffset += encoded.size
        }

        // Put resource table at 0x1C0
        val tableBytes = resourceTable.toByteArray()
        header.position(RESOURCE_TABLE_START)
        header.put(tableBytes)

        // Data size at 0x10
        header.position(0x10)
        header.putInt(payload.size())

        val fullOutput = ByteArrayOutputStream()
        fullOutput.write(header.array())
        fullOutput.write(payload.toByteArray())

        return fullOutput.toByteArray()
    }
}
