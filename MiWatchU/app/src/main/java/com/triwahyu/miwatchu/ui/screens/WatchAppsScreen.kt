package com.triwahyu.miwatchu.ui.screens

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
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.core.protocol.BleManager
import com.triwahyu.miwatchu.core.protocol.L1Packet
import com.triwahyu.miwatchu.ui.theme.*

@Composable
fun WatchAppsScreen() {
    var calcDisplay by remember { mutableStateOf("0") }
    var operand1 by remember { mutableDoubleStateOf(0.0) }
    var operator by remember { mutableStateOf<Char?>(null) }
    var clearOnNext by remember { mutableStateOf(false) }

    var actionStatus by remember { mutableStateOf("Kalkulator WatchU OS siap digunakan.") }

    fun onCalcKey(key: Char) {
        when {
            key in '0'..'9' -> {
                if (calcDisplay == "0" || clearOnNext) {
                    calcDisplay = key.toString()
                    clearOnNext = false
                } else if (calcDisplay.length < 10) {
                    calcDisplay += key
                }
            }
            key == '.' -> {
                if (!calcDisplay.contains('.')) calcDisplay += "."
            }
            key == 'C' -> {
                calcDisplay = "0"
                operand1 = 0.0
                operator = null
                clearOnNext = false
            }
            key in listOf('+', '-', '×', '÷') -> {
                operand1 = calcDisplay.toDoubleOrNull() ?: 0.0
                operator = key
                clearOnNext = true
            }
            key == '=' -> {
                val op2 = calcDisplay.toDoubleOrNull() ?: 0.0
                val res = when (operator) {
                    '+' -> operand1 + op2
                    '-' -> operand1 - op2
                    '×' -> operand1 * op2
                    '÷' -> if (op2 != 0.0) operand1 / op2 else Double.NaN
                    else -> op2
                }
                calcDisplay = if (res.isNaN()) "Error" else if (res == res.toLong().toDouble()) res.toLong().toString() else "%.4f".format(res).trimEnd('0').trimEnd('.')
                operator = null
                clearOnNext = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔢 WatchU OS: Kalkulator & Jam", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Tampilan kalkulator dan aplikasi mandiri di jam tangan", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Kalkulator Layar Jam
        Card(
            modifier = Modifier.width(260.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display bar kalkulator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFF14171F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(calcDisplay, color = NeonCyan, style = MaterialTheme.typography.headlineMedium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tombol Grid Kalkulator (4 Kolom)
                val buttons = listOf(
                    listOf('C', '÷', '×', '-'),
                    listOf('7', '8', '9', '+'),
                    listOf('4', '5', '6', '='),
                    listOf('1', '2', '3', '0')
                )

                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { k ->
                            val isAction = k in listOf('C', '÷', '×', '-', '+', '=')
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(
                                        if (k == '=') NeonCyan else if (isAction) Color(0xFF252A36) else Color(0xFF1A1D24),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onCalcKey(k) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    k.toString(),
                                    color = if (k == '=') CyberBlack else if (isAction) NeonAmber else Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(actionStatus, color = NeonGreen, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Kirim Hasil ke Jam
        Button(
            onClick = {
                // Kirim hasil hitungan kalkulator ke jam
                BleManager.sendNotification(
                    packageName = "com.triwahyu.miwatchu",
                    title = "🔢 Kalkulator Jam",
                    message = "Hasil: $calcDisplay",
                    isIncomingCall = false
                )
                actionStatus = "✅ Hasil hitungan [$calcDisplay] dikirim ke jam!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("📤 Kirim Hasil Hitungan ke Jam", color = CyberBlack)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tombol Getar Jam
        Button(
            onClick = {
                val vibratePayload = byteArrayOf(0x06, 0x01, 0x01)
                BleManager.sendL1Packet(L1Packet(L1Packet.TYPE_CMD, BleManager.getNextSeq(), vibratePayload))
                actionStatus = "⚡ Sinyal getar dikirim ke Redmi Watch 5 Active."
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("📳 Tes Getar Layar Jam (Haptic)", color = CyberBlack)
        }
    }
}
