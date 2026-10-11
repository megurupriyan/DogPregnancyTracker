package com.example.pawnatal.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.pawnatal.data.model.AppSettings
import com.example.pawnatal.data.model.TempUnit
import com.example.pawnatal.data.model.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pawnatal_app_settings", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val serialized = prefs.getString("settings_json", null)
        val defaultSettings = AppSettings(
            tempUnit = TempUnit.CELSIUS.name,
            weightUnit = WeightUnit.KG.name
        )
        val loaded = if (!serialized.isNullOrBlank()) {
            try {
                json.decodeFromString<AppSettings>(serialized)
            } catch (e: Exception) {
                defaultSettings
            }
        } else {
            defaultSettings
        }

        // Migrate to Indian metric standards if not yet migrated
        val hasMigratedMetric = prefs.getBoolean("migrated_to_metric_v2", false)
        return if (!hasMigratedMetric) {
            val migrated = loaded.copy(
                tempUnit = TempUnit.CELSIUS.name,
                weightUnit = WeightUnit.KG.name
            )
            prefs.edit()
                .putString("settings_json", json.encodeToString(migrated))
                .putBoolean("migrated_to_metric_v2", true)
                .apply()
            migrated
        } else {
            loaded
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit()
            .putString("settings_json", json.encodeToString(newSettings))
            .apply()
        _settingsFlow.value = newSettings
    }

    fun updateActiveProvider(providerName: String) {
        val current = _settingsFlow.value
        updateSettings(current.copy(activeProvider = providerName))
    }

    fun updateApiKey(providerName: String, key: String) {
        val current = _settingsFlow.value
        val updated = when (providerName) {
            "OPENAI" -> current.copy(openAiApiKey = key)
            "GEMINI" -> current.copy(geminiApiKey = key)
            "GROQ" -> current.copy(groqApiKey = key)
            "OPENROUTER" -> current.copy(openRouterApiKey = key)
            "CUSTOM" -> current.copy(customApiKey = key)
            else -> current.copy(openAiApiKey = key)
        }
        updateSettings(updated)
    }

    fun updateCustomModel(modelId: String) {
        val current = _settingsFlow.value
        updateSettings(current.copy(customModelId = modelId))
    }

    fun updateCustomBaseUrl(baseUrl: String) {
        val current = _settingsFlow.value
        updateSettings(current.copy(customBaseUrl = baseUrl))
    }

    fun toggleTempUnit() {
        val current = _settingsFlow.value
        val newUnit = if (current.tempUnit == TempUnit.FAHRENHEIT.name) TempUnit.CELSIUS.name else TempUnit.FAHRENHEIT.name
        updateSettings(current.copy(tempUnit = newUnit))
    }

    fun setTempUnit(unit: TempUnit) {
        val current = _settingsFlow.value
        updateSettings(current.copy(tempUnit = unit.name))
    }

    fun toggleWeightUnit() {
        val current = _settingsFlow.value
        val newUnit = if (current.weightUnit == WeightUnit.LBS.name) WeightUnit.KG.name else WeightUnit.LBS.name
        updateSettings(current.copy(weightUnit = newUnit))
    }

    fun setWeightUnit(unit: WeightUnit) {
        val current = _settingsFlow.value
        updateSettings(current.copy(weightUnit = unit.name))
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        val current = _settingsFlow.value
        updateSettings(current.copy(notificationsEnabled = enabled))
    }
}
