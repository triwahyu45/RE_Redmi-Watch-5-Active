package com.triwahyu.miwatchu.feature.toggles

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class WatchFeatureSettings(
    val enableHeartRateContinuous: Boolean = false,
    val enableSpo2Continuous: Boolean = false,
    val enableStressContinuous: Boolean = false,
    val enableSleepTracking: Boolean = true,
    val enableSedentaryReminder: Boolean = false,
    val enableWeatherSync: Boolean = false,
    val enableCloudTelemetry: Boolean = false,
    val enableLiftToWake: Boolean = true,
    val screenTimeoutSeconds: Int = 15
)

object FeatureToggles {
    private val _settings = MutableStateFlow(WatchFeatureSettings())
    val settings: StateFlow<WatchFeatureSettings> = _settings

    fun toggleHeartRate(enable: Boolean) {
        _settings.value = _settings.value.copy(enableHeartRateContinuous = enable)
    }

    fun toggleSpo2(enable: Boolean) {
        _settings.value = _settings.value.copy(enableSpo2Continuous = enable)
    }

    fun toggleStress(enable: Boolean) {
        _settings.value = _settings.value.copy(enableStressContinuous = enable)
    }

    fun toggleSleep(enable: Boolean) {
        _settings.value = _settings.value.copy(enableSleepTracking = enable)
    }

    fun toggleSedentary(enable: Boolean) {
        _settings.value = _settings.value.copy(enableSedentaryReminder = enable)
    }

    fun toggleWeather(enable: Boolean) {
        _settings.value = _settings.value.copy(enableWeatherSync = enable)
    }

    fun toggleCloudTelemetry(enable: Boolean) {
        _settings.value = _settings.value.copy(enableCloudTelemetry = enable)
    }

    fun toggleLiftToWake(enable: Boolean) {
        _settings.value = _settings.value.copy(enableLiftToWake = enable)
    }

    fun setScreenTimeout(seconds: Int) {
        _settings.value = _settings.value.copy(screenTimeoutSeconds = seconds)
    }
}
