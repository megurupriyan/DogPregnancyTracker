package com.example.pawnatal.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.pawnatal.data.model.DamProfile
import com.example.pawnatal.data.model.ObstetricCarePlan
import com.example.pawnatal.data.model.WeightUnit
import com.example.pawnatal.data.util.UnitFormatter
import com.example.pawnatal.theme.PawAlertRed
import com.example.pawnatal.theme.PawPrimary
import com.example.pawnatal.theme.PawSecondary
import com.example.pawnatal.theme.PawSuccess
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel
import com.example.pawnatal.ui.viewmodel.SynthesisUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupAndAiPlanScreen(
    viewModel: PregnancyViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val damProfile by viewModel.damProfile.collectAsState()
    val carePlan by viewModel.carePlan.collectAsState()
    val synthesisState by viewModel.synthesisState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var damName by remember(damProfile.name) { mutableStateOf(damProfile.name) }
    var breed by remember(damProfile.breed) { mutableStateOf(damProfile.breed) }
    var firstMatingDate by remember(damProfile.firstMatingDate) { mutableStateOf(damProfile.firstMatingDate) }
    var secondTieDate by remember(damProfile.secondTieDate) { mutableStateOf(damProfile.secondTieDate) }
    var ageYears by remember(damProfile.ageYears) { mutableStateOf(damProfile.ageYears.toString()) }
    var parity by remember(damProfile.parity) { mutableStateOf(damProfile.parity) }
    var baselineWeight by remember(damProfile.baselineWeightLbs) { mutableDoubleStateOf(damProfile.baselineWeightLbs) }
    var currentWeight by remember(damProfile.currentWeightLbs) { mutableDoubleStateOf(damProfile.currentWeightLbs) }

    // Date Picker States
    var showFirstMatingDatePicker by remember { mutableStateOf(false) }
    var showSecondTieDatePicker by remember { mutableStateOf(false) }

    // Vet Information Form States
    var vetDoctorName by remember(damProfile.vetDoctorName) { mutableStateOf(damProfile.vetDoctorName) }
    var vetClinicName by remember(damProfile.vetClinicName) { mutableStateOf(damProfile.vetClinicName) }
    var vetClinicPhone by remember(damProfile.vetClinicPhone) { mutableStateOf(damProfile.vetClinicPhone) }
    var vetClinicAddress by remember(damProfile.vetClinicAddress) { mutableStateOf(damProfile.vetClinicAddress) }
    var emergencyPhone by remember(damProfile.emergencyPhone) { mutableStateOf(damProfile.emergencyPhone) }
    var vetSavedMessage by remember { mutableStateOf<String?>(null) }

    // Expandable Card States
    var expandedMilestoneIds by remember { mutableStateOf(setOf<String>()) }
    var isNutritionExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // AI Model Engine Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PawPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PawPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Canine Obstetric AI Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active: ${settings.provider.displayName} • ${settings.activeModelId}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(onClick = onNavigateToSettings) {
                    Text(text = "Configure", fontSize = 12.sp, color = PawPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title and Subtitle - compact & refined
        Text(
            text = "Pregnancy Setup & AI Care Plan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Enter mating details and dam profile to synthesize ${damProfile.name}'s breed-calibrated timeline, vet milestones, and whelping alerts.",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status Messages Banner
        AnimatedVisibility(visible = synthesisState.successMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PawSuccess.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PawSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = synthesisState.successMessage.orEmpty(),
                        fontSize = 12.sp,
                        color = PawSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        AnimatedVisibility(visible = synthesisState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PawAlertRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PawAlertRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = synthesisState.errorMessage.orEmpty(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 1: Dam & Mating Profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dam & Mating Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Step 1 of 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dam Name
                OutlinedTextField(
                    value = damName,
                    onValueChange = {
                        damName = it
                        viewModel.updateDamProfile(damProfile.copy(name = it))
                    },
                    label = { Text("Dam Name") },
                    leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = PawPrimary) },
                    trailingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PawSuccess) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Breed & Size
                OutlinedTextField(
                    value = breed,
                    onValueChange = {
                        breed = it
                        viewModel.updateDamProfile(damProfile.copy(breed = it))
                    },
                    label = { Text("Breed & Size Classification") },
                    placeholder = { Text("e.g. Golden Retriever, Large Breed") },
                    supportingText = { Text("Default canine gestation: 63 days (61–65 days)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // First Mating Date with Material 3 Date Picker
                OutlinedTextField(
                    value = firstMatingDate,
                    onValueChange = {
                        firstMatingDate = it
                        viewModel.updateDamProfile(damProfile.copy(firstMatingDate = it))
                    },
                    readOnly = true,
                    singleLine = true,
                    label = { Text("First Mating / Ovulation Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PawPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { showFirstMatingDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick First Mating Date", tint = PawPrimary)
                        }
                    },
                    supportingText = { Text("Canine gestation defaults to 63 days from first mating.") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().clickable { showFirstMatingDatePicker = true }
                )

                if (showFirstMatingDatePicker) {
                    val initialMillis = remember(firstMatingDate) {
                        val parsed = com.example.pawnatal.data.util.GestationCalculator.parseMatingDate(firstMatingDate)
                        parsed.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                    }
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                    DatePickerDialog(
                        onDismissRequest = { showFirstMatingDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                                    firstMatingDate = date.toString()
                                    viewModel.updateDamProfile(damProfile.copy(firstMatingDate = date.toString()))
                                }
                                showFirstMatingDatePicker = false
                            }) {
                                Text("Select Date", color = PawPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showFirstMatingDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Second Tie Confirmation with Material 3 Date Picker
                OutlinedTextField(
                    value = secondTieDate,
                    onValueChange = {
                        secondTieDate = it
                        viewModel.updateDamProfile(damProfile.copy(secondTieDate = it))
                    },
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Second Tie / Confirmation Date (+48h)") },
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = PawSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { showSecondTieDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick Second Tie Date", tint = PawSecondary)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().clickable { showSecondTieDatePicker = true }
                )

                if (showSecondTieDatePicker) {
                    val initialMillis = remember(secondTieDate) {
                        val parsed = com.example.pawnatal.data.util.GestationCalculator.parseMatingDate(secondTieDate)
                        parsed.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                    }
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                    DatePickerDialog(
                        onDismissRequest = { showSecondTieDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                                    secondTieDate = date.toString()
                                    viewModel.updateDamProfile(damProfile.copy(secondTieDate = date.toString()))
                                }
                                showSecondTieDatePicker = false
                            }) {
                                Text("Select Date", color = PawPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSecondTieDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dam Age & Parity
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = ageYears,
                        onValueChange = {
                            ageYears = it
                            val parsed = it.toIntOrNull() ?: 3
                            viewModel.updateDamProfile(damProfile.copy(ageYears = parsed))
                        },
                        label = { Text("Age (Yrs)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = parity,
                        onValueChange = {
                            parity = it
                            viewModel.updateDamProfile(damProfile.copy(parity = it))
                        },
                        label = { Text("Parity") },
                        placeholder = { Text("1st Litter (Primiparous)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(2f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weight Controls (Respecting settings.weightUnit)
                val isKg = settings.weightUnit == WeightUnit.KG.name
                val stepAmountLbs = if (isKg) 0.440924 else 0.5 // ~0.2 kg or 0.5 lbs

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Baseline Weight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Baseline Pre-Mating Weight",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val gainFormatted = UnitFormatter.formatWeightGain(damProfile.weightGainLbs, settings.weightUnit)
                            Text(
                                text = "Current Gain: $gainFormatted",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = {
                                    val wasSynced = Math.abs(currentWeight - baselineWeight) < 0.1
                                    val newBase = (baselineWeight - stepAmountLbs).coerceAtLeast(3.0)
                                    baselineWeight = newBase
                                    if (wasSynced) {
                                        currentWeight = newBase
                                        viewModel.updateDamProfile(damProfile.copy(
                                            baselineWeightLbs = Math.round(newBase * 10.0) / 10.0,
                                            currentWeightLbs = Math.round(newBase * 10.0) / 10.0
                                        ))
                                    } else {
                                        viewModel.updateDamProfile(damProfile.copy(baselineWeightLbs = Math.round(newBase * 10.0) / 10.0))
                                    }
                                },
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                            }

                            Text(
                                text = UnitFormatter.formatWeight(baselineWeight, settings.weightUnit),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            FilledIconButton(
                                onClick = {
                                    val wasSynced = Math.abs(currentWeight - baselineWeight) < 0.1
                                    val newBase = baselineWeight + stepAmountLbs
                                    baselineWeight = newBase
                                    if (wasSynced) {
                                        currentWeight = newBase
                                        viewModel.updateDamProfile(damProfile.copy(
                                            baselineWeightLbs = Math.round(newBase * 10.0) / 10.0,
                                            currentWeightLbs = Math.round(newBase * 10.0) / 10.0
                                        ))
                                    } else {
                                        viewModel.updateDamProfile(damProfile.copy(baselineWeightLbs = Math.round(newBase * 10.0) / 10.0))
                                    }
                                },
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Current Weight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Maternal Weight",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tracked in checkups",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (Math.abs(currentWeight - baselineWeight) > 0.05) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• Reset",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PawPrimary,
                                        modifier = Modifier.clickable {
                                            currentWeight = baselineWeight
                                            viewModel.updateMaternalWeight(baselineWeight)
                                        }
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = {
                                    currentWeight = (currentWeight - stepAmountLbs).coerceAtLeast(3.0)
                                    viewModel.updateMaternalWeight(currentWeight)
                                },
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Current", modifier = Modifier.size(18.dp))
                            }

                            Text(
                                text = UnitFormatter.formatWeight(currentWeight, settings.weightUnit),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            FilledIconButton(
                                onClick = {
                                    currentWeight = currentWeight + stepAmountLbs
                                    viewModel.updateMaternalWeight(currentWeight)
                                },
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Current", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Save Real Veterinarian & Clinic Details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = PawPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Veterinary & Clinic Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (vetSavedMessage != null) {
                        Text(
                            text = "Saved ✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Save your attending veterinarian and 24/7 emergency hospital details for one-tap dialing and clinical plan sync.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Doctor Name
                OutlinedTextField(
                    value = vetDoctorName,
                    onValueChange = {
                        vetDoctorName = it
                        vetSavedMessage = null
                    },
                    label = { Text("Attending Veterinarian Name") },
                    placeholder = { Text("e.g. Dr. Sarah Jenkins, DVM") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PawPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Clinic Name
                OutlinedTextField(
                    value = vetClinicName,
                    onValueChange = {
                        vetClinicName = it
                        vetSavedMessage = null
                    },
                    label = { Text("Primary Veterinary Clinic / Hospital") },
                    placeholder = { Text("e.g. Central Companion Reproduction Clinic") },
                    leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null, tint = PawPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Clinic Phone - Full width, single line, prevents wrapping
                OutlinedTextField(
                    value = vetClinicPhone,
                    onValueChange = {
                        vetClinicPhone = it
                        vetSavedMessage = null
                    },
                    label = { Text("Clinic Phone") },
                    placeholder = { Text("+91 94968 11669") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PawPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 24/7 Emergency Line - Full width, single line, prevents wrapping
                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = {
                        emergencyPhone = it
                        vetSavedMessage = null
                    },
                    label = { Text("24/7 Emergency Hospital Line") },
                    placeholder = { Text("+91 94968 11669") },
                    leadingIcon = { Icon(Icons.Default.Emergency, contentDescription = null, tint = PawAlertRed) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Clinic Address
                OutlinedTextField(
                    value = vetClinicAddress,
                    onValueChange = {
                        vetClinicAddress = it
                        vetSavedMessage = null
                    },
                    label = { Text("Clinic Address (Directions & Maps)") },
                    placeholder = { Text("e.g. 100 Main St, Suite 200") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.updateVetDetails(
                            doctorName = vetDoctorName,
                            clinicName = vetClinicName,
                            phone = vetClinicPhone,
                            address = vetClinicAddress,
                            emergencyPhone = emergencyPhone
                        )
                        vetSavedMessage = "Vet details saved and synced with clinical plan!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PawSecondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Sync Veterinary Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (vetSavedMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = vetSavedMessage.orEmpty(),
                        color = PawSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Synthesize AI Care Plan Button
        Button(
            onClick = { viewModel.synthesizeCarePlan() },
            enabled = !synthesisState.isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = PawPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            if (synthesisState.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Synthesizing AI Clinical Care Plan...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Synthesize AI Clinical Care Plan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calibrates timeline, vet visits, and whelping kit",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 2: Calculated Obstetric Insights Card
        val insights = carePlan.insights
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = PawSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Calculated Obstetric Insights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(PawSecondary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Live Model",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hero Due Date Banner (Never wraps dates, elegant layout)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = PawPrimary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = PawPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ESTIMATED DUE DATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PawPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = insights.estimatedWhelpDate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(PawPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Day ${insights.currentDay}/${insights.gestationSpanDays} • Wk ${insights.currentWeek}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2-Column Balanced Key Metrics (Gestation Span & Litter Estimate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Gestation Span (50%)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timelapse, contentDescription = null, modifier = Modifier.size(15.dp), tint = PawPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gestation Span", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${insights.gestationSpanDays} Days", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = insights.gestationRangeText,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Litter Estimate (50%)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Diversity1, contentDescription = null, modifier = Modifier.size(15.dp), tint = PawSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Litter Estimate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = insights.estimatedLitterSize,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = insights.litterSubtitle,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Trimester Progress Bar
                Text(
                    text = insights.trimesterDescription,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (insights.percentComplete / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PawPrimary,
                    trackColor = PawPrimary.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tie: ${insights.tieDateDisplay}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Today: ${insights.todayDisplay} (${insights.percentComplete}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawPrimary
                    )
                    Text(
                        text = "Due: ${insights.dueDisplay}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: AI-Scheduled Milestones - Sleek, compact & expandable
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI-Scheduled Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${carePlan.milestones.size} Milestones",
                        style = MaterialTheme.typography.labelSmall,
                        color = PawSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                carePlan.milestones.forEach { milestone ->
                    val milestoneKey = milestone.id.ifEmpty { milestone.title }
                    val isExpanded = expandedMilestoneIds.contains(milestoneKey)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .animateContentSize()
                            .clickable {
                                expandedMilestoneIds = if (isExpanded) {
                                    expandedMilestoneIds - milestoneKey
                                } else {
                                    expandedMilestoneIds + milestoneKey
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (milestone.isCompleted) PawSuccess.copy(alpha = 0.15f) else PawPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (milestone.isCompleted) Icons.Default.CheckCircle else Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (milestone.isCompleted) PawSuccess else PawPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = milestone.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = milestone.dayWindow,
                                    fontSize = 11.sp,
                                    color = PawPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = milestone.description,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isExpanded) "Show less ▲" else "Read details ▼",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PawSecondary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.addMilestoneToCalendar(context, milestone) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Add to Calendar",
                                    tint = PawSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breed Nutrition Note Card - Sleek & expandable
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { isNutritionExpanded = !isNutritionExpanded }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = PawPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Breed Nutrition & Caloric Guidance",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = carePlan.breedNutritionNote,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = if (isNutritionExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isNutritionExpanded) "Show less ▲" else "Read full guidance ▼",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PawPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

