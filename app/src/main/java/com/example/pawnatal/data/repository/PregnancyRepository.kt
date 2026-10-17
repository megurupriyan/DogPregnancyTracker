package com.example.pawnatal.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.pawnatal.data.model.*
import com.example.pawnatal.data.notification.ReminderNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PregnancyRepository(
    context: Context,
    private val notificationManager: ReminderNotificationManager = ReminderNotificationManager(context)
) {
    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("pawnatal_pregnancy_data", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _damProfileFlow = MutableStateFlow(loadDamProfile())
    val damProfileFlow: StateFlow<DamProfile> = _damProfileFlow.asStateFlow()

    private val _carePlanFlow = MutableStateFlow(loadCarePlan())
    val carePlanFlow: StateFlow<ObstetricCarePlan> = _carePlanFlow.asStateFlow()

    private val _tempHistoryFlow = MutableStateFlow(loadTempHistory())
    val tempHistoryFlow: StateFlow<List<TemperatureRecord>> = _tempHistoryFlow.asStateFlow()

    init {
        // Automatically reconcile timeline on creation
        refreshForToday()
    }

    private fun resolveAnchorMatingDate(): String {
        // 1. Try earliest manual temperature record timestamp
        try {
            val tempSerialized = prefs.getString("temp_history_json", null)
            if (!tempSerialized.isNullOrBlank()) {
                val list = json.decodeFromString<List<TemperatureRecord>>(tempSerialized)
                val earliest = list.minByOrNull { it.timestamp }
                if (earliest != null && earliest.timestamp > 0) {
                    val recordDate = java.time.Instant.ofEpochMilli(earliest.timestamp)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    // Started at Day 2 on first entry, meaning mating was 1 day prior
                    return recordDate.minusDays(1).toString()
                }
            }
        } catch (_: Exception) {}

        // 2. Try app installation date
        try {
            val installTime = appContext.packageManager.getPackageInfo(appContext.packageName, 0).firstInstallTime
            if (installTime > 0) {
                val installDate = java.time.Instant.ofEpochMilli(installTime)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                return installDate.minusDays(1).toString()
            }
        } catch (_: Exception) {}

        // 3. Fallback to yesterday
        return java.time.LocalDate.now().minusDays(1).toString()
    }

    private fun loadDamProfile(): DamProfile {
        val serialized = prefs.getString("dam_profile_json", null)
        val decoded = if (!serialized.isNullOrBlank()) {
            try {
                json.decodeFromString<DamProfile>(serialized)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val profile = if (decoded != null) {
            if (decoded.firstMatingDate.isBlank()) {
                val anchor = resolveAnchorMatingDate()
                decoded.copy(
                    firstMatingDate = anchor,
                    secondTieDate = if (decoded.secondTieDate.isBlank()) {
                        java.time.LocalDate.parse(anchor).plusDays(2).toString()
                    } else decoded.secondTieDate
                )
            } else {
                decoded
            }
        } else {
            val anchor = resolveAnchorMatingDate()
            DamProfile(
                name = "Bella",
                breed = "Golden Retriever",
                sizeClassification = "Large Breed • 25–32 kg",
                firstMatingDate = anchor,
                secondTieDate = java.time.LocalDate.parse(anchor).plusDays(2).toString(),
                ageYears = 3,
                parity = "Primiparous (1st Litter)",
                baselineWeightLbs = 60.5,
                currentWeightLbs = 60.5
            )
        }

        // Persist immediately with encodeDefaults = true so firstMatingDate is never lost!
        prefs.edit().putString("dam_profile_json", json.encodeToString(profile)).apply()
        return profile
    }

    private fun loadCarePlan(): ObstetricCarePlan {
        val serialized = prefs.getString("care_plan_json", null)
        val initialPlan = if (!serialized.isNullOrBlank()) {
            try {
                json.decodeFromString<ObstetricCarePlan>(serialized)
            } catch (e: Exception) {
                val def = DefaultClinicalPlanProvider.createDefaultPlan(_damProfileFlow.value)
                prefs.edit().putString("care_plan_json", json.encodeToString(def)).apply()
                def
            }
        } else {
            val def = DefaultClinicalPlanProvider.createDefaultPlan(_damProfileFlow.value)
            prefs.edit().putString("care_plan_json", json.encodeToString(def)).apply()
            def
        }
        return com.example.pawnatal.data.util.GestationCalculator.reconcileCarePlan(initialPlan, _damProfileFlow.value)
    }

    private fun loadTempHistory(): List<TemperatureRecord> {
        val serialized = prefs.getString("temp_history_json", null)
        return if (!serialized.isNullOrBlank()) {
            try {
                json.decodeFromString<List<TemperatureRecord>>(serialized)
            } catch (e: Exception) {
                DefaultClinicalPlanProvider.getInitialTemperatureHistory()
            }
        } else {
            DefaultClinicalPlanProvider.getInitialTemperatureHistory()
        }
    }

    fun saveDamProfile(dam: DamProfile) {
        prefs.edit().putString("dam_profile_json", json.encodeToString(dam)).apply()
        _damProfileFlow.value = dam
        // Automatically reconcile the care plan's timeline and vet info with new mating date / profile
        val reconciled = com.example.pawnatal.data.util.GestationCalculator.reconcileCarePlan(_carePlanFlow.value, dam)
        saveCarePlan(reconciled)
    }

    fun saveCarePlan(plan: ObstetricCarePlan) {
        prefs.edit().putString("care_plan_json", json.encodeToString(plan)).apply()
        _carePlanFlow.value = plan
    }

    fun toggleWhelpingKitItem(itemId: String) {
        val currentPlan = _carePlanFlow.value
        val updatedKit = currentPlan.whelpingKit.map { item ->
            if (item.id == itemId) item.copy(isReady = !item.isReady) else item
        }
        val updatedPlan = currentPlan.copy(whelpingKit = updatedKit)
        saveCarePlan(updatedPlan)
    }

    fun toggleTodayRoutine(routineId: String) {
        val currentPlan = _carePlanFlow.value
        val updatedRoutines = currentPlan.todayRoutines.map { routine ->
            if (routine.id == routineId) routine.copy(isDone = !routine.isDone) else routine
        }
        val updatedPlan = currentPlan.copy(todayRoutines = updatedRoutines)
        saveCarePlan(updatedPlan)
    }

    fun toggleMedication(medId: String) {
        val currentPlan = _carePlanFlow.value
        val updatedMeds = currentPlan.medications.map { med ->
            if (med.id == medId) {
                val newCompleted = !med.isCompleted
                med.copy(
                    isCompleted = newCompleted,
                    dueStatus = if (newCompleted) "Done" else "Pending"
                )
            } else med
        }
        val updatedPlan = currentPlan.copy(medications = updatedMeds)
        saveCarePlan(updatedPlan)
    }

    fun recordTemperature(tempFahrenheit: Double, notes: String = "") {
        val now = System.currentTimeMillis()
        val label = com.example.pawnatal.data.util.UnitFormatter.formatTimestampLabelIndian(now)
        val newRecord = TemperatureRecord(
            id = "t_${System.currentTimeMillis()}",
            timestamp = now,
            displayLabel = "$label (Latest)",
            tempFahrenheit = tempFahrenheit,
            notes = notes
        )

        val updatedList = _tempHistoryFlow.value + newRecord
        prefs.edit().putString("temp_history_json", json.encodeToString(updatedList)).apply()
        _tempHistoryFlow.value = updatedList

        // Update Today's Routine temperature status if logged
        val currentPlan = _carePlanFlow.value
        val updatedRoutines = currentPlan.todayRoutines.map { routine ->
            if (routine.type == "TEMPERATURE") routine.copy(isDone = true, subtitle = "Logged: ${com.example.pawnatal.data.util.UnitFormatter.formatTemperature(tempFahrenheit, com.example.pawnatal.data.model.TempUnit.CELSIUS.name)}") else routine
        }
        saveCarePlan(currentPlan.copy(todayRoutines = updatedRoutines))

        // Check for Pre-Labor Hypothermia drop alert (< 99.0°F / 37.2°C)
        if (newRecord.isHypothermiaDrop) {
            notificationManager.sendHypothermiaAlert(_damProfileFlow.value.name, tempFahrenheit)
        }
    }

    /**
     * Called on date change or app resume to ensure all metrics, currentDay,
     * gestational week, and daily routines react dynamically to the new day.
     */
    fun refreshForToday() {
        val today = java.time.LocalDate.now()
        val currentDam = _damProfileFlow.value
        val currentPlan = _carePlanFlow.value
        val reconciled = com.example.pawnatal.data.util.GestationCalculator.reconcileCarePlan(currentPlan, currentDam, asOfDate = today)

        val lastReconciledDate = prefs.getString("last_reconciled_date", null)
        val todayStr = today.toString()

        if (lastReconciledDate != null && lastReconciledDate != todayStr) {
            // Calendar rollover: reset daily routine completions for the new day
            val refreshedRoutines = reconciled.todayRoutines.map { routine ->
                routine.copy(isDone = false)
            }
            val refreshedPlan = reconciled.copy(todayRoutines = refreshedRoutines)
            prefs.edit().putString("last_reconciled_date", todayStr).apply()
            saveCarePlan(refreshedPlan)
        } else {
            prefs.edit().putString("last_reconciled_date", todayStr).apply()
            saveCarePlan(reconciled)
        }
    }

    fun loadPreset(presetName: String) {
        val today = java.time.LocalDate.now()
        val newDam = when (presetName) {
            "FRENCH_BULLDOG" -> DamProfile(
                name = "Luna",
                breed = "French Bulldog",
                sizeClassification = "Small Breed • 9–13 kg",
                firstMatingDate = today.minusDays(14).toString(),
                secondTieDate = today.minusDays(12).toString(),
                ageYears = 2,
                parity = "Primiparous (1st Litter)",
                baselineWeightLbs = 24.0,
                currentWeightLbs = 25.5,
                vetDoctorName = "",
                vetClinicName = "",
                vetClinicPhone = "",
                vetClinicAddress = "",
                emergencyPhone = ""
            )
            "LABRADOR" -> DamProfile(
                name = "Daisy",
                breed = "Labrador Retriever",
                sizeClassification = "Large Breed • 25–32 kg",
                firstMatingDate = today.minusDays(28).toString(),
                secondTieDate = today.minusDays(26).toString(),
                ageYears = 4,
                parity = "Multiparous (2nd Litter)",
                baselineWeightLbs = 62.0,
                currentWeightLbs = 66.0,
                vetDoctorName = "",
                vetClinicName = "",
                vetClinicPhone = "",
                vetClinicAddress = "",
                emergencyPhone = ""
            )
            else -> DamProfile()
        }
        saveDamProfile(newDam)
        val defaultPlan = DefaultClinicalPlanProvider.createDefaultPlan(newDam)
        saveCarePlan(defaultPlan)
    }
}
