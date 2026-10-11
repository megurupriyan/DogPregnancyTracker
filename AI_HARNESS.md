# PawNatal Canine Obstetric AI Harness & Model Specification

## 1. Architectural Philosophy
The PawNatal AI Harness bridges modern Large Language Models (LLMs) with clinical canine reproductive medicine.
Rather than requiring breeders to consult fragmented veterinary books or complex gestation calculation tables, PawNatal passes the dam's reproductive history (first tie date, confirm date, breed, baseline weight, age, and parity) to an LLM calibrated to act as a **Canine Obstetric Clinical Model**.

The LLM synthesizes a deterministic, structured JSON care plan containing:
1. **Calculated Obstetric Insights**: Gestation length (63 days default, range 61–65), due date, current trimester, litter expectation.
2. **Breed-Specific Nutritional Protocols**: Caloric shifts, puppy kibble transition, dividing rations into 3–4 meals to adjust for compressed uterine space.
3. **Scheduled Veterinary Milestones**: Sac ultrasound (Day 28), Puppy Count Radiograph / X-Ray (Day 45–52), Pre-labor bloodwork (Day 56).
4. **Daily Routine & Medication Regimens**: Prenatal DHA, folic acid, fenbendazole deworming protocols.
5. **Complete 9-Week Gestation Guide**: Puppy size comparisons (e.g., walnut, lime, kiwi, apple), anatomical milestones, maternal physical/behavioral changes, and clinical action checklists.
6. **14-Item Whelping Kit Readiness Checklist**: Interactive supply checks (digital rectal thermometer, mucus aspirator, dental floss, heating pads, emergency puppy milk replacer).
7. **24/7 Emergency Veterinary Rapid Response**: Immediate danger signs (contractions >30 min without puppy, green discharge before first puppy, eclampsia signs) and calcium timing myth buster.

---

## 2. Multi-Provider AI Engine Architecture

PawNatal implements a flexible multi-provider adapter in `com.example.pawnatal.data.network.AiClient`:

| Provider | Supported Models | Default Endpoint | Pricing / Tier |
| :--- | :--- | :--- | :--- |
| **OpenAI** | `sol`, `gpt-4o-mini`, `gpt-4o`, custom | `https://api.openai.com/v1/chat/completions` | Pay-as-you-go |
| **Google Gemini** | `gemini-2.5-flash`, `gemini-1.5-flash` | `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent` | **Free Tier Available** |
| **Groq Cloud** | `llama-3.3-70b-versatile`, `llama-3.1-8b-instant` | `https://api.groq.com/openai/v1/chat/completions` | **Free Tier / High Speed** |
| **OpenRouter** | `meta-llama/llama-3.3-70b-instruct:free`, `google/gemini-2.0-flash-exp:free` | `https://openrouter.ai/api/v1/chat/completions` | Free & Low Cost models |
| **Custom / Local** | `sol`, local Ollama / LM Studio models | Configurable (e.g. `http://10.0.2.2:11434/v1/chat/completions`) | Free / Local / Private |

### Provider Configuration via UI
Breeders can configure their provider, model ID (e.g. `sol`), and API keys in the **Settings Screen** (`SettingsScreen.kt`).
The screen includes a **"Test Model Connection"** button which executes a real-time health-check ping against the selected endpoint.

---

## 3. Strict JSON Schema Specification

The model is instructed with a system prompt and returns pure JSON conforming to `ObstetricCarePlan`:

```kotlin
@Serializable
data class ObstetricCarePlan(
    val insights: ObstetricInsights,
    val breedNutritionNote: String,
    val milestones: List<ScheduledMilestone>,
    val todayRoutines: List<DailyRoutineItem>,
    val veterinaryVisits: List<VetAppointment>,
    val medications: List<MedicationItem>,
    val weeklyGuides: List<WeeklyGuideDetail>,
    val whelpingKit: List<WhelpingKitItem>,
    val emergencyInfo: EmergencyVetInfo
)
```

### Full JSON Output Structure
```json
{
  "insights": {
    "gestationSpanDays": 63,
    "gestationRangeText": "Range: 61–65 days",
    "estimatedWhelpDate": "May 3, 2025",
    "currentDay": 42,
    "currentWeek": 6,
    "currentTrimester": 2,
    "percentComplete": 67,
    "daysRemaining": 21,
    "estimatedLitterSize": "6 – 8 Pups",
    "litterSubtitle": "Golden primiparous avg.",
    "trimesterDescription": "Trimester II: Critical Ossification & Skeletal Hardening",
    "tieDateDisplay": "Mar 1",
    "todayDisplay": "Day 42",
    "dueDisplay": "May 3"
  },
  "breedNutritionNote": "Golden Retrievers carry an elevated risk of maternal fat accumulation without uterine tone. Transition Bella's diet today (Day 42) to high-protein gestation kibble with small, divided 3-meal rations (+25% protein, 1.2x base calories).",
  "milestones": [
    {
      "id": "m1",
      "title": "Ultrasound Gestational Sac Window",
      "dayWindow": "Day 28 • Mar 29",
      "description": "Viability & heartbeat detection confirmed. Cardiac activity verified at 220+ bpm.",
      "isCompleted": true,
      "iconType": "check"
    },
    {
      "id": "m2",
      "title": "Radiograph (Puppy Count X-Ray)",
      "dayWindow": "Day 45–52 • Apr 15–22",
      "description": "Fetal skull and spine calcification readable. Crucial to verify exact head count prior to whelping.",
      "isCompleted": false,
      "iconType": "radiology"
    },
    {
      "id": "m3",
      "title": "Pre-Labor Hypothermia Watch",
      "dayWindow": "Day 56 • Apr 26",
      "description": "Twice-daily rectal logging begins. A drop below 99.0°F (37.2°C) signals delivery within 12–24h.",
      "isCompleted": false,
      "iconType": "thermostat"
    }
  ],
  "todayRoutines": [
    {
      "id": "r1",
      "title": "Prenatal Calcium & Folate",
      "subtitle": "Given at 8:00 AM with meal",
      "timeOfDay": "8:00 AM",
      "isDone": true,
      "type": "MEDICATION"
    },
    {
      "id": "r2",
      "title": "Rectal Temperature Log",
      "subtitle": "Baseline 100.4°F • Critical monitor starts Day 56",
      "timeOfDay": "6:00 PM",
      "isDone": false,
      "type": "TEMPERATURE"
    }
  ],
  "veterinaryVisits": [
    {
      "id": "v1",
      "title": "Pre-Whelping Ultrasound & Puppy Count X-Ray",
      "scheduledDateText": "Sunday, April 20th • 10:30 AM",
      "isoDate": "2025-04-20",
      "status": "UPCOMING",
      "doctorName": "Dr. Aris Thorne, DVM",
      "clinicName": "Oakridge Veterinary Hospital & Reproduction Clinic",
      "address": "742 Evergreen Pkwy, Suite 100",
      "notes": "Essential radiograph after Day 45 to determine exact fetal skull count for safe delivery prep.",
      "daysUntil": 3
    }
  ],
  "medications": [
    {
      "id": "med1",
      "name": "Prenatal Multi-Vitamin & DHA",
      "dosageInstruction": "1 chewable tablet with morning meal • 8:00 AM",
      "timing": "8:00 AM",
      "category": "SUPPLEMENT",
      "isCompleted": true,
      "dueStatus": "Done"
    }
  ],
  "weeklyGuides": [
    {
      "weekNumber": 6,
      "dayRangeText": "Days 36–42",
      "trimesterText": "Trimester 2 • Critical Ossification",
      "title": "Rapid Growth & Skeleton Hardening",
      "summary": "Cartilage turns into mineralized bone. Your puppies are transitioning from delicate embryos into resilient little bodies!",
      "daysToDelivery": 21,
      "puppySizeComparison": "Size of a Kiwi Fruit (~75 mm)",
      "puppyWeightGrams": "~40–50 grams",
      "fetalHeartRateBpm": "200 – 220 BPM",
      "anatomicalMilestones": [
        { "title": "Skeleton Mineralization", "description": "Soft cartilage structures actively hardening into mineralized bone.", "icon": "format_h4" }
      ],
      "damStatus": {
        "physicalChanges": "Abdomen expansion is clearly visible. Mammary tissue developing; nipples turn darker and firmer.",
        "calorieDemandNote": "Calorie demand increases by +25%. Feed 3-4 small daily rations.",
        "behavioralNote": "Emerging nesting impulses. Provide quiet den sanctuary."
      },
      "clinicalActions": [
        { "id": "w6_1", "title": "Book Week 7/8 X-Ray Radiograph", "description": "Essential after Day 45 to count fetal skulls.", "isChecked": true }
      ],
      "historyHighlights": "Critical bone ossification underway."
    }
  ],
  "whelpingKit": [
    {
      "id": "k1",
      "name": "Digital rapid rectal thermometer",
      "purpose": "For tracking labor drop below 99°F",
      "isReady": true,
      "category": "MEDICAL"
    }
  ],
  "emergencyInfo": {
    "clinicName": "Oakridge Emergency Vet",
    "hotlinePhone": "(555) 234-5678",
    "distanceText": "4.2 miles away • Dr. Aris on Duty",
    "onDutyDoctor": "Dr. Aris Thorne, DVM",
    "immediateDialTriggers": [
      "Strong active contractions for >30 mins with no puppy produced.",
      "Greenish discharge before the arrival of the first pup (placental detachment alert).",
      "More than 2 hours gap between delivered puppies with visible strain."
    ],
    "mythBusterTitle": "Calcium Supplementation: When & Why?",
    "mythBusterContent": "Myth: Giving calcium pills before birth makes the puppies bones stronger.\n\nClinical Fact: Supplementing calcium prior to active labor actually suppresses parathyroid gland regulation, dangerously triggering life-threatening postpartum eclampsia. Only offer vet-approved calcium paste during or after active labor begins."
  }
}
```

---

## 4. Offline & Fallback Resilience
If an API key is not yet configured or an owner is operating without cell service, `DefaultClinicalPlanProvider.kt` automatically computes the exact mathematical gestational schedule:
- Whelp Date = First Mating Date + 63 days
- Day of Gestation = (Current Date - First Mating Date) in days
- Dynamic Trimester calculation (1: Days 1–21, 2: Days 22–42, 3: Days 43–63)
- Breed-specific adjustments based on dam classification.
- No network error blocks app navigation or corrupts user state.
