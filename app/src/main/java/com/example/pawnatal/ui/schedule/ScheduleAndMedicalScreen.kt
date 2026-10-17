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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

    val today = LocalDate.now()
    var showTempDialog by remember { mutableStateOf(false) }
    var showCalendarDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember(today, damProfile.firstMatingDate) { mutableStateOf(today) }
    var stripAnchorDate by remember(today, damProfile.firstMatingDate) { mutableStateOf(today) }
    var showAllReadings by remember { mutableStateOf(false) }

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
            com.example.pawnatal.data.util.GestationCalculator.parseMatingDate(damProfile.firstMatingDate)
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pre-Labor Basal Temp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Twice daily rectal reading • Progesterone drop tracking",
                        fontSize = 12.sp,
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

                // Today Morning / Evening Dynamic Cards
                val todayMorningReading = remember(tempHistory, today) {
                    tempHistory.filter {
                        val d = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault())
                        d.toLocalDate() == today && d.hour < 12
                    }.lastOrNull()
                }
                val todayEveningReading = remember(tempHistory, today) {
                    tempHistory.filter {
                        val d = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault())
                        d.toLocalDate() == today && d.hour >= 12
                    }.lastOrNull()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTempDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WbTwilight, contentDescription = null, tint = PawTertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Today Morning", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                if (todayMorningReading != null) {
                                    Text(
                                        text = UnitFormatter.formatTemperature(todayMorningReading.tempFahrenheit, settings.tempUnit),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                } else {
                                    Text("Tap to record", fontSize = 12.sp, color = PawPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTempDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Today Evening", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                if (todayEveningReading != null) {
                                    Text(
                                        text = UnitFormatter.formatTemperature(todayEveningReading.tempFahrenheit, settings.tempUnit),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                } else {
                                    val latest = tempHistory.lastOrNull()
                                    if (latest != null) {
                                        Text(
                                            text = UnitFormatter.formatTemperature(latest.tempFahrenheit, settings.tempUnit),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1
                                        )
                                    } else {
                                        Text("Tap to record", fontSize = 12.sp, color = PawPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dedicated Readings Log Table
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Readings Log History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${tempHistory.size} total entries",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Table Container Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Table Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DATE & TIME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.35f)
                            )
                            Text(
                                text = "STAGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.85f)
                            )
                            Text(
                                text = "READING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.0f)
                            )
                            Text(
                                text = "STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.95f)
                            )
                        }

                        // Table Body Rows (Reverse chronological: most recent first)
                        val sortedRecords = remember(tempHistory) { tempHistory.sortedByDescending { it.timestamp } }
                        val recordsToDisplay = if (showAllReadings) sortedRecords else sortedRecords.take(4)

                        if (recordsToDisplay.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No temperature records logged yet.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            recordsToDisplay.forEachIndexed { index, record ->
                                val recordDt = Instant.ofEpochMilli(record.timestamp).atZone(ZoneId.systemDefault())
                                val dateFormatted = recordDt.format(DateTimeFormatter.ofPattern("dd MMM"))
                                val timeFormatted = recordDt.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                val gestationDay = getGestationDayFor(recordDt.toLocalDate())

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Column 1: Date & Time
                                        Column(modifier = Modifier.weight(1.35f)) {
                                            Text(
                                                text = dateFormatted,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = timeFormatted,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        // Column 2: Gestation Day Stage
                                        Box(
                                            modifier = Modifier.weight(0.85f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (gestationDay in 1..65) "Day $gestationDay" else "Pre-term",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        // Column 3: Formatted Temperature Pill
                                        Box(
                                            modifier = Modifier.weight(1.0f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val formattedTemp = UnitFormatter.formatTemperature(record.tempFahrenheit, settings.tempUnit)
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (record.isHypothermiaDrop) PawAlertRed.copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.surface,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = formattedTemp,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (record.isHypothermiaDrop) PawAlertRed else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        // Column 4: Status Badge
                                        Box(
                                            modifier = Modifier.weight(0.95f),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (record.isHypothermiaDrop) PawAlertRed.copy(alpha = 0.15f)
                                                        else PawSuccess.copy(alpha = 0.15f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (record.isHypothermiaDrop) "Drop ⚠️" else "Normal",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (record.isHypothermiaDrop) PawAlertRed else PawSuccess,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }

                                    if (record.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Note: ${record.notes}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (index < recordsToDisplay.size - 1) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        // Expand/Collapse toggle if more than 4 items
                        if (sortedRecords.size > 4) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            TextButton(
                                onClick = { showAllReadings = !showAllReadings },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (showAllReadings) "Show Recent (4)" else "View All ${sortedRecords.size} Records",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PawPrimary
                                )
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
                        text = "Trimester ${carePlan.insights.currentTrimester}",
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
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                        color = if (visit.status == "COMPLETED") PawSuccess else PawPrimary,
                                        maxLines = 1,
                                        softWrap = false
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
                                    Text("Add to G-Cal", fontSize = 12.sp, maxLines = 1, softWrap = false)
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
                                    Text("Directions", fontSize = 12.sp, maxLines = 1, softWrap = false)
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
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Medication, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Medications & Supplements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val takenCount = carePlan.medications.count { it.isCompleted }
                    Text(
                        text = "$takenCount/${carePlan.medications.size} Taken",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PawPrimary,
                        maxLines = 1
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
                                    color = if (med.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                        color = if (med.isCompleted) PawSuccess else PawPrimary,
                                        maxLines = 1,
                                        softWrap = false
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
                    val guide = carePlan.weeklyGuides.find { it.weekNumber == carePlan.insights.currentWeek }
                    val nutritionTip = guide?.damStatus?.calorieDemandNote?.takeIf { it.isNotBlank() }
                        ?: carePlan.breedNutritionNote
                    Text(
                        text = "Week ${carePlan.insights.currentWeek}: $nutritionTip",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
