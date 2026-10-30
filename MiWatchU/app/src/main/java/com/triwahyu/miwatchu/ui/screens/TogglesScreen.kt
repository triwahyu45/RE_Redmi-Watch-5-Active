package com.triwahyu.miwatchu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.feature.toggles.FeatureToggles
import com.triwahyu.miwatchu.ui.theme.*

@Composable
fun TogglesScreen() {
    val settings by FeatureToggles.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("⚙️ Kontrol Fitur & Sensor Bebas", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Pilih sensor yang ingin kamu nyalakan atau matikan untuk menghemat baterai & privasi", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Sensor Kesehatan
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sensor Biometrik & Kesehatan:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))

                ToggleItem(
                    title = "Detak Jantung Kontinu (24 Jam)",
                    subtitle = "Matikan untuk menghemat baterai jam drastis",
                    checked = settings.enableHeartRateContinuous,
                    onCheckedChange = { FeatureToggles.toggleHeartRate(it) }
                )

                HorizontalDivider(color = CyberBlack, modifier = Modifier.padding(vertical = 10.dp))

                ToggleItem(
                    title = "Oksigen Darah (SpO2) Otomatis",
                    subtitle = "Matikan jika tidak sedang berolahraga / tidur",
                    checked = settings.enableSpo2Continuous,
                    onCheckedChange = { FeatureToggles.toggleSpo2(it) }
                )

                HorizontalDivider(color = CyberBlack, modifier = Modifier.padding(vertical = 10.dp))

                ToggleItem(
                    title = "Pemantau Stres Otomatis",
                    subtitle = "Menonaktifkan komputasi HRV latar belakang",
                    checked = settings.enableStressContinuous,
                    onCheckedChange = { FeatureToggles.toggleStress(it) }
                )

                HorizontalDivider(color = CyberBlack, modifier = Modifier.padding(vertical = 10.dp))

                ToggleItem(
                    title = "Peringatan Terlalu Lama Duduk",
                    subtitle = "Matikan agar jam tidak bergetar mengganggu saat main game / kerja",
                    checked = settings.enableSedentaryReminder,
                    onCheckedChange = { FeatureToggles.toggleSedentary(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sistem, Privasi & Baterai
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Privasi, Layar & Baterai:", color = NeonCyan, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))

                ToggleItem(
                    title = "Nyalakan Layar Angkat Tangan (Lift to Wake)",
                    subtitle = "Layar menyala otomatis saat tangan diangkat",
                    checked = settings.enableLiftToWake,
                    onCheckedChange = { FeatureToggles.toggleLiftToWake(it) }
                )

                HorizontalDivider(color = CyberBlack, modifier = Modifier.padding(vertical = 10.dp))

                ToggleItem(
                    title = "Sinkronisasi Cuaca GPS",
                    subtitle = "Kirim update cuaca otomatis dari HP ke jam",
                    checked = settings.enableWeatherSync,
                    onCheckedChange = { FeatureToggles.toggleWeather(it) }
                )

                HorizontalDivider(color = CyberBlack, modifier = Modifier.padding(vertical = 10.dp))

                ToggleItem(
                    title = "Blokir Telemetri Cloud Xiaomi",
                    subtitle = "Lindungi privasi: Cegah data jam dikirim ke server Xiaomi",
                    checked = settings.enableCloudTelemetry,
                    onCheckedChange = { FeatureToggles.toggleCloudTelemetry(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("💡 Semua pengaturan tersimpan secara lokal tanpa perlu sinkronisasi ke akun Mi Cloud.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.35f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CyberBlack
            )
        )
    }
}
