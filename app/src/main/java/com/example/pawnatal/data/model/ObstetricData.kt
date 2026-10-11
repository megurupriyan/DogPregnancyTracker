package com.example.pawnatal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ObstetricCarePlan(
    val insights: ObstetricInsights = ObstetricInsights(),
    val breedNutritionNote: String = "",
    val milestones: List<ScheduledMilestone> = emptyList(),
    val todayRoutines: List<DailyRoutineItem> = emptyList(),
    val veterinaryVisits: List<VetAppointment> = emptyList(),
    val medications: List<MedicationItem> = emptyList(),
    val weeklyGuides: List<WeeklyGuideDetail> = emptyList(),
    val whelpingKit: List<WhelpingKitItem> = emptyList(),
    val emergencyInfo: EmergencyVetInfo = EmergencyVetInfo()
)

@Serializable
data class ObstetricInsights(
    val gestationSpanDays: Int = 63,
    val gestationRangeText: String = "Range: 61–65 days",
    val estimatedWhelpDate: String = "May 3, 2025",
    val currentDay: Int = 42,
    val currentWeek: Int = 6,
    val currentTrimester: Int = 2,
    val percentComplete: Int = 67,
    val daysRemaining: Int = 21,
    val estimatedLitterSize: String = "6 – 8 Pups",
    val litterSubtitle: String = "Golden primiparous avg.",
    val trimesterDescription: String = "Trimester II: Critical Ossification & Skeletal Hardening",
    val tieDateDisplay: String = "Mar 1",
    val todayDisplay: String = "Day 42",
    val dueDisplay: String = "May 3"
)

@Serializable
data class ScheduledMilestone(
    val id: String = "",
    val title: String = "",
    val dayWindow: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val iconType: String = "radiology" // radiology, thermostat, check, restaurant, etc.
)

@Serializable
data class DailyRoutineItem(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val timeOfDay: String = "",
    val isDone: Boolean = false,
    val type: String = "MEDICATION" // MEDICATION, TEMPERATURE, EXERCISE, NUTRITION
)

@Serializable
data class VetAppointment(
    val id: String = "",
    val title: String = "",
    val scheduledDateText: String = "", // e.g. "Sunday, April 20th • 10:30 AM"
    val isoDate: String = "2025-04-20",
    val status: String = "UPCOMING", // COMPLETED, UPCOMING, PENDING
    val doctorName: String = "",
    val clinicName: String = "",
    val address: String = "",
    val notes: String = "",
    val daysUntil: Int = 3
)

@Serializable
data class MedicationItem(
    val id: String = "",
    val name: String = "",
    val dosageInstruction: String = "",
    val timing: String = "",
    val category: String = "SUPPLEMENT", // SUPPLEMENT, DEWORMER, RX
    val isCompleted: Boolean = false,
    val dueStatus: String = "Done" // Done, Pending, Due Tomorrow
)

@Serializable
data class WeeklyGuideDetail(
    val weekNumber: Int = 1,
    val dayRangeText: String = "", // "Days 36-42"
    val trimesterText: String = "", // "Trimester 2 • Critical Ossification"
    val title: String = "", // "Rapid Growth & Skeleton Hardening"
    val summary: String = "",
    val daysToDelivery: Int = 0,
    val puppySizeComparison: String = "", // "Size of a Kiwi Fruit (~75 mm, ~45 grams)"
    val puppyWeightGrams: String = "", // "~45 grams"
    val fetalHeartRateBpm: String = "", // "200 – 220 BPM"
    val anatomicalMilestones: List<AnatomicalFeature> = emptyList(),
    val damStatus: DamWeeklyStatus = DamWeeklyStatus(),
    val clinicalActions: List<ClinicalActionItem> = emptyList(),
    val historyHighlights: String = ""
)

@Serializable
data class AnatomicalFeature(
    val title: String = "",
    val description: String = "",
    val icon: String = "pets"
)

@Serializable
data class DamWeeklyStatus(
    val physicalChanges: String = "",
    val calorieDemandNote: String = "",
    val behavioralNote: String = ""
)

@Serializable
data class ClinicalActionItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isChecked: Boolean = false
)

@Serializable
data class WhelpingKitItem(
    val id: String = "",
    val name: String = "",
    val purpose: String = "",
    val isReady: Boolean = false,
    val category: String = "SUPPLY" // MEDICAL, WARMING, SANITATION, NURSING
)

@Serializable
data class EmergencyVetInfo(
    val clinicName: String = "Veterinary Emergency Clinic",
    val hotlinePhone: String = "",
    val distanceText: String = "Primary Veterinary Contact",
    val onDutyDoctor: String = "Attending Veterinarian",
    val immediateDialTriggers: List<String> = emptyList(),
    val mythBusterTitle: String = "Calcium Supplementation: When & Why?",
    val mythBusterContent: String = "Never supplement calcium pre-labor."
)
