package com.example.pawnatal.data.repository

import com.example.pawnatal.data.model.*
import com.example.pawnatal.data.util.GestationCalculator
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

object DefaultClinicalPlanProvider {

    fun createDefaultPlan(
        dam: DamProfile = DamProfile(),
        asOfDate: LocalDate = LocalDate.now()
    ): ObstetricCarePlan {
        val insights = GestationCalculator.calculateInsights(
            matingDateStr = dam.firstMatingDate,
            breed = dam.breed,
            existingInsights = null,
            asOfDate = asOfDate
        )

        val rawPlan = ObstetricCarePlan(
            insights = insights,
            breedNutritionNote = "${dam.breed} gestation protocol: Maintain high-protein, calorie-dense nutrition with small, divided meals as fetal growth progresses.",
            milestones = listOf(
                ScheduledMilestone(
                    id = "m1",
                    title = "Ultrasound Gestational Sac Window",
                    dayWindow = "Day 28",
                    description = "Viability & heartbeat detection confirmed. Cardiac activity verified at 200+ bpm.",
                    isCompleted = insights.currentDay >= 28,
                    iconType = "check"
                ),
                ScheduledMilestone(
                    id = "m2",
                    title = "Radiograph (Puppy Count X-Ray)",
                    dayWindow = "Day 45–52",
                    description = "Fetal skull and spine calcification readable. Crucial to verify exact head count prior to whelping.",
                    isCompleted = insights.currentDay >= 52,
                    iconType = "radiology"
                ),
                ScheduledMilestone(
                    id = "m3",
                    title = "Pre-Labor Hypothermia Watch",
                    dayWindow = "Day 56",
                    description = "Twice-daily rectal logging begins. A drop below 37.2°C (99.0°F) signals delivery within 12–24h.",
                    isCompleted = insights.currentDay >= 56,
                    iconType = "thermostat"
                ),
                ScheduledMilestone(
                    id = "m4",
                    title = "Breed Nutrition & Calcium Protocol",
                    dayWindow = "Day 58",
                    description = "Whelping box familiarization completed. Calcium paste staged strictly for active labor onset.",
                    isCompleted = insights.currentDay >= 58,
                    iconType = "restaurant"
                )
            ),
            todayRoutines = listOf(
                DailyRoutineItem(
                    id = "r1",
                    title = "Prenatal Multi-Vitamin & DHA",
                    subtitle = "Given at 8:00 AM with meal",
                    timeOfDay = "8:00 AM",
                    isDone = true,
                    type = "MEDICATION"
                ),
                DailyRoutineItem(
                    id = "r2",
                    title = "Rectal Temperature Log",
                    subtitle = "Baseline reading • Critical twice-daily logging starts Day 56",
                    timeOfDay = "6:00 PM",
                    isDone = false,
                    type = "TEMPERATURE"
                ),
                DailyRoutineItem(
                    id = "r3",
                    title = "Gentle 15-min walk & belly check",
                    subtitle = "Scheduled for 6:30 PM (Evening)",
                    timeOfDay = "6:30 PM",
                    isDone = false,
                    type = "EXERCISE"
                )
            ),
            veterinaryVisits = listOf(
                VetAppointment(
                    id = "v1",
                    title = "Day 28 Gestation Sac Ultrasound",
                    scheduledDateText = "Gestation Sac & Viability Scan",
                    isoDate = "",
                    status = "UPCOMING",
                    doctorName = dam.vetDoctorName.ifBlank { "Primary Veterinarian" },
                    clinicName = dam.vetClinicName.ifBlank { "Veterinary Clinic" },
                    address = dam.vetClinicAddress.ifBlank { "Clinic Address" },
                    notes = "Confirm gestational sacs, heartbeat viability, and approximate litter count."
                ),
                VetAppointment(
                    id = "v2",
                    title = "Pre-Whelping Puppy Count X-Ray",
                    scheduledDateText = "Radiograph Assessment",
                    isoDate = "",
                    status = "UPCOMING",
                    doctorName = dam.vetDoctorName.ifBlank { "Primary Veterinarian" },
                    clinicName = dam.vetClinicName.ifBlank { "Veterinary Clinic" },
                    address = dam.vetClinicAddress.ifBlank { "Clinic Address" },
                    notes = "Essential radiograph to verify exact fetal skull and spine count for safe whelping preparation."
                ),
                VetAppointment(
                    id = "v3",
                    title = "Pre-Labor Health & Delivery Prep Exam",
                    scheduledDateText = "Labor Readiness Check",
                    isoDate = "",
                    status = "PENDING",
                    doctorName = dam.vetDoctorName.ifBlank { "Primary Veterinarian" },
                    clinicName = dam.vetClinicName.ifBlank { "Veterinary Clinic" },
                    address = dam.vetClinicAddress.ifBlank { "Clinic Address" },
                    notes = "Maternal vital assessment, calcium baseline, and emergency dystocia review."
                )
            ),
            medications = listOf(
                MedicationItem(
                    id = "med1",
                    name = "Prenatal Multi-Vitamin & DHA",
                    dosageInstruction = "1 chewable tablet with morning meal • 8:00 AM",
                    timing = "8:00 AM",
                    category = "SUPPLEMENT",
                    isCompleted = true,
                    dueStatus = "Done"
                ),
                MedicationItem(
                    id = "med2",
                    name = "Folic Acid Micro-dose",
                    dosageInstruction = "400 mcg micro-dose tablet with evening meal • 8:00 PM",
                    timing = "8:00 PM",
                    category = "SUPPLEMENT",
                    isCompleted = false,
                    dueStatus = "Pending"
                ),
                MedicationItem(
                    id = "med3",
                    name = "Fenbendazole Dewormer Protocol",
                    dosageInstruction = "Day 40–60 protocol: 50mg/kg daily paste with food as advised by vet",
                    timing = "7:00 PM",
                    category = "DEWORMER",
                    isCompleted = false,
                    dueStatus = "Due Day 40"
                )
            ),
            weeklyGuides = (1..9).map { week -> generateWeeklyDetail(week, dam) },
            whelpingKit = listOf(
                WhelpingKitItem("k1", "Digital rapid rectal thermometer", "For tracking labor drop below 99.0°F (37.2°C)", true, "MEDICAL"),
                WhelpingKitItem("k2", "Clean surgical scissors & dental floss", "Sterilized cord clamping & precision snip", true, "MEDICAL"),
                WhelpingKitItem("k3", "Iodine / Betadine disinfectant solution", "Quick umbilical stump dipping", true, "MEDICAL"),
                WhelpingKitItem("k4", "Bulb syringe aspirator (mucus suction)", "Clear puppy nasal & airway fluid quickly", true, "MEDICAL"),
                WhelpingKitItem("k5", "Heating pad with thick towel barrier", "Keeps newborns warm (cannot thermoregulate)", true, "WARMING"),
                WhelpingKitItem("k6", "Clean whelping towels & digital scale", "12+ fresh towels + gram/oz precision scale", false, "SANITATION"),
                WhelpingKitItem("k7", "Puppy milk replacer (Esbilac) & bottles", "Emergency supplementation & mini nipples", false, "NURSING"),
                WhelpingKitItem("k8", "Whelping box den with draft-proof rails", "Crush-preventing pig rails installed", true, "WARMING"),
                WhelpingKitItem("k9", "Disposable nitrile gloves & sterile lubricant", "Clean examinations & glove barrier", true, "MEDICAL"),
                WhelpingKitItem("k10", "Color-coded puppy identification collars", "Track individual birth weight and feeding", true, "MEDICAL"),
                WhelpingKitItem("k11", "Veterinary calcium paste (Oral gel)", "Staged for active labor contractions only", true, "MEDICAL"),
                WhelpingKitItem("k12", "Nutri-Cal or pure glucose syrup", "Quick energy for dam during prolonged labor", true, "NURSING"),
                WhelpingKitItem("k13", "Trash disposal bags & puppy pee pads", "Immediate soiled whelp liner changes", false, "SANITATION"),
                WhelpingKitItem("k14", "Emergency Vet 24/7 hotline card on box", "Direct priority hotline posted by whelping box", false, "MEDICAL")
            ),
            emergencyInfo = EmergencyVetInfo(
                clinicName = dam.vetClinicName.ifBlank { "Veterinary Clinic" },
                hotlinePhone = dam.emergencyPhone.ifBlank { dam.vetClinicPhone },
                distanceText = "Primary Veterinary Contact",
                onDutyDoctor = dam.vetDoctorName.ifBlank { "Attending Veterinarian" },
                immediateDialTriggers = listOf(
                    "Strong active contractions for >30 mins with no puppy produced.",
                    "Greenish or black discharge before the arrival of the first pup (placental detachment alert).",
                    "More than 2 hours gap between delivered puppies with visible continuous straining.",
                    "Trembling, muscle rigidity, panting, or ataxia (signs of postpartum hypocalcemia / eclampsia)."
                ),
                mythBusterTitle = "Calcium Supplementation: When & Why?",
                mythBusterContent = "Myth: Giving calcium pills before birth makes the puppies bones stronger.\n\nClinical Fact: Supplementing calcium prior to active labor actually suppresses parathyroid gland regulation, dangerously triggering life-threatening postpartum eclampsia. Only offer vet-approved calcium paste during or after active labor begins."
            )
        )

        return GestationCalculator.reconcileCarePlan(rawPlan, dam, asOfDate)
    }

    private fun generateWeeklyDetail(week: Int, dam: DamProfile): WeeklyGuideDetail {
        val startDay = (week - 1) * 7 + 1
        val endDay = week * 7
        val daysToDeliv = maxOf(0, 63 - endDay)

        return when (week) {
            1 -> WeeklyGuideDetail(
                weekNumber = 1,
                dayRangeText = "Days 1–7",
                trimesterText = "Trimester 1 • Fertilization & Early Cell Division",
                title = "Ovulation & Cleavage Stage",
                summary = "Sperm fertilizes the ova in the oviducts. The zygotes begin rapid mitotic cleavage as they migrate towards the uterine horns.",
                daysToDelivery = 56,
                puppySizeComparison = "Microscopic blastocysts (Zygotes)",
                puppyWeightGrams = "<0.01 g",
                fetalHeartRateBpm = "Pre-cardiac",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Fertilization", "Ova fertilized in distal oviduct ampulla."),
                    AnatomicalFeature("Zygote Division", "Cell division proceeds to 4-cell and 8-cell stages."),
                    AnatomicalFeature("Tubal Migration", "Embryos slowly migrate towards uterine horns.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Minimal to no outward physical change.",
                    calorieDemandNote = "Maintain normal maintenance feeding. Do not increase calories yet.",
                    behavioralNote = "Normal temperament. Maintain calm exercise and daily routines."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w1_1", "Log Exact Tie Date", "Record date and time of first and second ties.", true),
                    ClinicalActionItem("w1_2", "Discontinue Flea/Tick Meds", "Verify all pest medications are strictly pregnancy-safe.", true)
                ),
                historyHighlights = "Conception phase initiated. Hormonal switch to continuous progesterone production."
            )
            2 -> WeeklyGuideDetail(
                weekNumber = 2,
                dayRangeText = "Days 8–14",
                trimesterText = "Trimester 1 • Blastocyst Migration",
                title = "Blastocyst Entry into Uterine Horns",
                summary = "Embryos reach the blastocyst stage and enter the uterine horns, remaining free-floating before spacing themselves evenly.",
                daysToDelivery = 49,
                puppySizeComparison = "Pinhead size blastocysts (~1 mm)",
                puppyWeightGrams = "<0.1 g",
                fetalHeartRateBpm = "Pre-cardiac",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Free Floating", "Blastocysts disperse evenly throughout both uterine horns."),
                    AnatomicalFeature("Zona Pellucida Shedding", "Embryo hatches from its outer coating."),
                    AnatomicalFeature("Cell Differentiation", "Inner cell mass differentiates into embryonic germ layers.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Slight pinking of vulva tissue.",
                    calorieDemandNote = "Standard maintenance diet. Avoid raw meat or unpasteurized dairy.",
                    behavioralNote = "Normal demeanor. Dam may seek slightly more affection."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w2_1", "Maintain Hydration", "Ensure constant access to clean, filtered water.", true)
                )
            )
            3 -> WeeklyGuideDetail(
                weekNumber = 3,
                dayRangeText = "Days 15–21",
                trimesterText = "Trimester 1 • Nidation & Implantation",
                title = "Uterine Implantation & Placental Attachment",
                summary = "Embryos embed firmly into the endometrium. The protective canine zonary placenta begins establishing its vascular network.",
                daysToDelivery = 42,
                puppySizeComparison = "Sesame seed size (~3 mm)",
                puppyWeightGrams = "~0.5 g",
                fetalHeartRateBpm = "Heart tube forming (~120 bpm)",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Zonary Placentation", "Endotheliochorial placental band secures nutrition."),
                    AnatomicalFeature("Neural Tube", "Primitive streak and neural tube begin neural closure."),
                    AnatomicalFeature("Heart Tube", "Primitive cardiovascular tube initiates rhythmic contractions.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Mild morning sluggishness or appetite fluctuation may occur.",
                    calorieDemandNote = "Offer small divided meals if slight nausea occurs around Days 18-21.",
                    behavioralNote = "Dam may rest more frequently during afternoon hours."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w3_1", "Monitor Appetite", "Record any transient nausea or pickiness.", true),
                    ClinicalActionItem("w3_2", "Schedule Day 28 Ultrasound", "Contact vet clinic to book gestational sac scan.", true)
                )
            )
            4 -> WeeklyGuideDetail(
                weekNumber = 4,
                dayRangeText = "Days 22–28",
                trimesterText = "Trimester 2 • Organogenesis & Heartbeat",
                title = "Fetal Organogenesis & Ultrasound Window",
                summary = "Major organs, eyes, and limb buds take shape. Uterine swellings feel like spherical ampullae during abdominal palpation.",
                daysToDelivery = 35,
                puppySizeComparison = "Walnut size (~15 mm)",
                puppyWeightGrams = "~2–3 g",
                fetalHeartRateBpm = "200 – 230 BPM",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Organogenesis", "Liver, kidneys, eyes, and neural cord developing rapidly."),
                    AnatomicalFeature("Limb Buds", "Distinct front and rear limb buds appear."),
                    AnatomicalFeature("Vigorous Heartbeat", "Heart chambers functional at 200+ beats per minute.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Clear vaginal mucous discharge ('pregnancy mucus') may appear.",
                    calorieDemandNote = "Begin introducing +10% quality protein into daily ration.",
                    behavioralNote = "Dam protects abdomen instinctively from rough pets or other dogs."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w4_1", "Day 28 Gestation Ultrasound", "Confirm heartbeats and amniotic sac health.", true),
                    ClinicalActionItem("w4_2", "Palpation Safety Warning", "Do not forcefully squeeze the abdomen.", true)
                )
            )
            5 -> WeeklyGuideDetail(
                weekNumber = 5,
                dayRangeText = "Days 29–35",
                trimesterText = "Trimester 2 • Fetal Transition & Toes",
                title = "Embryo to Fetus Transition",
                summary = "The embryos are now officially recognized as fetuses. Whiskers, toe claws, and sexual organs differentiate.",
                daysToDelivery = 28,
                puppySizeComparison = "Size of a Fig / Lime (~35 mm)",
                puppyWeightGrams = "~10–15 g",
                fetalHeartRateBpm = "210 – 240 BPM",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Toe Separation", "Digital webbing recedes to reveal distinct paw digits."),
                    AnatomicalFeature("Eyelids & Muzzle", "Eyelids develop and close over eyes; muzzle elongates."),
                    AnatomicalFeature("External Genitalia", "Male and female anatomical traits differentiate.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Abdomen begins gentle rounding. Mammary glands enlarge slightly.",
                    calorieDemandNote = "Increase food intake by 15-20%. Feed 3 moderate meals per day.",
                    behavioralNote = "Appetite markedly surges. Dam seeks shaded, comfortable spots."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w5_1", "Start High-Quality Diet", "Incorporate premium puppy food or nutrient density.", true),
                    ClinicalActionItem("w5_2", "Discontinue Strenuous Jumps", "Protect joints and spine as center of gravity shifts.", true)
                )
            )
            6 -> WeeklyGuideDetail(
                weekNumber = 6,
                dayRangeText = "Days 36–42",
                trimesterText = "Trimester 2 • Critical Ossification",
                title = "Rapid Growth & Skeleton Hardening",
                summary = "Cartilage turns into mineralized bone. Your puppies are transitioning from delicate embryos into resilient little bodies!",
                daysToDelivery = 21,
                puppySizeComparison = "Size of a Kiwi Fruit (~75 mm)",
                puppyWeightGrams = "~40–50 grams",
                fetalHeartRateBpm = "200 – 220 BPM",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Skeleton Mineralization", "Soft cartilage structures actively hardening into mineralized bone.", "format_h4"),
                    AnatomicalFeature("Facial & Whisker Buds", "Facial features define further; delicate vibrissae emerge on muzzle.", "face"),
                    AnatomicalFeature("Claw & Digit Separation", "Distinct claw plates and digital pads fully discernible on paws.", "file_download")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Abdomen expansion is clearly visible. Mammary tissue developing; nipples turn darker and firmer.",
                    calorieDemandNote = "Calorie demand increases by +25%. Transition strictly toward puppy formula or high-protein meals across 3-4 small daily rations.",
                    behavioralNote = "Emerging nesting impulses. She may begin pacing, seeking secluded dim corners, or collecting soft blankets."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w6_1", "Book Week 7/8 X-Ray Radiograph", "Essential after Day 45 to count fetal skulls and spine columns.", true),
                    ClinicalActionItem("w6_2", "Cease Strenuous Exercise", "Discontinue jumping or rough fetch. Transition to calm 15-min leash walks.", true),
                    ClinicalActionItem("w6_3", "Assemble Whelping Box", "Introduce whelping box in quiet room (72-75°F) early so dam accepts it.", false)
                ),
                historyHighlights = "Critical bone ossification underway. Heart sounds distinct with Doppler."
            )
            7 -> WeeklyGuideDetail(
                weekNumber = 7,
                dayRangeText = "Days 43–49",
                trimesterText = "Trimester 3 • Hair Growth & Calcification",
                title = "Coat Pigmentation & Fetal Calcification",
                summary = "Puppies develop individual hair coats, markings, and distinct fur textures. Bones are sufficiently calcified for radiographic skull counts.",
                daysToDelivery = 14,
                puppySizeComparison = "Size of a Small Apple / Peach (~10 cm)",
                puppyWeightGrams = "~90–120 grams",
                fetalHeartRateBpm = "200 – 220 BPM",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Coat Pigmentation", "Melanocytes produce breed colors and markings."),
                    AnatomicalFeature("Full Bone Calcification", "X-ray radiograph can clearly count puppies by counting skulls and spines."),
                    AnatomicalFeature("Sensory Buds", "Tactile hairs and sensory nerves fully wired.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Flank tight and noticeably swollen. Milk glands develop substantial colostrum stores.",
                    calorieDemandNote = "Feed 35-40% above baseline. Multiple small meals throughout the day due to compressed stomach.",
                    behavioralNote = "Spends extensive time inspecting the whelping box den."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w7_1", "Execute Puppy Count X-Ray", "Confirm exact number of puppies with vet clinic.", false),
                    ClinicalActionItem("w7_2", "Clean & Disinfect Whelping Liners", "Wash all towels and blankets in hypoallergenic detergent.", false)
                )
            )
            8 -> WeeklyGuideDetail(
                weekNumber = 8,
                dayRangeText = "Days 50–56",
                trimesterText = "Trimester 3 • Crowding & Colostrum",
                title = "Fetal Crowding & Movement Tangible",
                summary = "Puppies take up maximum uterine capacity. Gentle ripples and kicks are visible through the dam's abdominal wall when she rests.",
                daysToDelivery = 7,
                puppySizeComparison = "Size of a Grapefruit (~14 cm)",
                puppyWeightGrams = "~200–280 grams",
                fetalHeartRateBpm = "190 – 210 BPM",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Fetal Kicks", "Active movements, stretching, and turning clearly tangible on flanks."),
                    AnatomicalFeature("Lung Surfactant", "Surfactant production ramps up in fetal pulmonary alveoli."),
                    AnatomicalFeature("Claw Hardening", "Nails and footpads reach full whelping readiness.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Colostrum may drip from nipples. Flank drops downward as ligaments relax.",
                    calorieDemandNote = "Maximum metabolic demand. Keep food energy-dense; appetite may fluctuate as stomach is pressed.",
                    behavioralNote = "Intense nesting behavior. Scratching bed and seeking human reassurance."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w8_1", "Start Basal Temp Logging", "Record rectal temperature twice daily (AM and PM) without fail.", false),
                    ClinicalActionItem("w8_2", "Review Emergency Vet Route", "Confirm 24/7 hospital phone and travel time under 20 mins.", false)
                )
            )
            else -> WeeklyGuideDetail(
                weekNumber = 9,
                dayRangeText = "Days 57–63",
                trimesterText = "Trimester 3 • Full Maturity & Whelping",
                title = "Final Preparation & Active Labor Watch",
                summary = "Puppies are fully formed and mature. Watch the twice-daily rectal temperature closely: a drop below 99.0°F (37.2°C) confirms whelping within 12–24h!",
                daysToDelivery = 0,
                puppySizeComparison = "Full Newborn Puppy (~18–22 cm)",
                puppyWeightGrams = "~350–500 grams",
                fetalHeartRateBpm = "180 – 210 BPM",
                anatomicalMilestones = listOf(
                    AnatomicalFeature("Pulmonary Readiness", "Lungs ready for immediate air breathing upon delivery."),
                    AnatomicalFeature("Placental Detachment Prep", "Placental margins prepare for natural delivery separation."),
                    AnatomicalFeature("Whelping Position", "Pups orient into cranial or caudal presentation in pelvic canal.")
                ),
                damStatus = DamWeeklyStatus(
                    physicalChanges = "Progesterone plummets. Body temperature drops <99.0°F (37.2°C). Clear/opaque mucus plug discharges.",
                    calorieDemandNote = "Dam will typically refuse food 12-24 hours prior to active labor. Offer electrolytes/water.",
                    behavioralNote = "Heavy panting, shivering, repetitive nesting, pacing, and licking vulva."
                ),
                clinicalActions = listOf(
                    ClinicalActionItem("w9_1", "Hourly Temperature Check", "Log temp every 4 hours once reading drops below 99.5°F.", false),
                    ClinicalActionItem("w9_2", "Sterilize Scissors & Floss", "Ensure whelping kit is open and within arm's reach of box.", false),
                    ClinicalActionItem("w9_3", "Pre-Warm Newborn Den", "Turn heating pad on low with thick towel barrier ready.", false)
                )
            )
        }
    }

    fun getInitialTemperatureHistory(): List<TemperatureRecord> {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        val fmt = SimpleDateFormat("dd MMM a", Locale.ENGLISH)
        return listOf(
            TemperatureRecord(
                id = "t1",
                timestamp = now - dayMillis * 2,
                displayLabel = fmt.format(Date(now - dayMillis * 2)),
                tempFahrenheit = 100.8,
                notes = "Normal baseline resting"
            ),
            TemperatureRecord(
                id = "t2",
                timestamp = now - dayMillis * 2 + 43200000L,
                displayLabel = fmt.format(Date(now - dayMillis * 2 + 43200000L)),
                tempFahrenheit = 100.6,
                notes = "Evening post-meal"
            ),
            TemperatureRecord(
                id = "t3",
                timestamp = now - dayMillis,
                displayLabel = fmt.format(Date(now - dayMillis)),
                tempFahrenheit = 100.4,
                notes = "Morning resting, calm"
            ),
            TemperatureRecord(
                id = "t4",
                timestamp = now,
                displayLabel = "${fmt.format(Date(now))} (Latest)",
                tempFahrenheit = 100.5,
                notes = "Normal baseline reading"
            )
        )
    }
}
