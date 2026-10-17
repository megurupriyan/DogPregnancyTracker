package com.example.pawnatal

import com.example.pawnatal.data.model.*
import com.example.pawnatal.data.network.AiPromptBuilder
import com.example.pawnatal.data.repository.DefaultClinicalPlanProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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

    @Test
    fun damProfile_serializationRetainsMatingDateWithEncodeDefaults() {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        val dam = DamProfile(
            name = "Manikutty",
            breed = "Pomeranian",
            firstMatingDate = "2026-10-10",
            baselineWeightLbs = 14.5
        )

        val serialized = json.encodeToString(dam)
        assertTrue("Serialized JSON must contain firstMatingDate even if it's default/empty", serialized.contains("\"firstMatingDate\":\"2026-10-10\""))

        val deserialized = json.decodeFromString<DamProfile>(serialized)
        assertEquals("2026-10-10", deserialized.firstMatingDate)
        assertEquals("Manikutty", deserialized.name)
    }

    @Test
    fun gestationTimeline_advancesAccuratelyOverDaysAndWeeks() {
        val fixedToday = java.time.LocalDate.of(2026, 10, 17)

        // Day 1: Mating occurred today
        val damDay1 = DamProfile(name = "Test", breed = "Pomeranian", firstMatingDate = "2026-10-17")
        val planDay1 = DefaultClinicalPlanProvider.createDefaultPlan(damDay1, asOfDate = fixedToday)
        assertEquals(1, planDay1.insights.currentDay)
        assertEquals(1, planDay1.insights.currentWeek)
        assertEquals(1, planDay1.insights.currentTrimester)
        assertEquals(62, planDay1.insights.daysRemaining)

        // Day 8: Mating occurred 7 days ago (2026-10-10) -> currentDay = 7 + 1 = 8
        val damDay8 = DamProfile(name = "Manikutty", breed = "Pomeranian", firstMatingDate = "2026-10-10")
        val planDay8 = DefaultClinicalPlanProvider.createDefaultPlan(damDay8, asOfDate = fixedToday)
        assertEquals(8, planDay8.insights.currentDay)
        assertEquals(2, planDay8.insights.currentWeek)
        assertEquals(1, planDay8.insights.currentTrimester)
        assertEquals(55, planDay8.insights.daysRemaining)

        // Day 42: Mating occurred 41 days ago (2026-09-06) -> currentDay = 41 + 1 = 42
        val damDay42 = DamProfile(name = "Test", breed = "Pomeranian", firstMatingDate = "2026-09-06")
        val planDay42 = DefaultClinicalPlanProvider.createDefaultPlan(damDay42, asOfDate = fixedToday)
        assertEquals(42, planDay42.insights.currentDay)
        assertEquals(6, planDay42.insights.currentWeek)
        assertEquals(2, planDay42.insights.currentTrimester)
        assertEquals(21, planDay42.insights.daysRemaining)

        // Day 63: Delivery Day
        val damDay63 = DamProfile(name = "Test", breed = "Pomeranian", firstMatingDate = "2026-08-16")
        val planDay63 = DefaultClinicalPlanProvider.createDefaultPlan(damDay63, asOfDate = fixedToday)
        assertEquals(63, planDay63.insights.currentDay)
        assertEquals(9, planDay63.insights.currentWeek)
        assertEquals(3, planDay63.insights.currentTrimester)
        assertEquals(0, planDay63.insights.daysRemaining)
    }

    @Test
    fun milestones_markCompletedDynamicallyBasedOnCurrentDay() {
        val fixedToday = java.time.LocalDate.of(2026, 10, 17)
        // Day 32 (Week 5)
        val damDay32 = DamProfile(name = "Test", breed = "Pomeranian", firstMatingDate = "2026-09-16")
        val plan = DefaultClinicalPlanProvider.createDefaultPlan(damDay32, asOfDate = fixedToday)

        // Ultrasound Window (m1 - Day 28) should be completed on Day 32
        val ultrasound = plan.milestones.find { it.id == "m1" }
        assertNotNull(ultrasound)
        assertTrue("Ultrasound (Day 28) should be completed on Day 32", ultrasound!!.isCompleted)

        // Radiograph / X-Ray (m2 - Day 52) should NOT be completed on Day 32
        val xray = plan.milestones.find { it.id == "m2" }
        assertNotNull(xray)
        assertFalse("X-Ray (Day 52) should not be completed on Day 32", xray!!.isCompleted)
    }

    @Test
    fun gestationCalculator_fallbackOnBlankMatingDate() {
        val fixedToday = java.time.LocalDate.of(2026, 10, 17)
        val damBlank = DamProfile(name = "New Dam", breed = "Pomeranian", firstMatingDate = "")
        val plan = DefaultClinicalPlanProvider.createDefaultPlan(damBlank, asOfDate = fixedToday)

        // Defaults to yesterday so currentDay is 2
        assertEquals(2, plan.insights.currentDay)
        assertEquals(1, plan.insights.currentWeek)
        assertNotNull(plan.insights.estimatedWhelpDate)
    }
}
