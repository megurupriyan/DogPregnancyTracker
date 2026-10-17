package com.example.pawnatal.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawnatal.data.model.DailyRoutineItem
import com.example.pawnatal.data.model.DamProfile
import com.example.pawnatal.data.model.ObstetricCarePlan
import com.example.pawnatal.theme.*
import com.example.pawnatal.ui.components.RecordTempDialog
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel

@Composable
fun HomeDashboardScreen(
    viewModel: PregnancyViewModel,
    modifier: Modifier = Modifier
) {
    val damProfile by viewModel.damProfile.collectAsState()
    val carePlan by viewModel.carePlan.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var showTempDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val insights = carePlan.insights

    if (showTempDialog) {
        RecordTempDialog(
            initialTemp = 100.4,
            tempUnit = settings.tempUnit,
            onDismiss = { showTempDialog = false },
            onConfirm = { temp, notes ->
                viewModel.recordTemperature(temp, notes)
                showTempDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Gestational Stage Status Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = PawPrimaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gestational Stage: Week ${insights.currentWeek} (Trimester ${insights.currentTrimester})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PawOnPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Due ${insights.estimatedWhelpDate} • Day ${insights.currentDay} of ${insights.gestationSpanDays}",
                        fontSize = 12.sp,
                        color = PawOnPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(PawPrimary, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${insights.daysRemaining} Days Left",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bella's Journey Card
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
                    Column {
                        Text(
                            text = "${damProfile.name}'s Journey",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${damProfile.breed} • Confirmed ${insights.estimatedLitterSize}",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PawPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = PawPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3 Highlights Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Progress Done
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${insights.percentComplete}% Done", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PawPrimary)
                            Text(text = "${insights.currentDay} of ${insights.gestationSpanDays} Days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Litter Estimate
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = insights.estimatedLitterSize,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = PawSecondary
                            )
                            Text(text = "Litter Estimate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Countdown
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${insights.daysRemaining / 7} Weeks", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PawTertiary)
                            Text(text = "Countdown", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Trimester Timeline Bar - Dynamically computed based on currentDay
                val currentDay = insights.currentDay
                val tri1Progress = (currentDay / 21f).coerceIn(0f, 1f)
                val tri2Progress = ((currentDay - 21) / 21f).coerceIn(0f, 1f)
                val tri3Progress = ((currentDay - 42) / 21f).coerceIn(0f, 1f)

                val isTri1 = currentDay in 1..21
                val isTri2 = currentDay in 22..42
                val isTri3 = currentDay >= 43

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { tri1Progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PawPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { tri2Progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PawPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { tri3Progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PawPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tri 1 (Days 1–21)",
                        fontSize = 10.sp,
                        fontWeight = if (isTri1) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTri1) PawPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tri 2 (Days 22–42)",
                        fontSize = 10.sp,
                        fontWeight = if (isTri2) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTri2) PawPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tri 3 (Days 43–63)",
                        fontSize = 10.sp,
                        fontWeight = if (isTri3) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTri3) PawPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fetus Growth Dynamic Card
        val currentWeekGuide = carePlan.weeklyGuides.find { it.weekNumber == insights.currentWeek }
            ?: carePlan.weeklyGuides.firstOrNull()

        val weekEmoji = when (insights.currentWeek) {
            1 -> "🔬"
            2 -> "📍"
            3 -> "🌱"
            4 -> "🌰"
            5 -> "🍋"
            6 -> "🥝"
            7 -> "🍎"
            8 -> "🍊"
            else -> "🐶"
        }

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
                    Column {
                        Text(
                            text = "Fetus Growth: Week ${insights.currentWeek}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentWeekGuide?.title ?: "Growth Phase"} • ${currentWeekGuide?.dayRangeText ?: "Days ${(insights.currentWeek - 1) * 7 + 1}–${insights.currentWeek * 7}"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PawSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ChildCare, contentDescription = null, tint = PawSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Size Comparison Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = PawTertiaryContainer.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = weekEmoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Size: ${currentWeekGuide?.puppySizeComparison ?: "Developing Embryo"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PawOnTertiaryContainer
                            )
                            Text(
                                text = currentWeekGuide?.summary ?: "Fetal organs and skeletal framework developing on schedule.",
                                fontSize = 12.sp,
                                color = PawOnTertiaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Anatomy Features
                val milestones = currentWeekGuide?.anatomicalMilestones.orEmpty()
                if (milestones.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        milestones.take(3).forEach { feat ->
                            FeaturePill(
                                icon = Icons.Default.CheckCircle,
                                title = feat.title,
                                subtitle = feat.description,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dam Health Vitals
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "${damProfile.name}'s Maternal Vitals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val weightFormatted = com.example.pawnatal.data.util.UnitFormatter.formatWeight(damProfile.currentWeightLbs, settings.weightUnit)
                    val gainFormatted = com.example.pawnatal.data.util.UnitFormatter.formatWeightGain(damProfile.weightGainLbs, settings.weightUnit)
                    val (appetiteVal, appetiteSub) = when (insights.currentWeek) {
                        in 1..3 -> "Normal" to "Maintenance"
                        in 4..5 -> "Surging (+15%)" to "3 meals daily"
                        in 6..7 -> "High (+25–35%)" to "Small meals x4"
                        else -> "Fluctuating" to "Energy-dense"
                    }
                    val (moodVal, moodSub) = when (insights.currentWeek) {
                        in 1..3 -> "Affectionate" to "Normal routine"
                        in 4..5 -> "Protective" to "Restful naps"
                        in 6..7 -> "Nesting" to "Den seeking"
                        else -> "Restless" to "Labor vigilance"
                    }
                    VitalCard(
                        label = "Weight",
                        value = weightFormatted,
                        sub = gainFormatted,
                        icon = Icons.Default.Scale,
                        modifier = Modifier.weight(1f)
                    )
                    VitalCard(
                        label = "Appetite",
                        value = appetiteVal,
                        sub = appetiteSub,
                        icon = Icons.Default.Restaurant,
                        modifier = Modifier.weight(1f)
                    )
                    VitalCard(
                        label = "Mood",
                        value = moodVal,
                        sub = moodSub,
                        icon = Icons.Default.Bedtime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Today's Routine Checklist
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
                    Column {
                        Text(
                            text = "Today's Routine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val completedCount = carePlan.todayRoutines.count { it.isDone }
                        Text(
                            text = "$completedCount of ${carePlan.todayRoutines.size} protocols logged today",
                            fontSize = 12.sp,
                            color = PawSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = { showTempDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Log Routine", tint = PawPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                carePlan.todayRoutines.forEach { routine ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (routine.isDone) PawSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.toggleTodayRoutine(routine.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = routine.isDone,
                                onCheckedChange = { viewModel.toggleTodayRoutine(routine.id) },
                                colors = CheckboxDefaults.colors(checkedColor = PawSuccess)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = routine.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (routine.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = routine.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (routine.type == "TEMPERATURE" && !routine.isDone) {
                                TextButton(onClick = { showTempDialog = true }) {
                                    Text("Log", color = PawPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showTempDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PawPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record Temp", fontSize = 13.sp)
            }

            val phoneToCall = damProfile.vetClinicPhone.ifBlank { damProfile.emergencyPhone }
            OutlinedButton(
                onClick = {
                    if (phoneToCall.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                        context.startActivity(intent)
                    } else {
                        android.widget.Toast.makeText(context, "No clinic phone saved. Please configure in Setup tab.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = PawSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Vet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 13.sp,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VitalCard(
    label: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val valueFontSize = when {
        value.length > 11 -> 12.sp
        value.length > 8 -> 13.sp
        else -> 14.sp
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = PawSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = valueFontSize,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                fontSize = 10.sp,
                color = PawPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
