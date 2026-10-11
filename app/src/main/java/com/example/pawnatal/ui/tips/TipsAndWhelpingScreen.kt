package com.example.pawnatal.ui.tips

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
import com.example.pawnatal.theme.*
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel

@Composable
fun TipsAndWhelpingScreen(
    viewModel: PregnancyViewModel,
    modifier: Modifier = Modifier
) {
    val carePlan by viewModel.carePlan.collectAsState()
    val damProfile by viewModel.damProfile.collectAsState()
    val emergencyInfo = carePlan.emergencyInfo
    val whelpingKit = carePlan.whelpingKit
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    var showEditVetDialog by remember { mutableStateOf(false) }

    var editDoctorName by remember(damProfile.vetDoctorName) { mutableStateOf(damProfile.vetDoctorName) }
    var editClinicName by remember(damProfile.vetClinicName) { mutableStateOf(damProfile.vetClinicName) }
    var editPhone by remember(damProfile.vetClinicPhone) { mutableStateOf(damProfile.vetClinicPhone) }
    var editAddress by remember(damProfile.vetClinicAddress) { mutableStateOf(damProfile.vetClinicAddress) }
    var editEmergencyPhone by remember(damProfile.emergencyPhone) { mutableStateOf(damProfile.emergencyPhone) }

    if (showEditVetDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showEditVetDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Veterinary Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showEditVetDialog = false }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editDoctorName,
                        onValueChange = { editDoctorName = it },
                        label = { Text("Attending Doctor") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editClinicName,
                        onValueChange = { editClinicName = it },
                        label = { Text("Clinic Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editEmergencyPhone,
                        onValueChange = { editEmergencyPhone = it },
                        label = { Text("24/7 Emergency Hotline") },
                        placeholder = { Text("+1 (555) 999-9999") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("General Clinic Phone") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Clinic Address") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateVetDetails(
                                doctorName = editDoctorName,
                                clinicName = editClinicName,
                                phone = editPhone,
                                address = editAddress,
                                emergencyPhone = editEmergencyPhone
                            )
                            showEditVetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PawPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Save Veterinary Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val readyCount = whelpingKit.count { it.isReady }
    val readyPercentage = if (whelpingKit.isNotEmpty()) (readyCount * 100) / whelpingKit.size else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Delivery Nest Header Banner - Dynamically calculated
        Card(
            colors = CardDefaults.cardColors(containerColor = PawPrimaryContainer),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PawPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${damProfile.name}'s Delivery Nest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PawOnPrimaryContainer
                    )
                    val weeksRemaining = ((carePlan.insights.daysRemaining + 6) / 7).coerceAtLeast(0)
                    Text(
                        text = "$weeksRemaining weeks to due date (${carePlan.insights.dueDisplay}). Calm environment, ready hands.",
                        fontSize = 12.sp,
                        color = PawOnPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 24/7 Rapid Response Emergency Vet Card
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PawAlertRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Emergency, contentDescription = null, tint = PawAlertRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "24/7 Rapid Response",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawAlertRed
                            )
                            Text(
                                text = emergencyInfo.clinicName.ifBlank { "Veterinary Emergency Clinic" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val hasPhone = emergencyInfo.hotlinePhone.isNotBlank()
                    Button(
                        onClick = {
                            if (hasPhone) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${emergencyInfo.hotlinePhone}"))
                                context.startActivity(intent)
                            } else {
                                showEditVetDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasPhone) PawAlertRed else PawPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(if (hasPhone) Icons.Default.Call else Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (hasPhone) "Call Now" else "Set Phone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (emergencyInfo.hotlinePhone.isNotBlank())
                            "${emergencyInfo.hotlinePhone} • ${emergencyInfo.distanceText}"
                        else
                            "No emergency phone saved • Tap to configure",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { showEditVetDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Edit Contact", fontSize = 11.sp, color = PawPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // When to Dial Immediately warning box
                Card(
                    colors = CardDefaults.cardColors(containerColor = PawAlertRed.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = PawAlertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "When to Dial Immediately",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PawAlertRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        emergencyInfo.immediateDialTriggers.forEach { trigger ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = PawAlertRed, fontWeight = FontWeight.Bold)
                                Text(
                                    text = trigger,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Whelping Kit Readiness Checklist
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
                            text = "Whelping Kit Readiness",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$readyCount of ${whelpingKit.size} items ready • Goal: 100% by Day 56",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(PawSecondary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$readyPercentage% Ready",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PawSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { readyPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PawSecondary,
                    trackColor = PawSecondary.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                whelpingKit.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isReady) PawSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.toggleWhelpingKitItem(item.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isReady,
                                onCheckedChange = { viewModel.toggleWhelpingKitItem(item.id) },
                                colors = CheckboxDefaults.colors(checkedColor = PawSuccess)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (item.isReady) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.purpose,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (item.isReady) "Ready" else "Needed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isReady) PawSuccess else PawPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nutrition & Behavioral Guidance Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = null, tint = PawPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nutrition & Growth Formula",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Switch to High-Density Growth Formula",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PawPrimary
                )
                Text(
                    text = "From Day 42 onward, puppies grow at an exponential rate, reducing maternal stomach capacity. Transition gradually over 5 days to a veterinary puppy formula containing high protein, DHA, and caloric density.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Portion Strategy: Feed 3 to 4 smaller, frequent meals throughout the day rather than two heavy bowls.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = PawSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Decoding the Nesting Frenzy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Furious blanket digging, panting, and hiding behind sofas are natural hormonal impulses triggered by dropping progesterone. Introduce the whelping box with your personal scent to establish a calming sanctuary. Maintain ambient room temp at 75°F–80°F.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Myth Busted & Calcium Warning Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = PawAlertRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = emergencyInfo.mythBusterTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PawAlertRed
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = emergencyInfo.mythBusterContent,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
