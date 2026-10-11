# Changelog

All notable changes to the **PawNatal** Dog Pregnancy & Gestation Tracker will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.3.0] - 2026-10-11

### Fixed & Enhanced
- **AI Plan Screen Layout & Typography Redesign**:
  - Replaced crowded 3-column stats card with a prominent full-width **Hero Due Date Banner** and a balanced **50% / 50% 2-column grid** for Gestation Span and Litter Estimate, preventing date mid-string splitting (e.g., `2026-12-1\n2`).
  - Refactored veterinary phone number inputs into full-width vertical fields, allowing complete international phone numbers (e.g., `+919496811669`) without multi-line wrapping.
  - Implemented interactive expandable milestone cards with 2-line previews and `Read details ▼` / `Show less ▲` toggles, compressing runaway milestone card heights (~75dp).
  - Added expandable Breed Nutrition guidance card with smooth expansion toggle.
  - Scaled down typography scale across headers and labels for a crisp, high-density veterinary UI.

---

## [1.2.0] - 2026-10-11

### Added & Enhanced
- **Official PawNatal Adaptive App Icon & Launcher**:
  - Replaced legacy default Android green robot launcher icons across all density buckets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`).
  - Extracted official Stitch brand mascot logo asset and integrated with modern adaptive icon vector definitions (`ic_launcher.xml` and `ic_launcher_round.xml`).
  - Configured high-resolution notification small icon badge (`ic_paw_notification`).
- **Reliable Background Reminders & Alarms (When Killed or Backgrounded)**:
  - Engineered standalone [`AlarmScheduler`](file:///Users/net/Desktop/Works/DogPregnancyTracker/app/src/main/java/com/example/pawnatal/data/notification/AlarmScheduler.kt) leveraging Android `AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP)` with `Intent.FLAG_RECEIVER_FOREGROUND` to bypass background execution limits.
  - Implemented [`PregnancyAlarmReceiver`](file:///Users/net/Desktop/Works/DogPregnancyTracker/app/src/main/java/com/example/pawnatal/data/notification/PregnancyAlarmReceiver.kt) that dynamically wakes up the broadcast receiver, loads dam profile from storage, recalculates today's exact gestation day, and posts rich notifications.
  - Implemented [`BootReceiver`](file:///Users/net/Desktop/Works/DogPregnancyTracker/app/src/main/java/com/example/pawnatal/data/notification/BootReceiver.kt) listening to `ACTION_BOOT_COMPLETED` and `ACTION_MY_PACKAGE_REPLACED` to guarantee notifications persist across device restarts.
  - Handled Android 13+ (API 33+) `POST_NOTIFICATIONS` runtime permission with in-app request flow and test notification trigger in Settings screen.
- **Dynamic Daily Date Change Responsiveness**:
  - Implemented [`DateChangeReceiver`](file:///Users/net/Desktop/Works/DogPregnancyTracker/app/src/main/java/com/example/pawnatal/data/notification/DateChangeReceiver.kt) registering `ACTION_DATE_CHANGED`, `ACTION_TIME_SET`, and `ACTION_TIMEZONE_CHANGED`.
  - Added `refreshForToday()` lifecycle synchronization in `PregnancyRepository` and `PregnancyViewModel`, updating gestation day, week countdown, trimester progress bar, and resetting daily routine checklists on midnight rollover.
  - Wired `MainActivity.onResume()` to trigger automatic timeline recalculation whenever the app is brought into focus.
- **Standardized Indian Metric System & Date Formats**:
  - Configured default settings to Indian metric standards: Celsius (`°C`) for temperature and kilograms (`kg`) for maternal weight.
  - Standardized Indian date formats across all UI screens and clinical calculators (`d MMMM yyyy`, `dd/MM/yyyy`, `EEEE, d MMMM yyyy`, and `d MMM`).
  - Updated all clinical presets, maternal vitals cards, basal temperature records, and schedule views to respect Indian metric formatting.

---

## [1.1.0] - 2026-10-11

### Fixed & Enhanced
- **Global Unit System Fidelity (Temperature & Weight)**:
  - Created centralized [`UnitFormatter`](file:///Users/net/Desktop/Works/DogPregnancyTracker/app/src/main/java/com/example/pawnatal/data/util/UnitFormatter.kt) handling bi-directional conversion, formatting, and mathematical rounding for temperature (`°F` vs `°C`) and weight (`lbs` vs `kg`).
  - Unit settings dynamically take effect across the entire app: Home Maternal Vitals, Basal Temperature tracker chart and chips, Temperature recording dialog (`RecordTempDialog`), Setup screen baseline/current weights, and Labor drop alert triggers.
  - Implemented unit-aware temperature logging with smart increments (±0.1°C / ±0.1°F) and safe canonical storage in Fahrenheit.
- **Material 3 Date Picker Integration**:
  - Integrated Material 3 `DatePickerDialog` across all date fields: First Mating Date, Second Tie Date, and Schedule Gestation Calendar header shortcut.
  - Fully calendar-assisted date selection replaces manual string editing while preserving ISO-8601 validation.
- **Dynamic Maternal Journey Progression**:
  - Fixed Trimester timeline visualization bug in `HomeDashboardScreen`: Replaced hardcoded trimester styling with dynamic `LinearProgressIndicator` bound to calculated gestation day `(currentDay / 21f)`.
  - On Day 2, only Trimester 1 displays active progress (~9% fill), while Trimesters 2 and 3 accurately remain at 0%.
- **Interactive Gestation Calendar Strip**:
  - Redesigned `ScheduleAndMedicalScreen` calendar header: Replaced static days with a dynamic 7-day gestation window calculated relative to `firstMatingDate` and today.
  - Added clickable dates with persistent `selectedDate` state, week navigation arrows (`<`, `>`), "Jump to Today" shortcut, DatePicker dialog launcher, and contextual Gestation Day pill tags (`D1..D63`, `Pre`, `Post`).
- **Medications & Supplements Layout Alignment**:
  - Completely redesigned medication regimen cards into a clean 2-tier responsive layout.
  - Top row aligns the completion checkbox, medication title, and color-coded status badge pill; indented lower row formats full clinical dosage instructions and category pills without line squeezing or text clipping.
- **Real Veterinarian & Emergency Details Persistence**:
  - Removed all hardcoded dummy doctor names and phone numbers across presets, default plans, and notifications.
  - Added dedicated Veterinary & Clinic Details card in `SetupAndAiPlanScreen` and interactive "Edit Veterinary Details" modal in `TipsAndWhelpingScreen`.
  - Persisted attending veterinarian name, clinic name, clinic phone, clinic address, and 24/7 emergency hotline in `DamProfile` with instant one-tap dialing and Google Calendar appointment sync.
- **Automatic Gestation & Clinical Plan Reconciliation**:
  - Created [`GestationCalculator`](file:///Users/net/Desktop/Works/DogPregnancyTracker/app/src/main/java/com/example/pawnatal/data/util/GestationCalculator.kt) engine that dynamically calculates countdowns, milestones, ultrasound/radiograph visits, and daily routines relative to `firstMatingDate` and current date.
  - Added "Reset to Baseline" shortcut in Setup screen to easily resynchronize maternal weight records.

---

## [1.0.0] - 2026-10-11

### Added
- **Obstetric AI Engine (Clinical Model v2.4)**:
  - Integration with multiple AI providers: OpenAI (`sol`, `gpt-4o-mini`, `gpt-4o`), Google Gemini (Free Tier: `gemini-2.5-flash`), Groq Cloud (`llama-3.3-70b-versatile`), OpenRouter, and custom local endpoints.
  - Clinical prompt builder synthesizes tailored gestation care plan based on dam's breed, mating date, second tie date, age, parity, and baseline weight.
  - Strict JSON schema deserialization into type-safe Kotlin models using `kotlinx.serialization`.
  - Built-in clinical fallback engine (`DefaultClinicalPlanProvider`) supporting 100% offline functionality.

- **Pregnancy Setup & AI Care Plan (Entry Point Screen)**:
  - Form capturing Dam & Mating Profile: Dam name, breed classification, first mating date, second tie date, age, parity, baseline weight, and current weight.
  - One-tap "Synthesize AI Clinical Care Plan" with real-time generation indicator.
  - Live Calculated Obstetric Insights card (63-day gestation span, estimated whelping date, trimester completion percentage, litter size estimate).
  - AI-Scheduled Milestones (Ultrasound day 28, radiograph/X-ray puppy count day 45–52, pre-labor hypothermia rectal logging day 56).
  - Breed-calibrated maternal nutrition notes and caloric shift indicators.

- **Home Dashboard Screen**:
  - Maternal journey status banner displaying current week, trimester, and countdown.
  - Interactive highlights: percent complete, litter estimate, countdown weeks, and 3-trimester progression bar.
  - Week-specific fetus growth card with fruit/object size comparison (e.g. kiwi ~7.5 cm, ~45g) and anatomical highlights (bone ossification, paw claws, 200–240 bpm heartbeat).
  - Maternal health vitals (weight gain tracker, appetite scaling, nesting mood).
  - Today's routine interactive checklist with protocol counter.
  - Quick action shortcuts (Record Temp, Call Vet, Add Note, Emergency Hotline).

- **Weekly Guide Screen**:
  - Horizontal scrollable week selector spanning all 9 gestation weeks (Days 1–63).
  - Deep-dive cards for each week covering fetal developmental milestones, puppy size in mm/grams, and heart rates.
  - Mother dog status updates covering physical changes, calorie scaling (+25% puppy formula), and behavioral den sanctuary preparation.
  - Clinical action checklists for each week with interactive check toggles.
  - Historical collapsible accordions reviewing Weeks 1–5 (Fertilization, Implantation, Organogenesis) and Weeks 7–9 countdown.

- **Schedule & Medical Screen + Google Calendar Integration**:
  - Gestation calendar strip with daily activity indicators.
  - Pre-Labor Basal Rectal Temperature Tracker with custom Canvas Bezier curve trend chart and red dotted 99.0°F (37.2°C) whelping drop trigger line.
  - "Add to G-Cal" integration generating calendar intents for Google Calendar and native device calendar apps with prefilled appointment details.
  - Directions button opening Google Maps with clinic location.
  - Interactive medications and supplements daily regimen checklist with status counters.

- **Tips & Whelping Kit Screen**:
  - 24/7 Rapid response emergency vet hotline with 1-tap dialer.
  - Immediate danger sign triggers ("When to Dial Immediately") highlighting placental separation risks and eclampsia symptoms.
  - Interactive 14-item whelping kit readiness checklist with dynamic progress bar and completion percentage.
  - Nutrition guide on transitioning to high-protein puppy kibble and portion frequency.
  - Behavioral nesting insights and ambient whelping room temperature recommendations (75°F–80°F).
  - Clinical Myth Buster addressing calcium timing dangers and postpartum eclampsia prevention.

- **Settings Screen**:
  - Model and provider selector with password-masked API key input fields.
  - Support for custom model ID (including `sol`) and custom base URLs for local LLMs/proxies.
  - "Test Model Connection" button with real-time ping and feedback status.
  - Customizable units (°F vs °C for temperature, lbs vs kg for weight).
  - Notification toggles for daily care routines and pre-labor hypothermia alerts.
  - Preset switcher for instant demonstration with sample breeds (Golden Retriever, French Bulldog, Labrador).

- **AI Harness & Tooling**:
  - Dedicated agent skill: `.agents/skills/dog-pregnancy-tracker/SKILL.md`.
  - Comprehensive specification: `AI_HARNESS.md`.
  - Full project guide: `README.md`.
  - Unit test suite: `PawNatalClinicalTest.kt` verifying gestation calculation formulas, temperature thresholds, prompt formatting, and settings.
