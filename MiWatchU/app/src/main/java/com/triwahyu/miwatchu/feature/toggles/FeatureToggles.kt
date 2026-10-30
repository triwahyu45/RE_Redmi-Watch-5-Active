package com.triwahyu.miwatchu.feature.toggles

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class WatchFeatureSettings(
    val enableHeartRateContinuous: Boolean = false,
    val enableSleepTracking: Boolean = true,
    val enableWeatherSync: Boolean = false,
    val enableCloudTelemetry: Boolean = false,
    val screenTimeoutSeconds: Int = 15,
    val enableDndScheduled: Boolean = false
)

object FeatureToggles {
    private val _settings = MutableStateFlow(WatchFeatureSettings())
    val settings: StateFlow<WatchFeatureSettings> = _settings

    fun updateSettings(newSettings: WatchFeatureSettings) {
        _settings.value = newSettings
    }

    fun toggleHeartRate(enable: Boolean) {
        _settings.value = _settings.value.copy(enableHeartRateContinuous = enable)
    }

    fun toggleWeather(enable: Boolean) {
        _settings.value = _settings.value.copy(enableWeatherSync = enable)
    }

    fun toggleCloudTelemetry(enable: Boolean) {
        _settings.value = _settings.value.copy(enableCloudTelemetry = enable)
    }
}
