package com.example.pawnatal.data.network

import com.example.pawnatal.data.model.DamProfile

object AiPromptBuilder {

    fun buildSystemInstruction(): String {
        return """
            You are PawNatal's Canine Obstetric AI Clinical Engine (v2.4 Clinical Veterinary Model).
            Your job is to generate a complete, medically accurate, breed-calibrated pregnancy care and whelping plan for a pregnant canine dam.
            
            Keep all string descriptions concise and data-dense (1-2 sentences per description field) to ensure rapid response generation without sacrificing clinical veterinary precision.
            You MUST return ONLY a valid, raw JSON object conforming EXACTLY to the provided schema with NO markdown ticks (no ```json ... ```), no preambles, and no conversational text.
        """.trimIndent()
    }

    fun buildUserPrompt(dam: DamProfile): String {
        return """
            Synthesize a clinical obstetric plan for the following canine dam:
            - Dam Name: ${dam.name}
            - Breed: ${dam.breed}
            - Size & Classification: ${dam.sizeClassification}
            - First Mating / Ovulation Date: ${dam.firstMatingDate}
            - Second Tie Date: ${dam.secondTieDate}
            - Dam Age: ${dam.ageYears} Years
            - Parity: ${dam.parity}
            - Baseline Weight: ${dam.baselineWeightLbs} lbs
            - Current Weight: ${dam.currentWeightLbs} lbs
            - Vet Doctor: ${dam.vetDoctorName}
            - Vet Clinic: ${dam.vetClinicName}
            - Emergency Phone: ${dam.emergencyPhone}
            
            Calculate and generate:
            1. Obstetric Insights:
               - Gestation span (standard 63 days, range 61-65 days)
               - Estimated whelping date (First Mating Date + 63 days)
               - Current day of pregnancy (relative to first mating date or default day 42)
               - Current week and trimester (Tri 1: 1-21, Tri 2: 22-42, Tri 3: 43-63)
               - Percent completion
               - Estimated litter size calibrated to ${dam.breed} and parity (${dam.parity})
               - All visit and milestone dates computed relative to First Mating Date (${dam.firstMatingDate})
            2. Breed Nutrition Note:
               - High-protein gestation kibble caloric shift tailored to ${dam.breed}
            3. Scheduled Milestones (At least 4 milestones: Ultrasound sac window day 28, puppy count X-Ray day 45-52, pre-labor hypothermia rectal watch day 56, active whelping den prep).
            4. Today's Routine (Daily medications, temperature checks, gentle exercise, nutrition).
            5. Veterinary Visits (Day 28 ultrasound, Day 45-52 puppy count X-ray, Day 56 pre-labor bloodwork).
            6. Medications & Supplements (Prenatal multivitamins & DHA, folic acid, fenbendazole dewormer protocol).
            7. Weekly Guides (9 detailed weeks from Conception to Whelping, with puppy size comparisons, anatomy milestones, dam physical/behavioral changes, calorie scaling, and clinical actions).
            8. Whelping Kit Items (14 essential items including digital rectal thermometer, suction bulb, umbilical floss/scissors, iodine, heating pad, whelping box, colostrum/milk replacer, etc.).
            9. Emergency Vet Info with immediate red-flag dial triggers and calcium timing myth buster.

            JSON Schema required:
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
                "estimatedLitterSize": "1–4 Pups (Keep short, e.g. 1–4 Pups)",
                "litterSubtitle": "Pomeranian primiparous avg",
                "trimesterDescription": "Trimester I: Embryonic Cleavage & Oviduct Transit",
                "tieDateDisplay": "Oct 10, 2026 (Short date format)",
                "todayDisplay": "Day 1 (Short format)",
                "dueDisplay": "Dec 12, 2026 (Short date format)"
              },
              "breedNutritionNote": "String",
              "milestones": [
                {
                  "id": "String",
                  "title": "String",
                  "dayWindow": "String",
                  "description": "String",
                  "isCompleted": false,
                  "iconType": "radiology"
                }
              ],
              "todayRoutines": [
                {
                  "id": "String",
                  "title": "String",
                  "subtitle": "String",
                  "timeOfDay": "String",
                  "isDone": false,
                  "type": "MEDICATION"
                }
              ],
              "veterinaryVisits": [
                {
                  "id": "String",
                  "title": "String",
                  "scheduledDateText": "String",
                  "isoDate": "YYYY-MM-DD",
                  "status": "UPCOMING",
                  "doctorName": "String",
                  "clinicName": "String",
                  "address": "String",
                  "notes": "String",
                  "daysUntil": 3
                }
              ],
              "medications": [
                {
                  "id": "String",
                  "name": "String",
                  "dosageInstruction": "String",
                  "timing": "String",
                  "category": "SUPPLEMENT",
                  "isCompleted": false,
                  "dueStatus": "Done"
                }
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
                  "anatomicalMilestones": [
                    { "title": "String", "description": "String", "icon": "pets" }
                  ],
                  "damStatus": {
                    "physicalChanges": "String",
                    "calorieDemandNote": "String",
                    "behavioralNote": "String"
                  },
                  "clinicalActions": [
                    { "id": "String", "title": "String", "description": "String", "isChecked": false }
                  ],
                  "historyHighlights": "String"
                }
              ],
              "whelpingKit": [
                {
                  "id": "String",
                  "name": "String",
                  "purpose": "String",
                  "isReady": false,
                  "category": "MEDICAL"
                }
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
        """.trimIndent()
    }
}
