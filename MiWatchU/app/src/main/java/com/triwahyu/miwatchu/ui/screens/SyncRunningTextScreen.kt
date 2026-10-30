package com.triwahyu.miwatchu.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.core.protocol.BleManager
import com.triwahyu.miwatchu.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SyncRunningTextScreen() {
    var textInput by remember { mutableStateOf("TRI WAHYU • WATCHU OS AKTIF!") }
    var speed by remember { mutableFloatStateOf(3f) }
    var applyStatus by remember { mutableStateOf("Ketik teks dan tekan tombol Apply di bawah.") }
    var isApplying by remember { mutableStateOf(false) }

    // Animasi preview teks berjalan di HP
    var scrollOffset by remember { mutableFloatStateOf(200f) }
    LaunchedEffect(textInput, speed) {
        while (true) {
            scrollOffset -= speed * 1.5f
            if (scrollOffset < -textInput.length * 20f) {
                scrollOffset = 220f
            }
            delay(30)
        }
    }

    val presets = listOf(
        "TRI WAHYU • WATCHU OS",
        "MOBILE LEGENDS MODE ON 🎮",
        "SEMANGAT KULIAHNYA! 🚀",
        "RUNNING TEXT REALTIME ⚡"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📢 Sync Running Text ke Jam", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Sekali klik 'Apply', teks di layar jam langsung berganti seketika!", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Mockup Layar Jam Tangan (Rasio 320x385)
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(130.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .border(2.dp, NeonGreen, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawContext.canvas.nativeCanvas.apply {
                    val paintText = android.graphics.Paint().apply {
                        color = android.graphics.Color.GREEN
                        textSize = 20f * (size.width / 220f)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    drawText(textInput, scrollOffset, size.height / 2f + 6f, paintText)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Preview Running Text pada Layar Jam", color = NeonGreen, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Input Teks
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Teks Berjalan yang Ingin Dikirim") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Cepat
        Text("Pilihan Teks Cepat:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.take(2).forEach { p ->
                SuggestionChip(
                    onClick = { textInput = p },
                    label = { Text(p.take(14) + "...", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.drop(2).forEach { p ->
                SuggestionChip(
                    onClick = { textInput = p },
                    label = { Text(p.take(14) + "...", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Slider Kecepatan
        Text("Kecepatan Teks: ${speed.toInt()}x", color = TextPrimary)
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = 1f..6f,
            steps = 4,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(applyStatus, color = NeonCyan, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))

        // Tombol APPLY
        Button(
            onClick = {
                isApplying = true
                applyStatus = "Mengirim running text ke jam via Bluetooth L1..."

                // 1. Kirim via protokol L1 custom
                BleManager.sendRunningTextDirect(textInput, speed.toInt())

                // 2. Kirim notifikasi sekunder untuk update instan
                BleManager.sendNotification(
                    packageName = "com.triwahyu.miwatchu",
                    title = "⚡ Running Text",
                    message = textInput,
                    isIncomingCall = false
                )

                applyStatus = "✅ Berhasil Di-apply! Jam sekarang menampilkan: \"$textInput\""
                isApplying = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("⚡ APPLY RUNNING TEXT KE JAM", color = CyberBlack, style = MaterialTheme.typography.titleMedium)
        }
    }
}
