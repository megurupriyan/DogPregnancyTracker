---
name: dog-pregnancy-tracker
description: Orchestrates development, clinical AI prompt generation, testing, calendar sync, and veterinary data management for the PawNatal Native Android Dog Pregnancy & Gestation Tracker app.
---

# PawNatal Canine Pregnancy Tracker AI Harness Skill

This skill guides AI agents and developers in maintaining, testing, enhancing, and interacting with the **PawNatal** canine pregnancy tracking application.

## Project Overview

PawNatal is a native Android application built with **Jetpack Compose**, **Material 3**, and an **Obstetric AI Engine**. It guides dog owners and canine breeders from the first mating tie through delivery (standard 63 days) and newborn whelping.

### Core Architecture
- **Language & UI**: Kotlin 2.3+, Jetpack Compose with Material 3 design system tokens calibrated from Google Stitch designs.
- **Entry Point**: `SetupAndAiPlanScreen` (Pregnancy Setup & AI Care Plan).
- **Architecture Pattern**: Clean Architecture (MVI/MVVM: UI -> ViewModel -> Repository -> Network/DataStore).
- **AI Harness**: Multi-provider support (OpenAI `sol` / `gpt-4o-mini`, Google Gemini `gemini-2.5-flash`, Groq `llama-3.3-70b`, OpenRouter, and custom endpoints) with structured JSON generation and clinical offline fallback.
- **Calendar Integration**: `CalendarHelper` adds obstetric milestones and veterinary appointments to Google Calendar and native Android device calendars.
- **Pre-Labor Alert System**: Twice-daily basal rectal temperature monitoring with dynamic canvas trend curves and instant hypothermia drop alerts (< 99.0°F / 37.2°C) indicating labor within 12–24 hours.

---

## Directory Structure

```
DogPregnancyTracker/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/pawnatal/
│   │   │   │   ├── MainActivity.kt               # Entry Activity hosting PawNatalAppScreen
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── DamProfile.kt         # Dam mating, parity, weight vitals
│   │   │   │   │   │   ├── ObstetricData.kt      # Milestones, weekly guides, care plan
│   │   │   │   │   │   ├── AiSettings.kt         # AI providers, API keys, units
│   │   │   │   │   │   └── TemperatureRecord.kt  # Basal rectal readings & thresholds
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── AiClient.kt           # Multi-provider LLM network client
│   │   │   │   │   │   └── AiPromptBuilder.kt    # Canine clinical prompt generator
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── PregnancyRepository.kt
│   │   │   │   │   │   ├── SettingsRepository.kt
│   │   │   │   │   │   └── DefaultClinicalPlanProvider.kt
│   │   │   │   │   ├── calendar/
│   │   │   │   │   │   └── CalendarHelper.kt     # Google Calendar intent integration
│   │   │   │   │   └── notification/
│   │   │   │   │       └── ReminderNotificationManager.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt                  # Stitch color palette tokens
│   │   │   │   │   ├── Theme.kt                  # Material 3 light/dark schemes
│   │   │   │   │   └── Type.kt                   # Typography tokens
│   │   │   │   └── ui/
│   │   │   │       ├── PawNatalAppScreen.kt      # Main host with bottom navigation
│   │   │   │       ├── components/
│   │   │   │       │   ├── PawTopAppBar.kt
│   │   │   │       │   ├── PawBottomNavBar.kt
│   │   │   │       │   ├── TempChartCanvas.kt   # Smooth Bezier trend canvas with 99°F line
│   │   │   │       │   └── RecordTempDialog.kt
│   │   │   │       ├── setup/
│   │   │   │       │   └── SetupAndAiPlanScreen.kt  # Required entry point screen
│   │   │   │       ├── home/
│   │   │   │       │   └── HomeDashboardScreen.kt
│   │   │   │       ├── weekly/
│   │   │   │       │   └── WeeklyGuideScreen.kt    # 9-week gestation timeline
│   │   │   │       ├── schedule/
│   │   │   │       │   └── ScheduleAndMedicalScreen.kt
│   │   │   │       ├── tips/
│   │   │   │       │   └── TipsAndWhelpingScreen.kt  # Emergency hotline & 14-item kit
│   │   │   │       ├── settings/
│   │   │   │       │   └── SettingsScreen.kt       # AI model selector & API keys
│   │   │   │       └── viewmodel/
│   │   │   │           ├── PregnancyViewModel.kt
│   │   │   │           └── SettingsViewModel.kt
│   │   │   └── AndroidManifest.xml
│   │   └── test/java/com/example/pawnatal/
│   │       └── PawNatalClinicalTest.kt
├── design_reference/                             # Extracted Google Stitch screens & HTML
├── AI_HARNESS.md                                 # Full AI harness specification
├── CHANGELOG.md                                  # Version release notes
└── README.md                                     # Setup & user documentation
```

---

## AI Harness & Output Schema

The AI Harness converts a dam's profile into an `ObstetricCarePlan` JSON object.

### Target JSON Schema
When invoking the model (`sol`, `gpt-4o-mini`, `gemini-2.5-flash`, etc.), the prompt requires:
```json
{
  "insights": {
    "gestationSpanDays": 63,
    "gestationRangeText": "Range: 61–65 days",
    "estimatedWhelpDate": "String",
    "currentDay": 42,
    "currentWeek": 6,
    "currentTrimester": 2,
    "percentComplete": 67,
    "daysRemaining": 21,
    "estimatedLitterSize": "String",
    "litterSubtitle": "String",
    "trimesterDescription": "String",
    "tieDateDisplay": "String",
    "todayDisplay": "String",
    "dueDisplay": "String"
  },
  "breedNutritionNote": "String",
  "milestones": [
    { "id": "String", "title": "String", "dayWindow": "String", "description": "String", "isCompleted": false, "iconType": "radiology" }
  ],
  "todayRoutines": [
    { "id": "String", "title": "String", "subtitle": "String", "timeOfDay": "String", "isDone": false, "type": "MEDICATION" }
  ],
  "veterinaryVisits": [
    { "id": "String", "title": "String", "scheduledDateText": "String", "isoDate": "YYYY-MM-DD", "status": "UPCOMING", "doctorName": "String", "clinicName": "String", "address": "String", "notes": "String", "daysUntil": 3 }
  ],
  "medications": [
    { "id": "String", "name": "String", "dosageInstruction": "String", "timing": "String", "category": "SUPPLEMENT", "isCompleted": false, "dueStatus": "Done" }
  ],
  "weeklyGuides": [
    {
      "weekNumber": 1,
      "dayRangeText": "Days 1-7",
      "trimesterText": "Trimester 1",
      "title": "String",
      "summary": "String",
      "daysToDelivery": 56,
      "puppySizeComparison": "String",
      "puppyWeightGrams": "String",
      "fetalHeartRateBpm": "String",
      "anatomicalMilestones": [{ "title": "String", "description": "String", "icon": "pets" }],
      "damStatus": { "physicalChanges": "String", "calorieDemandNote": "String", "behavioralNote": "String" },
      "clinicalActions": [{ "id": "String", "title": "String", "description": "String", "isChecked": false }],
      "historyHighlights": "String"
    }
  ],
  "whelpingKit": [
    { "id": "String", "name": "String", "purpose": "String", "isReady": false, "category": "MEDICAL" }
  ],
  "emergencyInfo": {
    "clinicName": "String",
    "hotlinePhone": "String",
    "distanceText": "String",
    "onDutyDoctor": "String",
    "immediateDialTriggers": ["String"],
    "mythBusterTitle": "String",
    "mythBusterContent": "String"
  }
}
```

---

## Development & CLI Runbook

### Build Debug APK
```bash
./gradlew assembleDebug
```
Output APK: `app/build/outputs/apk/debug/app-debug.apk`

### Execute Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Install & Run on Connected Device / Emulator
```bash
android run app/build/outputs/apk/debug/app-debug.apk
# Or with adb directly:
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.pawnatal/.MainActivity
```

### Capture Screenshot
```bash
android screenshot screenshot_output.png
```

### Inspect UI Hierarchy
```bash
android layout
```
