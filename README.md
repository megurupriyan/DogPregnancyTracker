# PawNatal • Canine Pregnancy & Gestation Tracker

> **A native Android application with an Obstetric AI Engine, Google Calendar integration, pre-labor basal temperature analytics, and a comprehensive 9-week gestation care harness.**

---

## 🌟 Overview

**PawNatal** is an Android application designed for dog owners, breeders, and canine care specialists. Grounded in veterinary reproductive science, PawNatal guides users through every stage of a dog's pregnancy—from the initial tie through the critical 63-day gestation window and into active whelping.

The app uses an **Obstetric AI Engine** to synthesize a personalized, breed-calibrated care plan based on dam profile inputs, complete with veterinary schedules, daily routines, nutritional shifts, and emergency whelping readiness.

---

## 📸 Key Features & Screen Highlights

### 1. Pregnancy Setup & AI Care Plan (Entry Point Screen)
- **Dam & Mating Profile Form**: Record dam name, breed classification (e.g. Large Breed, Small Breed), first mating / ovulation date, second tie date, age, parity, and baseline weight.
- **One-Tap AI Synthesis**: Press **"Synthesize AI Clinical Care Plan"** to call the configured LLM (`sol`, `gpt-4o-mini`, `gemini-2.5-flash`, etc.).
- **Calculated Obstetric Insights**: Instant display of gestation span (standard 63 days, range 61–65), due date, current trimester, and estimated litter size.
- **Calibrated Milestones**: Ultrasound sac check (Day 28), Puppy Count X-Ray (Day 45–52), and Pre-labor hypothermia rectal watch (Day 56).
- **Breed Nutrition Guidance**: Caloric scaling instructions tailored to the dam's breed and size.

### 2. Home Dashboard Screen
- **Maternal Journey Status**: Gestational week, trimester, and countdown badges.
- **Interactive Progress Rings**: 3-trimester progression indicator with Day X/63 milestone timeline.
- **Fetus Growth Card**: Week-specific fetus development cards comparing puppy size to fruits/objects (e.g. Kiwi fruit ~7.5 cm, ~45g), with anatomical notes (bone ossification, paw claw emergence, 200–240 bpm heartbeats).
- **Maternal Health Vitals**: Tracks weight gain (+4.8 lbs), calorie demand, and nesting mood.
- **Today's Routine**: Interactive checklist for daily prenatal vitamins, temp logs, and leash walks.
- **Quick Action Bar**: One-tap shortcuts for Recording Temp, Calling the assigned vet, Adding notes, and Dialing the emergency hotline.

### 3. Weekly Guide Screen (Weeks 1 to 9)
- **Horizontal Gestation Explorer**: Browse any of the 9 weeks of gestation.
- **Fetal Development**: Exact size in mm and grams, heartbeat rate, and organogenesis details.
- **Dam Physical & Behavioral Shifts**: Mammary tissue development, appetite scaling, and nesting instincts.
- **Clinical Action Checklists**: Interactive checkboxes for critical clinical steps (e.g. booking radiograph, ceasing strenuous exercise, introducing whelping den).
- **Timeline History Accordions**: Collapsible reviews of Weeks 1–5 (fertilization to embryo) and Weeks 7–9 countdown.

### 4. Medical & Care Schedule + Google Calendar
- **Interactive Calendar Strip**: Day-by-day gestation timeline with event markers.
- **Pre-Labor Basal Temperature Tracker**:
  - Live Canvas Bezier curve trend chart of rectal temperature readings.
  - Red dotted 99.0°F (37.2°C) trigger line.
  - Safe zone baseline band (99.8°F–101.2°F).
  - Instant alert triggering when rectal temperature drops below 99.0°F, signaling whelping onset within 12–24 hours.
- **Google Calendar Integration**: Tap **"Add to G-Cal"** to export appointments directly into Google Calendar or device calendars via standard Android Calendar provider intents.
- **One-Tap Directions**: Opens Google Maps with pre-filled clinic addresses.
- **Medication Regimen Checklist**: Prenatal multivitamins, DHA, folic acid, and fenbendazole deworming trackers.

### 5. Tips & Whelping Kit Screen
- **24/7 Rapid Response Emergency Vet**: Clinic details, priority hotline, and one-tap calling.
- **"When to Dial Immediately" Warning Card**: Critical triggers including active contractions >30 mins without puppy, greenish/black discharge before the first puppy, and eclampsia tremors.
- **Interactive 14-Item Whelping Kit Checklist**: Tracks scissors, aspirator bulbs, iodine, heating pads, towels, digital scales, and puppy milk replacer with a progress bar and completion percentage.
- **Nutrition Guide**: Gradual transition to high-protein puppy formula.
- **Clinical Myth Buster**: Clinical explanation on why pre-labor calcium supplements are dangerous and can cause postpartum eclampsia.

### 6. AI & App Settings Screen
- **Model Selector**: Switch between OpenAI (`sol`, `gpt-4o-mini`, `gpt-4o`), Google Gemini (`gemini-2.5-flash`), Groq Cloud (`llama-3.3-70b-versatile`), OpenRouter, or custom local endpoints.
- **Password-Masked API Key Fields**: Securely stored using Android SharedPreferences/DataStore.
- **Test Connection Tool**: Live ping test button with instant connection feedback.
- **App Customization**: Temperature units (°C default / °F), weight units (kg default / lbs), Indian date format standards (`d MMMM yyyy`, `dd/MM/yyyy`), and notification preferences.
- **Profile Presets**: Instantly switch between sample profiles (Bella - Golden Retriever, Luna - French Bulldog, Daisy - Labrador).

---

## 🛠️ Tech Stack & Architecture

- **Platform**: Android Native (API 24 to API 36)
- **Language**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose with Material 3 (BOM 2026.03.01)
- **Design Tokens**: Calibrated from Google Stitch design specifications
- **Architecture**: Clean Architecture (MVI / MVVM with StateFlow)
- **Serialization**: `kotlinx.serialization.json`
- **Networking**: `OkHttp 4.12`
- **Calendar**: Android `CalendarContract` & Google Calendar Web Intents
- **Notifications & Background Alarms**: `AlarmManager` with `setExactAndAllowWhileIdle(RTC_WAKEUP)`, `BroadcastReceiver` (`PregnancyAlarmReceiver`, `BootReceiver`), and `NotificationCompat` with Dedicated High-Priority Alert Channel
- **Lifecycle & Midnight Sync**: `DateChangeReceiver` + `MainActivity.onResume()` dynamic date recalculation
- **Testing**: JUnit 4 unit tests (`PawNatalClinicalTest.kt`)

---

## 🚀 Building & Running

### 1. Build the APK via Terminal
```bash
./gradlew assembleDebug
```
The compiled APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### 3. Deploy to Emulator or Device using Android CLI
```bash
# List available devices
adb devices

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch App
adb shell am start -n com.example.pawnatal/.MainActivity
```

---

## 📄 License
This project is developed for canine health tracking under the MIT License.
