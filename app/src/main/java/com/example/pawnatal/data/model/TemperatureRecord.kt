package com.example.pawnatal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TemperatureRecord(
    val id: String,
    val timestamp: Long,
    val displayLabel: String, // e.g. "Apr 17 PM (Latest)"
    val tempFahrenheit: Double,
    val notes: String = ""
) {
    val tempCelsius: Double
        get() = (tempFahrenheit - 32.0) * 5.0 / 9.0

    val isHypothermiaDrop: Boolean
        get() = tempFahrenheit < 99.0

    fun formattedValue(unit: TempUnit): String {
        return if (unit == TempUnit.CELSIUS) {
            String.format("%.1f°C", tempCelsius)
        } else {
            String.format("%.1f°F", tempFahrenheit)
        }
    }
}
