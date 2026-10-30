package com.triwahyu.miwatchu.feature.marquee

import com.triwahyu.miwatchu.core.protocol.BleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object MarqueeController {
    private var streamingJob: Job? = null

    fun startStreaming(
        text: String,
        title: String = "📢 RUNNING TEXT",
        speedMs: Long = 1200L,
        windowSize: Int = 18,
        onUpdate: (String) -> Unit
    ) {
        stopStreaming()
        streamingJob = CoroutineScope(Dispatchers.Default).launch {
            val padded = " ".repeat(windowSize) + text + " ".repeat(windowSize)
            val totalSteps = padded.length - windowSize + 1

            while (isActive) {
                for (i in 0 until totalSteps) {
                    if (!isActive) break
                    val chunk = padded.substring(i, i + windowSize)
                    BleManager.sendNotification(
                        packageName = "com.triwahyu.miwatchu",
                        title = title,
                        message = chunk
                    )
                    onUpdate(chunk)
                    delay(speedMs)
                }
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
    }
}
