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
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("⚙️ Kontrol Fitur Bebas", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Aktifkan atau matikan fitur jam sesuai keinginan tanpa paksaan cloud", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sensor & Pemantauan:", color = NeonAmber, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Detak Jantung Kontinu", color = TextPrimary)
                        Text("Matikan untuk menghemat baterai jam drastis", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = settings.enableHeartRateContinuous,
                        onCheckedChange = { FeatureToggles.toggleHeartRate(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                HorizontalDivider(color = CyberBlack, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sinkronisasi Cuaca", color = TextPrimary)
                        Text("Kirim data cuaca GPS ke jam secara berkala", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = settings.enableWeatherSync,
                        onCheckedChange = { FeatureToggles.toggleWeather(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                HorizontalDivider(color = CyberBlack, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cloud Telemetry Xiaomi", color = TextPrimary)
                        Text("Blokir pengiriman data jam ke server cloud Xiaomi", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = settings.enableCloudTelemetry,
                        onCheckedChange = { FeatureToggles.toggleCloudTelemetry(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
            }
        }
    }
}
