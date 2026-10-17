package com.example.pawnatal.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pawnatal.data.model.DamProfile
import com.example.pawnatal.data.model.ObstetricCarePlan
import com.example.pawnatal.data.util.GestationCalculator
import kotlinx.serialization.json.Json
import java.time.LocalDate

class PregnancyAlarmReceiver : BroadcastReceiver() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val notificationManager = ReminderNotificationManager(context)

        // Load profile and plan directly from SharedPreferences
        val prefs = context.getSharedPreferences("pawnatal_pregnancy_data", Context.MODE_PRIVATE)
        val damProfile = try {
            val serialized = prefs.getString("dam_profile_json", null)
            if (!serialized.isNullOrBlank()) json.decodeFromString<DamProfile>(serialized) else DamProfile()
        } catch (e: Exception) {
            DamProfile()
        }

        val carePlan = try {
            val serialized = prefs.getString("care_plan_json", null)
            if (!serialized.isNullOrBlank()) json.decodeFromString<ObstetricCarePlan>(serialized) else null
        } catch (e: Exception) {
            null
        }

        val insights = GestationCalculator.calculateInsights(
            matingDateStr = damProfile.firstMatingDate,
            breed = damProfile.breed,
            existingInsights = carePlan?.insights,
            asOfDate = LocalDate.now()
        )

        when (action) {
            AlarmScheduler.ACTION_MORNING_REMINDER -> {
                val currentDay = insights.currentDay
                val milestone = carePlan?.milestones?.firstOrNull { !it.isCompleted }?.title ?: "Care & Nutrition"
                val pendingMeds = carePlan?.medications?.count { !it.isCompleted } ?: 0

                notificationManager.sendMorningRoutineReminder(
                    damName = damProfile.name,
                    dayOfGestation = currentDay,
                    milestoneTitle = milestone,
                    pendingMedsCount = pendingMeds
                )
                // Reschedule for next day
                AlarmScheduler.scheduleDailyReminders(context)
            }

            AlarmScheduler.ACTION_EVENING_REMINDER -> {
                val currentDay = insights.currentDay
                val isLateGestation = currentDay >= 55

                notificationManager.sendEveningTempReminder(
                    damName = damProfile.name,
                    dayOfGestation = currentDay,
                    isLateGestation = isLateGestation
                )
                // Reschedule for next day
                AlarmScheduler.scheduleDailyReminders(context)
            }

            AlarmScheduler.ACTION_TEST_REMINDER -> {
                notificationManager.sendTestReminder()
            }
        }
    }
}
