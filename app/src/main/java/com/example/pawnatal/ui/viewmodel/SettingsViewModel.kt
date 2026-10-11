package com.example.pawnatal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawnatal.data.model.AppSettings
import com.example.pawnatal.data.network.AiClient
import com.example.pawnatal.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConnectionTestState(
    val isTesting: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val aiClient: AiClient = AiClient()
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow

    private val _connectionTestState = MutableStateFlow(ConnectionTestState())
    val connectionTestState: StateFlow<ConnectionTestState> = _connectionTestState.asStateFlow()

    fun updateProvider(providerName: String) {
        settingsRepository.updateActiveProvider(providerName)
    }

    fun updateApiKey(providerName: String, key: String) {
        settingsRepository.updateApiKey(providerName, key)
    }

    fun updateCustomModel(modelId: String) {
        settingsRepository.updateCustomModel(modelId)
    }

    fun updateCustomBaseUrl(baseUrl: String) {
        settingsRepository.updateCustomBaseUrl(baseUrl)
    }

    fun toggleTempUnit() {
        settingsRepository.toggleTempUnit()
    }

    fun setTempUnit(unit: com.example.pawnatal.data.model.TempUnit) {
        settingsRepository.setTempUnit(unit)
    }

    fun toggleWeightUnit() {
        settingsRepository.toggleWeightUnit()
    }

    fun setWeightUnit(unit: com.example.pawnatal.data.model.WeightUnit) {
        settingsRepository.setWeightUnit(unit)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
    }

    fun testConnection() {
        viewModelScope.launch {
            _connectionTestState.value = ConnectionTestState(isTesting = true)
            val currentSettings = settings.value
            val result = aiClient.testConnection(currentSettings)
            result.onSuccess { message ->
                _connectionTestState.value = ConnectionTestState(
                    isTesting = false,
                    resultMessage = message,
                    isSuccess = true
                )
            }.onFailure { error ->
                _connectionTestState.value = ConnectionTestState(
                    isTesting = false,
                    resultMessage = "Connection failed: ${error.message}",
                    isSuccess = false
                )
            }
        }
    }

    fun clearTestMessage() {
        _connectionTestState.value = ConnectionTestState()
    }
}
