package com.example.pawnatal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pawnatal.theme.PawAlertRed
import com.example.pawnatal.theme.PawPrimary
import com.example.pawnatal.theme.PawSuccess

@Composable
fun RecordTempDialog(
    initialTemp: Double = 100.4,
    tempUnit: String = "FAHRENHEIT",
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    val isCelsius = tempUnit == com.example.pawnatal.data.model.TempUnit.CELSIUS.name
    
    // Store displayed temp in the selected unit
    val initialDisplayTemp = if (isCelsius) {
        Math.round(((initialTemp - 32.0) * 5.0 / 9.0) * 10.0) / 10.0
    } else {
        Math.round(initialTemp * 10.0) / 10.0
    }

    var currentTemp by remember { mutableDoubleStateOf(initialDisplayTemp) }
    var notes by remember { mutableStateOf("") }

    val dropThreshold = if (isCelsius) 37.2 else 99.0
    val isDrop = currentTemp < dropThreshold

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PawPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = PawPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Record Rectal Temp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clinical note
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDrop) PawAlertRed.copy(alpha = 0.1f) else PawSuccess.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isDrop)
                            "⚠️ Critical Drop: Temp below ${if (isCelsius) "37.2°C" else "99.0°F"} indicates whelping onset within 12–24 hours!"
                        else
                            "Normal safe baseline: ${if (isCelsius) "37.7°C – 38.4°C" else "99.8°F – 101.2°F"}. Labor drop expected around Day 60–62.",
                        fontSize = 12.sp,
                        color = if (isDrop) PawAlertRed else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Large Temp Display with Steppers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            val minVal = if (isCelsius) 35.0 else 96.0
                            currentTemp = (Math.round((currentTemp - 0.1) * 10.0) / 10.0).coerceAtLeast(minVal)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus 0.1")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", currentTemp),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDrop) PawAlertRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCelsius) "Degrees Celsius (°C)" else "Degrees Fahrenheit (°F)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            val maxVal = if (isCelsius) 41.0 else 105.0
                            currentTemp = (Math.round((currentTemp + 0.1) * 10.0) / 10.0).coerceAtMost(maxVal)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Plus 0.1")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observation Notes (Optional)") },
                    placeholder = { Text("e.g. Resting quietly, morning reading") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Button
                Button(
                    onClick = {
                        val savedFahrenheit = if (isCelsius) {
                            (currentTemp * 9.0 / 5.0) + 32.0
                        } else {
                            currentTemp
                        }
                        onConfirm(Math.round(savedFahrenheit * 10.0) / 10.0, notes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PawPrimary)
                ) {
                    Text(
                        text = "Confirm & Save Entry",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
