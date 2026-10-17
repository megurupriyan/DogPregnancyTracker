package com.example.pawnatal.ui.weekly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawnatal.data.model.WeeklyGuideDetail
import com.example.pawnatal.theme.*
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel

@Composable
fun WeeklyGuideScreen(
    viewModel: PregnancyViewModel,
    modifier: Modifier = Modifier
) {
    val carePlan by viewModel.carePlan.collectAsState()
    val damProfile by viewModel.damProfile.collectAsState()
    val weeklyGuides = carePlan.weeklyGuides

    var selectedWeekNumber by remember(carePlan.insights.currentWeek) { mutableStateOf(carePlan.insights.currentWeek) }
    var expandWeeks1to5 by remember { mutableStateOf(false) }
    var expandWeeks7to9 by remember { mutableStateOf(false) }

    val activeGuide = weeklyGuides.find { it.weekNumber == selectedWeekNumber }
        ?: weeklyGuides.firstOrNull()
        ?: return

    val scrollState = rememberScrollState()
    val weekSelectorScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Horizontal Week Selector Bar
        Text(
            text = "Gestation Week Selector",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(weekSelectorScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..9).forEach { weekNum ->
                val isSelected = weekNum == selectedWeekNumber
                val isCurrent = weekNum == carePlan.insights.currentWeek

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PawPrimary else if (isCurrent) PawPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                    modifier = Modifier
                        .clickable { selectedWeekNumber = weekNum }
                        .padding(vertical = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Wk $weekNum",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Days ${(weekNum - 1) * 7 + 1}–${weekNum * 7}",
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Week Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(PawSecondary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeGuide.trimesterText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawSecondary
                        )
                    }

                    val deliveryCountdown = if (activeGuide.weekNumber == carePlan.insights.currentWeek) {
                        carePlan.insights.daysRemaining
                    } else {
                        activeGuide.daysToDelivery
                    }
                    Text(
                        text = "$deliveryCountdown Days to Delivery",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PawPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activeGuide.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = activeGuide.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Puppy Development Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ChildCare, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Puppy Development",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Week ${activeGuide.weekNumber} State",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Size & Heart rate banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PawTertiaryContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Size Comparison", fontSize = 11.sp, color = PawOnTertiaryContainer)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeGuide.puppySizeComparison,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PawOnTertiaryContainer
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Fetal Heart Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeGuide.fetalHeartRateBpm,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PawPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Anatomical Milestones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                activeGuide.anatomicalMilestones.forEach { feat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(PawPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = feat.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = feat.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mother Dog (Bella) Changes Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = PawSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mother Dog (${damProfile.name})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Healthy Progress",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PawSuccess
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Physical
                DetailRow(
                    icon = Icons.Default.PregnantWoman,
                    title = "Physical & Abdomen Changes",
                    description = activeGuide.damStatus.physicalChanges
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Nutrition
                DetailRow(
                    icon = Icons.Default.Restaurant,
                    title = "Calorie & Diet Demand",
                    description = activeGuide.damStatus.calorieDemandNote
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Behavioral
                DetailRow(
                    icon = Icons.Default.Bed,
                    title = "Behavior & Den Sanctuary",
                    description = activeGuide.damStatus.behavioralNote
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Clinical Action Checklist
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clinical Action Checklist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${activeGuide.clinicalActions.size} Actions",
                        fontSize = 11.sp,
                        color = PawSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                activeGuide.clinicalActions.forEach { action ->
                    var isChecked by remember(action.id) { mutableStateOf(action.isChecked) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { isChecked = !isChecked },
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = PawSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = action.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = action.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Historical Timeline Accordions (Weeks 1-5 & Weeks 7-9)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Weeks 1-5 Accordion Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandWeeks1to5 = !expandWeeks1to5 }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Weeks 1 – 5 Review", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Fertilization, Implantation & Organogenesis", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(
                        imageVector = if (expandWeeks1to5) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = expandWeeks1to5) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "• Week 1–2 (Days 1–14): Conception to Blastocyst. Cellular migration into uterine horns.\n" +
                                    "• Week 3 (Days 15–21): Uterine Implantation. Protective zonary placenta formation.\n" +
                                    "• Week 4–5 (Days 22–35): Organogenesis & Heartbeat. First ultrasound detection window.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Weeks 7-9 Accordion Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandWeeks7to9 = !expandWeeks7to9 }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upcoming, contentDescription = null, tint = PawSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Weeks 7 – 9 Countdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Coat Development, Colostrum & Labor Watch", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(
                        imageVector = if (expandWeeks7to9) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = expandWeeks7to9) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "• Week 7 (Days 43–49): Hair Growth & Calcification. Safe X-ray puppy counting.\n" +
                                    "• Week 8 (Days 50–56): Crowding & Colostrum Milk. Temperature charting begins twice daily.\n" +
                                    "• Week 9 (Days 57–63): Full Maturity & Whelping. Critical temperature drop below 99.0°F.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
