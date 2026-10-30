package com.triwahyu.miwatchu.feature.games

import com.triwahyu.miwatchu.core.protocol.BleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

object ReactionGame {
    private val _gameState = MutableStateFlow("Siap Main? Tekan Mulai!")
    val gameState: StateFlow<String> = _gameState

    private val _lastScore = MutableStateFlow<Long?>(null)
    val lastScore: StateFlow<Long?> = _lastScore

    private var triggerTimestamp: Long = 0
    private var gameJob: Job? = null

    fun startRound() {
        gameJob?.cancel()
        _gameState.value = "Tunggu sinyal getar di jam..."
        BleManager.sendNotification(
            packageName = "com.triwahyu.miwatchu.game",
            title = "🎮 GAME REFLEKS",
            message = "FOKUS! Tunggu getaran..."
        )

        gameJob = CoroutineScope(Dispatchers.Default).launch {
            // Delay acak antara 2 s/d 5 detik
            val delayMs = Random.nextLong(2000, 5000)
            delay(delayMs)

            triggerTimestamp = System.currentTimeMillis()
            _gameState.value = "⚡ TEKAN SEKARANG!"
            // Kirim getaran kencang ke jam
            BleManager.sendNotification(
                packageName = "com.triwahyu.miwatchu.game",
                title = "⚡ TEKAN SEKARANG!",
                message = "Sentuh jam sekarang!",
                isIncomingCall = true // Memanfaatkan haptic alert
            )
        }
    }

    fun onUserReacted() {
        if (triggerTimestamp > 0) {
            val elapsed = System.currentTimeMillis() - triggerTimestamp
            _lastScore.value = elapsed
            _gameState.value = "Hebat! Waktu reaksi: ${elapsed} ms"
            triggerTimestamp = 0

            BleManager.sendNotification(
                packageName = "com.triwahyu.miwatchu.game",
                title = "🏆 SKOR KAMU",
                message = "${elapsed} ms! Mantap!"
            )
        }
    }
}
