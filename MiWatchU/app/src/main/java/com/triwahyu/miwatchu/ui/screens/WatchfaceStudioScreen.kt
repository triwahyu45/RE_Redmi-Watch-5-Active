package com.triwahyu.miwatchu.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    var compileStatus by remember { mutableStateOf("Siap kompilasi watchface video/animasi.") }
    var progress by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎨 Studio Watchface Video", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Atur posisi jam dan elemen visual secara bebas", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Preview Layar Jam (Rasio 320 x 385)
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(265.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        clockX = (clockX + dragAmount.x * 1.5f).coerceIn(10f, 220f)
                        clockY = (clockY + dragAmount.y * 1.5f).coerceIn(40f, 350f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = size.width / WatchfaceBinCompiler.WATCH_WIDTH.toFloat()
                drawContext.canvas.nativeCanvas.apply {
                    val paintTime = android.graphics.Paint().apply {
                        color = android.graphics.Color.CYAN
                        textSize = 42f * scale
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    val paintSub = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 18f * scale
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }

                    var curY = clockY * scale
                    if (showTime) {
                        val t = if (showSeconds) "10:30:45" else "10:30"
                        drawText(t, clockX * scale, curY, paintTime)
                        curY += 24f * scale
                    }
                    if (showDate) {
                        drawText("JUMAT, 30 OKT", clockX * scale, curY, paintSub)
                        curY += 20f * scale
                    }
                    if (showBattery) {
                        drawText("🔋 98%", clockX * scale, curY, paintSub)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("💡 Sentuh & geser layar jam di atas untuk mengatur posisi jam bebas!", color = NeonAmber, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Kontrol Slider & Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Komponen yang Ditampilkan:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showTime, onCheckedChange = { showTime = it })
                    Text("Tampilkan Jam (HH:mm)")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = showSeconds, onCheckedChange = { showSeconds = it })
                    Text("Detik (:ss)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showDate, onCheckedChange = { showDate = it })
                    Text("Tampilkan Tanggal")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = showBattery, onCheckedChange = { showBattery = it })
                    Text("Baterai (%)")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Posisi X: ${clockX.toInt()} px | Posisi Y: ${clockY.toInt()} px", color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (progress > 0f) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = NeonGreen)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(compileStatus, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    compileStatus = "Mengompilasi frame binary LVGL..."
                    val config = ClockConfig(
                        posX = clockX,
                        posY = clockY,
                        showTime = showTime,
                        showSeconds = showSeconds,
                        showDate = showDate,
                        showBattery = showBattery
                    )

                    withContext(Dispatchers.Default) {
                        // Buat sampel 15 frame video kustom
                        val frames = (1..15).map {
                            Bitmap.createBitmap(WatchfaceBinCompiler.WATCH_WIDTH, WatchfaceBinCompiler.WATCH_HEIGHT, Bitmap.Config.ARGB_8888)
                        }
                        val binData = WatchfaceBinCompiler.compile(frames, config)

                        withContext(Dispatchers.Main) {
                            compileStatus = "Mengirim ${binData.size} bytes ke jam via BLE..."
                        }

                        BleManager.sendWatchfaceBinary(binData) { prog ->
                            progress = prog
                        }

                        withContext(Dispatchers.Main) {
                            compileStatus = "✅ Berhasil dipasang ke Redmi Watch 5 Active!"
                            progress = 0f
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚀 Kompilasi & Pasang ke Jam", color = Color.Black)
        }
    }
}
