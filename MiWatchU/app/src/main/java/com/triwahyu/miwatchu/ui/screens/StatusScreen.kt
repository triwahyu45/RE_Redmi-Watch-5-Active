package com.triwahyu.miwatchu.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.core.protocol.BleManager
import com.triwahyu.miwatchu.service.WatchNotificationListenerService
import com.triwahyu.miwatchu.ui.theme.*

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    val statusMessage by BleManager.statusMessage.collectAsState()
    val targetMac by BleManager.targetMac.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ℹ️ Status & Kredensial Independen", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Koneksi langsung tanpa server Mi Fitness (Auth Token & SPP/BLE)", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Smartwatch Terdeteksi:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nama: Redmi Watch 5 Active", color = TextPrimary)
                Text("Model: ${BleManager.DEFAULT_MODEL} (SiFli SF32LB551)", color = TextSecondary)
                Text("Firmware: ${BleManager.FIRMWARE_VERSION} (Official Dumped)", color = TextSecondary)
                Text("Device ID (DID): ${BleManager.DEFAULT_DID}", color = TextSecondary)
                Text("MAC: $targetMac", color = TextSecondary)
                Text("Token: ${BleManager.AUTH_TOKEN.take(8)}... (Authentic)", color = NeonGreen)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CyberBlack)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Status: $statusMessage", color = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("WhatsApp & Call Forwarder:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val isRunning = WatchNotificationListenerService.isServiceRunning
                Text(
                    text = if (isRunning) "✅ Service Aktif (Meneruskan Pesan & Panggilan)" else "⚠️ Izin Akses Notifikasi Belum Diaktifkan",
                    color = if (isRunning) NeonGreen else NeonAmber
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlack)
                ) {
                    Text("Buka Pengaturan Izin Notifikasi", color = NeonCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                BleManager.connect(context)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔄 Sambungkan Bluetooth Langsung (SPP & BLE)", color = CyberBlack)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "💡 Tips: Jika jam belum merespon, 'Force Stop' Mi Fitness di Pengaturan HP agar koneksi Bluetooth tidak bentrok / dibajak.",
            color = NeonAmber,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
