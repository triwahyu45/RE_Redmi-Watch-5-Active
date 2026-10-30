package com.triwahyu.miwatchu.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.core.protocol.BleManager
import com.triwahyu.miwatchu.feature.watchface.ClockConfig
import com.triwahyu.miwatchu.feature.watchface.WatchfaceBinCompiler
import com.triwahyu.miwatchu.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WatchfaceStudioScreen() {
    val coroutineScope = rememberCoroutineScope()
    var clockX by remember { mutableStateOf(40f) }
    var clockY by remember { mutableStateOf(100f) }
    var showTime by remember { mutableStateOf(true) }
    var showSeconds by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(true) }
    var showBattery by remember { mutableStateOf(true) }

    val themes = listOf("Cyber Neon", "Titanium Luxury", "Emerald Matrix", "Minimalist OLED")
    var selectedTheme by remember { mutableStateOf("Cyber Neon") }

    val colors = listOf(
        "Cyan" to Color(0xFF00E5FF),
        "Gold" to Color(0xFFFFB800),
        "Emerald" to Color(0xFF00FF9D),
        "White" to Color(0xFFFFFFFF),
        "Violet" to Color(0xFFBD00FF)
    )
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val frameOptions = listOf(1 to "1 Frame (Instan ~15s)", 3 to "3 Frames (Animasi ~40s)", 8 to "8 Frames (Smooth ~90s)")
    var selectedFrameCount by remember { mutableIntStateOf(1) }

    var compileStatus by remember { mutableStateOf("Siap membuat watchface kustom.") }
    var isSending by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎨 Studio Watchface & Animasi", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Desain bebas posisi jam, wallpaper bergerak & teks di layar", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Preview Layar Jam (Rasio 320 x 385)
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(265.dp)
                .background(Color(0xFF0A0C10), RoundedCornerShape(20.dp))
                .border(2.dp, colors[selectedColorIndex].second, RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        clockX = (clockX + dragAmount.x * 1.45f).coerceIn(10f, 220f)
                        clockY = (clockY + dragAmount.y * 1.45f).coerceIn(40f, 350f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = size.width / WatchfaceBinCompiler.WATCH_WIDTH.toFloat()

                // Render Background Theme
                when (selectedTheme) {
                    "Cyber Neon" -> {
                        drawCircle(
                            color = colors[selectedColorIndex].second.copy(alpha = 0.25f),
                            radius = 75f * scale,
                            center = center
                        )
                    }
                    "Titanium Luxury" -> {
                        drawRoundRect(
                            color = Color(0xFFFFB800).copy(alpha = 0.3f),
                            size = androidx.compose.ui.geometry.Size(size.width - 24f, size.height - 24f),
                            topLeft = androidx.compose.ui.geometry.Offset(12f, 12f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                    "Emerald Matrix" -> {
                        drawLine(
                            color = Color(0xFF00FF9D).copy(alpha = 0.6f),
                            start = androidx.compose.ui.geometry.Offset(16f, size.height - 20f),
                            end = androidx.compose.ui.geometry.Offset(16f, size.height - 60f),
                            strokeWidth = 3f
                        )
                    }
                }

                // Render Live Clock Typography
                drawContext.canvas.nativeCanvas.apply {
                    val paintTime = android.graphics.Paint().apply {
                        val c = colors[selectedColorIndex].second
                        color = android.graphics.Color.rgb((c.red * 255).toInt(), (c.green * 255).toInt(), (c.blue * 255).toInt())
                        textSize = 44f * scale
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                    val paintSub = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 17f * scale
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }

                    var curY = clockY * scale
                    if (showTime) {
                        val t = if (showSeconds) "10:30:45" else "10:30"
                        drawText(t, clockX * scale, curY, paintTime)
                        curY += 22f * scale
                    }
                    if (showDate) {
                        drawText("JUMAT, 30 OKT", clockX * scale, curY, paintSub)
                        curY += 18f * scale
                    }
                    if (showBattery) {
                        drawText("🔋 98%", clockX * scale, curY, paintSub)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("👆 Sentuh & geser jam di kotak preview untuk memindahkan posisinya!", color = NeonAmber, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Pilihan Tema Visual
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Tema Desain:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    themes.forEach { theme ->
                        val isSelected = selectedTheme == theme
                        OutlinedButton(
                            onClick = { selectedTheme = theme },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else TextSecondary),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text(theme.split(" ")[0], style = MaterialTheme.typography.bodySmall, color = if (isSelected) NeonCyan else TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Warna Aksen Jam:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    colors.forEachIndexed { idx, item ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(item.second)
                                .border(if (selectedColorIndex == idx) 3.dp else 1.dp, Color.White, CircleShape)
                                .clickable { selectedColorIndex = idx }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Kontrol Elemen & Posisi
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Elemen Tampilan:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showTime, onCheckedChange = { showTime = it })
                    Text("Jam (HH:mm)", color = TextPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Checkbox(checked = showSeconds, onCheckedChange = { showSeconds = it })
                    Text("Detik (:ss)", color = TextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showDate, onCheckedChange = { showDate = it })
                    Text("Tanggal", color = TextPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Checkbox(checked = showBattery, onCheckedChange = { showBattery = it })
                    Text("Baterai (%)", color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Posisi Jam: X = ${clockX.toInt()} px, Y = ${clockY.toInt()} px", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(10.dp))
                Text("Mode Frame / Animasi:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                frameOptions.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { selectedFrameCount = opt.first }
                    ) {
                        RadioButton(selected = selectedFrameCount == opt.first, onClick = { selectedFrameCount = opt.first })
                        Text(opt.second, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSending) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp), color = NeonCyan, trackColor = CyberSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text("${(progress * 100).toInt()}% • $compileStatus", color = NeonCyan, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Text(compileStatus, color = NeonGreen, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (isSending) return@Button
                isSending = true
                compileStatus = "Mengompilasi frame tema $selectedTheme..."

                coroutineScope.launch {
                    val colorObj = colors[selectedColorIndex].second
                    val intColor = android.graphics.Color.rgb((colorObj.red * 255).toInt(), (colorObj.green * 255).toInt(), (colorObj.blue * 255).toInt())

                    val config = ClockConfig(
                        posX = clockX,
                        posY = clockY,
                        showTime = showTime,
                        showSeconds = showSeconds,
                        showDate = showDate,
                        showBattery = showBattery,
                        textColor = intColor
                    )

                    withContext(Dispatchers.Default) {
                        // 1. Generate frames sesuai tema
                        val baseFrames = WatchfaceBinCompiler.generateThemedFrames(selectedTheme, selectedFrameCount)

                        // 2. Compile ke format biner resmi 0x1234A55A
                        val binData = WatchfaceBinCompiler.compile(baseFrames, config)

                        withContext(Dispatchers.Main) {
                            compileStatus = "Mengirim biner ${binData.size / 1024} KB ke jam via BLE..."
                        }

                        // 3. Kirim via Bluetooth L1
                        BleManager.sendWatchfaceBinary(binData) { prog ->
                            progress = prog
                            compileStatus = "Mengirim payload: ${(prog * 100).toInt()}%"
                        }

                        withContext(Dispatchers.Main) {
                            compileStatus = "🎉 Selesai! Watchface kustom aktif pada Redmi Watch 5 Active."
                            isSending = false
                            progress = 0f
                        }
                    }
                }
            },
            enabled = !isSending,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (isSending) "⏳ Sedang Memasang..." else "🚀 KOMPILASI & PASANG KE JAM", color = CyberBlack, style = MaterialTheme.typography.titleSmall)
        }
    }
}
