package com.triwahyu.miwatchu.ui.screens

import android.bluetooth.BluetoothProfile
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.core.protocol.BleManager
import com.triwahyu.miwatchu.ui.theme.*

@Composable
fun PairingScreen() {
    val context = LocalContext.current
    val connectionState by BleManager.connectionState.collectAsState()
    val statusMessage by BleManager.statusMessage.collectAsState()
    val targetMac by BleManager.targetMac.collectAsState()
    val isScanning by BleManager.isScanning.collectAsState()
    val discoveredDevices by BleManager.discoveredDevices.collectAsState()

    val isConnected = connectionState == BluetoothProfile.STATE_CONNECTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔗 Mode Pairing Jam", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Pasangkan Redmi Watch 5 Active langsung ke aplikasi WatchU", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status Jam:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
                    Badge(
                        containerColor = if (isConnected) NeonGreen else Color.Red
                    ) {
                        Text(if (isConnected) "TERHUBUNG" else "TERPUTUS", color = Color.Black, modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("• Perangkat Target: Redmi Watch 5 Active", color = TextPrimary)
                Text("• MAC Address: $targetMac", color = TextSecondary)
                Text("• Jalur: Xiaomi BLE L1 & SPP Transport", color = TextSecondary)

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CyberBlack)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Status Log: $statusMessage", color = NeonCyan, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Hubungkan Cepat
        Button(
            onClick = {
                BleManager.connect(context)
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (isConnected) NeonGreen else NeonCyan),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(if (isConnected) Icons.Default.CheckCircle else Icons.Default.Refresh, contentDescription = null, tint = CyberBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isConnected) "✅ Jam Sudah Terhubung" else "⚡ SAMBUNGKAN KE JAM SEKARANG", color = CyberBlack, style = MaterialTheme.typography.titleSmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tombol Pindai Perangkat Baru
        OutlinedButton(
            onClick = {
                BleManager.startBleScan(context)
            },
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber)
        ) {
            Text(if (isScanning) "⏳ Sedang Memindai Jam..." else "🔍 PINDAI BLUETOOTH (MODE PAIRING / QR)", color = NeonAmber)
        }

        if (isScanning) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = NeonAmber, trackColor = CyberSurface)
        }

        // Daftar Perangkat Ditemukan
        if (discoveredDevices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Perangkat yang Ditemukan:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            discoveredDevices.forEach { dev ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { BleManager.pairDevice(context, dev) },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(dev.name ?: "Redmi Watch 5 Active", color = TextPrimary)
                            Text(dev.address, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { BleManager.pairDevice(context, dev) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Pair", color = CyberBlack)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "💡 Info: Karena Mi Fitness sudah di-uninstall, aplikasi Mi WatchU akan langsung mengambil alih koneksi Bluetooth ke jam secara mandiri.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
