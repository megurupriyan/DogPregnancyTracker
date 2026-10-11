package com.example.pawnatal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DamProfile(
    val name: String = "Bella",
    val breed: String = "Golden Retriever",
    val sizeClassification: String = "Large Breed • 25–32 kg",
    val firstMatingDate: String = java.time.LocalDate.now().minusDays(1).toString(), // Default to Day 2
    val secondTieDate: String = java.time.LocalDate.now().plusDays(1).toString(),   // Optional +48h
    val ageYears: Int = 3,
    val parity: String = "Primiparous (1st Litter)",
    val baselineWeightLbs: Double = 60.5,
    val currentWeightLbs: Double = 60.5,
    val vetDoctorName: String = "",
    val vetClinicName: String = "",
    val vetClinicPhone: String = "",
    val vetClinicAddress: String = "",
    val emergencyPhone: String = ""
) {
    val weightGainLbs: Double
        get() = currentWeightLbs - baselineWeightLbs
}
