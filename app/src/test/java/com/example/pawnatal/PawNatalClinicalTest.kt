package com.example.pawnatal

import com.example.pawnatal.data.model.*
import com.example.pawnatal.data.network.AiPromptBuilder
import com.example.pawnatal.data.repository.DefaultClinicalPlanProvider
import org.junit.Assert.*
import org.junit.Test

class PawNatalClinicalTest {

    @Test
    fun defaultPlan_hasCompleteCanineObstetricTimeline() {
        val today = java.time.LocalDate.now()
        val matingDate42DaysAgo = today.minusDays(41).toString()
        val dam = DamProfile(
            name = "Bella",
            breed = "Golden Retriever",
            firstMatingDate = matingDate42DaysAgo,
            baselineWeightLbs = 60.5,
            currentWeightLbs = 64.2
        )
        val plan = DefaultClinicalPlanProvider.createDefaultPlan(dam)

        // Verify Gestation standard 63 days and dynamic calculations
        assertEquals(63, plan.insights.gestationSpanDays)
        assertEquals(42, plan.insights.currentDay)
        assertEquals(6, plan.insights.currentWeek)
        assertEquals(2, plan.insights.currentTrimester)

        // Verify all 9 gestation weeks exist
        assertEquals(9, plan.weeklyGuides.size)
        assertEquals(1, plan.weeklyGuides.first().weekNumber)
        assertEquals(9, plan.weeklyGuides.last().weekNumber)

        // Verify whelping kit has 14 essential items
        assertEquals(14, plan.whelpingKit.size)
        assertTrue(plan.whelpingKit.any { it.name.contains("thermometer", ignoreCase = true) })
        assertTrue(plan.whelpingKit.any { it.name.contains("scissors", ignoreCase = true) })
        assertTrue(plan.whelpingKit.any { it.name.contains("aspirator", ignoreCase = true) })

        // Verify emergency hotline & dial triggers
        assertNotNull(plan.emergencyInfo.hotlinePhone)
        assertTrue(plan.emergencyInfo.immediateDialTriggers.isNotEmpty())
    }

    @Test
    fun unitFormatter_formatsUnitsCorrectly() {
        // Temperature
        assertEquals("100.4°F", com.example.pawnatal.data.util.UnitFormatter.formatTemperature(100.4, "FAHRENHEIT"))
        assertEquals("38.0°C", com.example.pawnatal.data.util.UnitFormatter.formatTemperature(100.4, "CELSIUS"))

        // Weight
        assertEquals("60.5 lbs", com.example.pawnatal.data.util.UnitFormatter.formatWeight(60.5, "LBS"))
        assertEquals("27.4 kg", com.example.pawnatal.data.util.UnitFormatter.formatWeight(60.5, "KG"))
        assertEquals("+3.7 lbs", com.example.pawnatal.data.util.UnitFormatter.formatWeightGain(3.7, "LBS"))
        assertEquals("+1.7 kg", com.example.pawnatal.data.util.UnitFormatter.formatWeightGain(3.7, "KG"))
    }

    @Test
    fun temperatureRecord_detectsHypothermiaDropCorrectly() {
        val normalReading = TemperatureRecord("t1", System.currentTimeMillis(), "Apr 17 PM", 100.4)
        assertFalse(normalReading.isHypothermiaDrop)
        assertEquals("100.4°F", normalReading.formattedValue(TempUnit.FAHRENHEIT))

        val dropReading = TemperatureRecord("t2", System.currentTimeMillis(), "Apr 18 AM", 98.6)
        assertTrue("Temperatures below 99.0°F must be flagged as hypothermia drop", dropReading.isHypothermiaDrop)
        assertTrue(dropReading.formattedValue(TempUnit.CELSIUS).contains("37.0°C"))
    }

    @Test
    fun aiPromptBuilder_containsDamProfileDetailsAndSchema() {
        val dam = DamProfile(
            name = "Bella",
            breed = "Golden Retriever",
            firstMatingDate = "2025-03-01",
            baselineWeightLbs = 60.5
        )
        val prompt = AiPromptBuilder.buildUserPrompt(dam)

        assertTrue(prompt.contains("Bella"))
        assertTrue(prompt.contains("Golden Retriever"))
        assertTrue(prompt.contains("2025-03-01"))
        assertTrue(prompt.contains("60.5"))
        assertTrue(prompt.contains("JSON Schema"))
        assertTrue(prompt.contains("gestationSpanDays"))
        assertTrue(prompt.contains("whelpingKit"))
    }

    @Test
    fun appSettings_resolvesCorrectApiKeyAndModel() {
        var settings = AppSettings(
            activeProvider = AiProvider.OPENAI.name,
            openAiApiKey = "sk-test-openai",
            geminiApiKey = "test-gemini",
            customModelId = "sol"
        )
        assertEquals("sk-test-openai", settings.getActiveApiKey())
        assertEquals("sol", settings.activeModelId)

        settings = settings.copy(activeProvider = AiProvider.GEMINI.name)
        assertEquals("test-gemini", settings.getActiveApiKey())
        assertEquals("sol", settings.activeModelId)
    }
}
