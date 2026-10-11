package com.example.pawnatal.data.util

import com.example.pawnatal.data.model.AppSettings
import com.example.pawnatal.data.model.TempUnit
import com.example.pawnatal.data.model.WeightUnit
import java.util.Locale

object UnitFormatter {

    fun formatWeight(lbs: Double, settings: AppSettings): String {
        return formatWeight(lbs, settings.weightUnit)
    }

    fun formatWeight(lbs: Double, weightUnitStr: String): String {
        return if (weightUnitStr.equals(WeightUnit.KG.name, ignoreCase = true)) {
            val kg = lbs * 0.45359237
            String.format(Locale.US, "%.1f kg", kg)
        } else {
            String.format(Locale.US, "%.1f lbs", lbs)
        }
    }

    fun formatWeightDiff(lbsDiff: Double, weightUnitStr: String): String {
        val sign = if (lbsDiff >= 0) "+" else ""
        return if (weightUnitStr.equals(WeightUnit.KG.name, ignoreCase = true)) {
            val kg = lbsDiff * 0.45359237
            String.format(Locale.US, "%s%.1f kg", sign, kg)
        } else {
            String.format(Locale.US, "%s%.1f lbs", sign, lbsDiff)
        }
    }

    fun formatTemperature(tempFahrenheit: Double, settings: AppSettings): String {
        return formatTemperature(tempFahrenheit, settings.tempUnit)
    }

    fun formatTemperature(tempFahrenheit: Double, tempUnitStr: String): String {
        return if (tempUnitStr.equals(TempUnit.CELSIUS.name, ignoreCase = true)) {
            val c = (tempFahrenheit - 32.0) * 5.0 / 9.0
            String.format(Locale.US, "%.1f°C", c)
        } else {
            String.format(Locale.US, "%.1f°F", tempFahrenheit)
        }
    }

    fun fahrenheitToCelsius(f: Double): Double = (f - 32.0) * 5.0 / 9.0
    fun celsiusToFahrenheit(c: Double): Double = (c * 9.0 / 5.0) + 32.0

    fun getLaborDropThresholdText(tempUnitStr: String): String {
        return if (tempUnitStr.equals(TempUnit.CELSIUS.name, ignoreCase = true)) {
            "37.2°C"
        } else {
            "99.0°F"
        }
    }

    fun getSafeZoneText(tempUnitStr: String): String {
        return if (tempUnitStr.equals(TempUnit.CELSIUS.name, ignoreCase = true)) {
            "37.7°C–38.4°C"
        } else {
            "99.8°F–101.2°F"
        }
    }

    fun formatWeightGain(lbsDiff: Double, weightUnitStr: String): String = formatWeightDiff(lbsDiff, weightUnitStr)
    fun formatTempSafeZone(tempUnitStr: String): String = getSafeZoneText(tempUnitStr)
    fun formatTempDropThreshold(tempUnitStr: String): String = getLaborDropThresholdText(tempUnitStr)

    // Indian Date Formatters (dd/MM/yyyy, d MMMM yyyy, dd MMM yyyy)
    fun formatDateIndian(date: java.time.LocalDate): String = GestationCalculator.dtfIndian.format(date)
    fun formatDateLongIndian(date: java.time.LocalDate): String = GestationCalculator.dtfLong.format(date)
    fun formatDateMediumIndian(date: java.time.LocalDate): String = GestationCalculator.dtfMedium.format(date)
    fun formatDateShortIndian(date: java.time.LocalDate): String = GestationCalculator.dtfShort.format(date)
    fun formatDateTimeIndian(timestampMs: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.ENGLISH)
        return sdf.format(java.util.Date(timestampMs))
    }
    fun formatTimestampLabelIndian(timestampMs: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
        return sdf.format(java.util.Date(timestampMs))
    }
}
