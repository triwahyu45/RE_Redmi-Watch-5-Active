package com.triwahyu.miwatchu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.feature.marquee.MarqueeController
import com.triwahyu.miwatchu.ui.theme.*

@Composable
fun MarqueeScreen() {
    var textInput by remember { mutableStateOf("Halo Tri Wahyu! Mi WatchU Running Text Aktif!") }
    var speedMs by remember { mutableFloatStateOf(1000f) }
    var isStreaming by remember { mutableStateOf(false) }
    var currentPreview by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📢 Live Running Text", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Streaming teks berjalan real-time ke layar jam tangan", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Teks Berjalan") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Kecepatan Scroll: ${speedMs.toInt()} ms", color = TextPrimary)
        Slider(
            value = speedMs,
            onValueChange = { speedMs = it },
            valueRange = 400f..2500f,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Monitor Tampilan Jam:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (currentPreview.isNotBlank()) "[$currentPreview]" else "[Standby]",
                    style = MaterialTheme.typography.headlineSmall,
                    color = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isStreaming) {
                    MarqueeController.stopStreaming()
                    isStreaming = false
                } else {
                    MarqueeController.startStreaming(
                        text = textInput,
                        speedMs = speedMs.toLong()
                    ) { chunk ->
                        currentPreview = chunk
                    }
                    isStreaming = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (isStreaming) Color.Red else NeonGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isStreaming) "⏹ Hentikan Running Text" else "▶ Mulai Streaming ke Jam", color = Color.Black)
        }
    }
}
