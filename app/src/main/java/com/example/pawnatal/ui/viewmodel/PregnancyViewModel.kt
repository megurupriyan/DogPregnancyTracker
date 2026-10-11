package com.example.pawnatal.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawnatal.data.calendar.CalendarHelper
import com.example.pawnatal.data.model.*
import com.example.pawnatal.data.network.AiClient
import com.example.pawnatal.data.repository.DefaultClinicalPlanProvider
import com.example.pawnatal.data.repository.PregnancyRepository
import com.example.pawnatal.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SynthesisUiState(
    val isGenerating: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class PregnancyViewModel(
    private val pregnancyRepository: PregnancyRepository,
    private val settingsRepository: SettingsRepository,
    private val aiClient: AiClient = AiClient()
) : ViewModel() {

    val damProfile: StateFlow<DamProfile> = pregnancyRepository.damProfileFlow
    val carePlan: StateFlow<ObstetricCarePlan> = pregnancyRepository.carePlanFlow
    val tempHistory: StateFlow<List<TemperatureRecord>> = pregnancyRepository.tempHistoryFlow
    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow

    private val _synthesisState = MutableStateFlow(SynthesisUiState())
    val synthesisState: StateFlow<SynthesisUiState> = _synthesisState.asStateFlow()

    fun updateDamProfile(updated: DamProfile) {
        pregnancyRepository.saveDamProfile(updated)
    }

    fun updateVetDetails(
        doctorName: String,
        clinicName: String,
        phone: String,
        address: String,
        emergencyPhone: String
    ) {
        val currentDam = damProfile.value
        val updated = currentDam.copy(
            vetDoctorName = doctorName.trim(),
            vetClinicName = clinicName.trim(),
            vetClinicPhone = phone.trim(),
            vetClinicAddress = address.trim(),
            emergencyPhone = emergencyPhone.trim()
        )
        pregnancyRepository.saveDamProfile(updated)
    }

    fun updateMaternalWeight(newWeightLbs: Double) {
        val currentDam = damProfile.value
        val updated = currentDam.copy(
            currentWeightLbs = (Math.round(newWeightLbs * 10.0) / 10.0)
        )
        pregnancyRepository.saveDamProfile(updated)
    }

    fun synthesizeCarePlan() {
        viewModelScope.launch {
            _synthesisState.value = SynthesisUiState(isGenerating = true)
            val currentDam = damProfile.value
            val currentSettings = settings.value

            val result = aiClient.synthesizeCarePlan(currentDam, currentSettings)

            result.onSuccess { newPlan ->
                val reconciledPlan = com.example.pawnatal.data.util.GestationCalculator.reconcileCarePlan(newPlan, currentDam)
                pregnancyRepository.saveCarePlan(reconciledPlan)
                _synthesisState.value = SynthesisUiState(
                    isGenerating = false,
                    successMessage = "Care plan synthesized successfully using ${currentSettings.activeModelId}!"
                )
            }.onFailure { error ->
                // If AI call failed (e.g. no key or offline), generate calculated clinical fallback
                val calculatedPlan = DefaultClinicalPlanProvider.createDefaultPlan(currentDam)
                pregnancyRepository.saveCarePlan(calculatedPlan)
                _synthesisState.value = SynthesisUiState(
                    isGenerating = false,
                    errorMessage = "Notice: ${error.message ?: "Could not reach AI model"}. Loaded canine veterinary clinical engine plan."
                )
            }
        }
    }

    fun clearSynthesisMessages() {
        _synthesisState.value = SynthesisUiState()
    }

    fun toggleWhelpingKitItem(id: String) {
        pregnancyRepository.toggleWhelpingKitItem(id)
    }

    fun toggleTodayRoutine(id: String) {
        pregnancyRepository.toggleTodayRoutine(id)
    }

    fun toggleMedication(id: String) {
        pregnancyRepository.toggleMedication(id)
    }

    fun recordTemperature(tempFahrenheit: Double, notes: String = "") {
        pregnancyRepository.recordTemperature(tempFahrenheit, notes)
    }

    fun addAppointmentToCalendar(context: Context, appointment: VetAppointment) {
        CalendarHelper.addVetAppointmentToCalendar(context, appointment, damProfile.value.name)
    }

    fun addMilestoneToCalendar(context: Context, milestone: ScheduledMilestone) {
        CalendarHelper.addMilestoneToCalendar(
            context,
            milestone,
            damProfile.value.name,
            carePlan.value.insights.estimatedWhelpDate
        )
    }

    fun loadPreset(presetName: String) {
        pregnancyRepository.loadPreset(presetName)
    }

    fun refreshForToday() {
        pregnancyRepository.refreshForToday()
    }
}
