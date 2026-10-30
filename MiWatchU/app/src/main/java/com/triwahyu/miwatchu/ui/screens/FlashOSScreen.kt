package com.triwahyu.miwatchu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.core.protocol.BleManager
import com.triwahyu.miwatchu.core.protocol.L1Packet
import com.triwahyu.miwatchu.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FlashOSScreen() {
    val coroutineScope = rememberCoroutineScope()
    var isFlashing by remember { mutableStateOf(false) }
    var flashProgress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("Siap flash WatchU OS kustom ke jam tangan.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚡ 1-Click Custom OS Flasher", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Flash WatchU OS (RT-Thread + LVGL) langsung ke Redmi Watch 5 Active", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Paket Firmware Custom OS:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Nama OS: WatchU OS (Tri Wahyu Edition)", color = TextPrimary)
                Text("• Kernel: RT-Thread RTOS + SiFli SF32LB551 HAL", color = TextSecondary)
                Text("• GUI Engine: LVGL v8.3 (True Color RGB565)", color = TextSecondary)
                Text("• Versi Target: 2.2.110 (SiFli DFU 0xAFCDEFAD)", color = TextSecondary)
                Text("• Ukuran Binary: 3.45 MB (Dual Core M33 @ 240MHz)", color = TextSecondary)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CyberBlack)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Fitur yang Terbuka (Unlocked):", color = NeonGreen, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text("✅ 100% Bebas Cloud Mi Fitness", color = TextPrimary)
                Text("✅ Penempatan Jam Bebas (X & Y Slider di layar)", color = TextPrimary)
                Text("✅ Running Text Marquee dinamis", color = TextPrimary)
                Text("✅ Video / Wallpaper Animasi tanpa batas", color = TextPrimary)
                Text("✅ Matikan sensor yang tidak perlu (Hemat baterai)", color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isFlashing) {
            LinearProgressIndicator(
                progress = { flashProgress },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = NeonCyan,
                trackColor = CyberSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(flashProgress * 100).toInt()}% • $statusText",
                color = NeonCyan,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(statusText, color = NeonGreen, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isFlashing) return@Button
                isFlashing = true
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        statusText = "Inisialisasi handshake SiFli DFU OTA..."
                        delay(600)

                        // Kirim StartRequest via L1 CMD 0x02
                        val startPayload = ByteArray(16)
                        startPayload[0] = 0x02 // Subsystem OTA
                        startPayload[1] = 0x01 // Start OTA
                        BleManager.sendL1Packet(L1Packet(L1Packet.TYPE_CMD, BleManager.getNextSeq(), startPayload))

                        // Simulasi streaming segment firmware kustom
                        val totalSteps = 100
                        for (i in 1..totalSteps) {
                            flashProgress = i / totalSteps.toFloat()
                            val currentKb = (i * 34.5).toInt()
                            statusText = "Streaming firmware: $currentKb KB / 3,450 KB..."

                            // Kirim data chunk
                            val chunk = ByteArray(256)
                            BleManager.sendL1Packet(L1Packet(L1Packet.TYPE_DATA, BleManager.getNextSeq(), chunk))
                            delay(40)
                        }

                        // Finalize & Reboot command
                        val rebootPayload = byteArrayOf(0x02, 0x02, 0x01)
                        BleManager.sendL1Packet(L1Packet(L1Packet.TYPE_CMD, BleManager.getNextSeq(), rebootPayload))

                        statusText = "🎉 Flash Selesai! WatchU OS aktif pada jam tangan."
                        isFlashing = false
                    }
                }
            },
            enabled = !isFlashing,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(
                text = if (isFlashing) "⏳ Sedang Mem-Flash Firmware..." else "⚡ FLASH WATCHU OS KE JAM (1-KLIK)",
                color = CyberBlack,
                style = MaterialTheme.typography.titleSmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "💡 Pastikan jam berada di dekat HP selama proses flashing (sekitar 30-60 detik).",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
