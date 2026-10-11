package com.example.pawnatal.ui.schedule

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.pawnatal.data.model.VetAppointment
import com.example.pawnatal.data.util.UnitFormatter
import com.example.pawnatal.theme.*
import com.example.pawnatal.ui.components.RecordTempDialog
import com.example.pawnatal.ui.components.TempChartCanvas
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAndMedicalScreen(
    viewModel: PregnancyViewModel,
    modifier: Modifier = Modifier
) {
    val carePlan by viewModel.carePlan.collectAsState()
    val damProfile by viewModel.damProfile.collectAsState()
    val tempHistory by viewModel.tempHistory.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var showTempDialog by remember { mutableStateOf(false) }
    var showCalendarDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var stripAnchorDate by remember { mutableStateOf(LocalDate.now()) }

    val scrollState = rememberScrollState()

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
        // Gestation Calendar Strip Card - Fully interactive & relative to mating date
        val matingDate = remember(damProfile.firstMatingDate) {
            try {
                LocalDate.parse(damProfile.firstMatingDate)
            } catch (e: Exception) {
                LocalDate.now()
            }
        }

        fun getGestationDayFor(date: LocalDate): Long {
            return ChronoUnit.DAYS.between(matingDate, date) + 1
        }

        val currentSelectedDay = getGestationDayFor(selectedDate)
        val currentSelectedWeek = ((currentSelectedDay - 1) / 7 + 1).coerceIn(1, 9)

        if (showCalendarDatePicker) {
            val initialMillis = remember(selectedDate) {
                selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            }
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            DatePickerDialog(
                onDismissRequest = { showCalendarDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val chosen = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            selectedDate = chosen
                            stripAnchorDate = chosen
                        }
                        showCalendarDatePicker = false
                    }) {
                        Text("Jump to Date", color = PawPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCalendarDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gestation Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { stripAnchorDate = stripAnchorDate.minusDays(7) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week", tint = PawPrimary)
                        }

                        IconButton(
                            onClick = { showCalendarDatePicker = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = PawPrimary)
                        }

                        IconButton(
                            onClick = { stripAnchorDate = stripAnchorDate.plusDays(7) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Week", tint = PawPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive 7-Day Strip
                val stripDays = remember(stripAnchorDate) {
                    (-3..3).map { stripAnchorDate.plusDays(it.toLong()) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    stripDays.forEach { date ->
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val gDay = getGestationDayFor(date)

                        val dayLabel = when {
                            gDay in 1..63 -> "D$gDay"
                            gDay < 1 -> "Pre"
                            else -> "Post"
                        }

                        val dayOfWeekAbbr = date.dayOfWeek.name.take(3)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> PawPrimary
                                        isToday -> PawPrimary.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    }
                                )
                                .clickable { selectedDate = date }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = dayOfWeekAbbr,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${date.dayOfMonth}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else if (isToday) PawPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selected Day Summary Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val statusText = when {
                                currentSelectedDay in 1..63 -> "Gestation Day $currentSelectedDay of 63 • Week $currentSelectedWeek"
                                currentSelectedDay < 1 -> "Pre-Mating Period (${-currentSelectedDay + 1} days before mating)"
                                else -> "Post-Gestation / Nursing Period"
                            }
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                color = PawPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (selectedDate != LocalDate.now()) {
                            TextButton(
                                onClick = {
                                    selectedDate = LocalDate.now()
                                    stripAnchorDate = LocalDate.now()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Jump to Today", fontSize = 11.sp, color = PawPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pre-Labor Basal Temp Section
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
                        Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pre-Labor Basal Temp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Twice daily rectal reading",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clinical trigger note
                Card(
                    colors = CardDefaults.cardColors(containerColor = PawSuccess.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PawSuccess, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val safeZoneText = UnitFormatter.formatTempSafeZone(settings.tempUnit)
                        val dropThresholdText = UnitFormatter.formatTempDropThreshold(settings.tempUnit)
                        Text(
                            text = "Normal Safe Baseline ($safeZoneText). A sharp drop below $dropThresholdText signals progesterone plummeting and whelping onset within 12–24 hours.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Chart
                TempChartCanvas(records = tempHistory, tempUnit = settings.tempUnit)

                Spacer(modifier = Modifier.height(16.dp))

                // Morning / Evening Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WbTwilight, contentDescription = null, tint = PawTertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Today Morning", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(UnitFormatter.formatTemperature(100.4, settings.tempUnit), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Today Evening", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val latest = tempHistory.lastOrNull()?.tempFahrenheit ?: 100.6
                                Text(UnitFormatter.formatTemperature(latest, settings.tempUnit), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showTempDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PawPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log New Temperature Reading", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Veterinary Milestones
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
                        Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = PawSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Veterinary Milestones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Trimester 3",
                        fontSize = 12.sp,
                        color = PawSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                carePlan.veterinaryVisits.forEach { visit ->
                    val isUpcoming = visit.status == "UPCOMING"
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUpcoming) PawPrimaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (visit.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.Event,
                                        contentDescription = null,
                                        tint = if (visit.status == "COMPLETED") PawSuccess else PawPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = visit.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (visit.status == "COMPLETED") PawSuccess.copy(alpha = 0.15f)
                                            else if (isUpcoming) PawPrimary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        text = if (isUpcoming) "In ${visit.daysUntil} Days" else visit.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (visit.status == "COMPLETED") PawSuccess else PawPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = visit.scheduledDateText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PawPrimary)
                            Text(text = "${visit.doctorName} • ${visit.clinicName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = visit.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                            Spacer(modifier = Modifier.height(12.dp))

                            // Google Calendar & Directions Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.addAppointmentToCalendar(context, visit) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PawSecondary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to G-Cal", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val geoUri = Uri.parse("geo:0,0?q=${Uri.encode("${visit.clinicName}, ${visit.address}")}")
                                        val intent = Intent(Intent.ACTION_VIEW, geoUri)
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Directions", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Medications & Daily Regimen
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
                        Icon(imageVector = Icons.Default.Medication, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Medications & Supplements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val takenCount = carePlan.medications.count { it.isCompleted }
                    Text(
                        text = "$takenCount/${carePlan.medications.size} Taken",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PawPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                carePlan.medications.forEach { med ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (med.isCompleted) PawSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.toggleMedication(med.id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Header row: Checkbox + Name + Status Badge Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = med.isCompleted,
                                    onCheckedChange = { viewModel.toggleMedication(med.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = PawSuccess),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = med.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f),
                                    color = if (med.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (med.isCompleted) PawSuccess.copy(alpha = 0.15f)
                                            else PawPrimary.copy(alpha = 0.12f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (med.isCompleted) "Completed ✓" else med.dueStatus,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (med.isCompleted) PawSuccess else PawPrimary
                                    )
                                }
                            }

                            // Dosage Instructions & Category Tag
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.width(34.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = med.dosageInstruction,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (med.category.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Category: ${med.category}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PawSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hydration & Calorie Density Guide
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = PawSecondary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hydration & Calorie Density",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Week 6: Fetal size accelerates by 30%. Increase meal frequency to 3-4 smaller energy-dense portions daily.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
