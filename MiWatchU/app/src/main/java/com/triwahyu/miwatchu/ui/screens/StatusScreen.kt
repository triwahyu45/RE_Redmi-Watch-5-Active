package com.triwahyu.miwatchu.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("ℹ️ Status & Kredensial Independen", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Koneksi langsung tanpa server Mi Fitness (Auth Token & SPP/BLE L1)", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Smartwatch Terdeteksi:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Perangkat: Redmi Watch 5 Active", color = TextPrimary)
                Text("• Model: ${BleManager.DEFAULT_MODEL} (SiFli SF32LB551)", color = TextSecondary)
                Text("• Firmware Dump: ${BleManager.FIRMWARE_VERSION} (Official Unpacked)", color = TextSecondary)
                Text("• Device ID (DID): ${BleManager.DEFAULT_DID}", color = TextSecondary)
                Text("• MAC Address: $targetMac", color = TextSecondary)
                Text("• Auth Key: ${BleManager.AUTH_TOKEN.take(8)}... (Authentic Token)", color = NeonGreen)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CyberBlack)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Status Koneksi: $statusMessage", color = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("WhatsApp & Call Forwarder:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val isRunning = WatchNotificationListenerService.isServiceRunning
                Text(
                    text = if (isRunning) "✅ Service Aktif (Meneruskan Pesan & Panggilan WA)" else "⚠️ Izin Akses Notifikasi Belum Diaktifkan",
                    color = if (isRunning) NeonGreen else NeonAmber
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlack),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Text("Buka Pengaturan Izin Notifikasi HP", color = NeonCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { BleManager.connect(context) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("🔄 Sambungkan", color = CyberBlack)
            }

            Button(
                onClick = {
                    BleManager.sendNotification(
                        packageName = "com.whatsapp",
                        title = "Mi WatchU Test",
                        message = "Tes koneksi berhasil! Jam terhubung ke Mi WatchU.",
                        isIncomingCall = false
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("🔔 Tes Notifikasi", color = CyberBlack)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "💡 Tips: Jika jam belum merespon, 'Force Stop' Mi Fitness di Pengaturan HP agar koneksi Bluetooth tidak dibajak di latar belakang.",
            color = NeonAmber,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
