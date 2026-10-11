package com.example.pawnatal.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawnatal.data.model.AiProvider
import com.example.pawnatal.theme.*
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel
import com.example.pawnatal.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    pregnancyViewModel: PregnancyViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by settingsViewModel.settings.collectAsState()
    val testState by settingsViewModel.connectionTestState.collectAsState()

    var showPassword by remember { mutableStateOf(false) }
    var selectedProvider by remember(settings.activeProvider) { mutableStateOf(settings.activeProvider) }
    var apiKeyInput by remember(settings.activeProvider, settings) { mutableStateOf(settings.getActiveApiKey()) }
    var modelInput by remember(settings.customModelId) { mutableStateOf(settings.customModelId) }
    var baseUrlInput by remember(settings.customBaseUrl) { mutableStateOf(settings.customBaseUrl) }

    var expandedProviderMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI & App Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // AI Configuration Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PawPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LLM Engine & Model Selection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose from OpenAI, Gemini (Free), Groq, or custom endpoints",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Provider Dropdown
                    Text(
                        text = "AI Provider",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedProviderMenu,
                        onExpandedChange = { expandedProviderMenu = !expandedProviderMenu }
                    ) {
                        OutlinedTextField(
                            value = settings.provider.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProviderMenu) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedProviderMenu,
                            onDismissRequest = { expandedProviderMenu = false }
                        ) {
                            AiProvider.values().forEach { provider ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(provider.displayName, fontWeight = FontWeight.SemiBold)
                                            Text("Default Model: ${provider.defaultModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedProvider = provider.name
                                        settingsViewModel.updateProvider(provider.name)
                                        expandedProviderMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // API Key Field
                    Text(
                        text = "${settings.provider.displayName} API Key",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            settingsViewModel.updateApiKey(selectedProvider, it)
                        },
                        placeholder = { Text("Paste your API key here") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Hide key" else "Show key"
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Model ID (e.g. "sol" or custom)
                    Text(
                        text = "Model ID (e.g. sol, gpt-4o-mini, gemini-2.5-flash)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = {
                            modelInput = it
                            settingsViewModel.updateCustomModel(it)
                        },
                        placeholder = { Text("e.g. sol") },
                        supportingText = { Text("Currently active model: ${settings.activeModelId}") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Base URL (for custom endpoints / proxies)
                    if (settings.provider == AiProvider.CUSTOM) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Custom Base URL",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = baseUrlInput,
                            onValueChange = {
                                baseUrlInput = it
                                settingsViewModel.updateCustomBaseUrl(it)
                            },
                            placeholder = { Text("http://10.0.2.2:11434/v1/chat/completions") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Test Connection Button
                    OutlinedButton(
                        onClick = { settingsViewModel.testConnection() },
                        enabled = !testState.isTesting,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (testState.isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing Connection...")
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Model Connection")
                        }
                    }

                    AnimatedVisibility(visible = testState.resultMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (testState.isSuccess) PawSuccess.copy(alpha = 0.12f) else PawAlertRed.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (testState.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (testState.isSuccess) PawSuccess else PawAlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = testState.resultMessage.orEmpty(),
                                    fontSize = 12.sp,
                                    color = if (testState.isSuccess) PawSuccess else PawAlertRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Customizations Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Customization & Units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Temperature Unit Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Temperature Unit", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Applied across charts, logs & alerts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isF = settings.tempUnit == com.example.pawnatal.data.model.TempUnit.FAHRENHEIT.name
                            FilterChip(
                                selected = isF,
                                onClick = { settingsViewModel.setTempUnit(com.example.pawnatal.data.model.TempUnit.FAHRENHEIT) },
                                label = { Text("°F") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PawPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = !isF,
                                onClick = { settingsViewModel.setTempUnit(com.example.pawnatal.data.model.TempUnit.CELSIUS) },
                                label = { Text("°C") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PawPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Weight Unit Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weight Unit", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Applied across dam profile & vitals", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isLbs = settings.weightUnit == com.example.pawnatal.data.model.WeightUnit.LBS.name
                            FilterChip(
                                selected = isLbs,
                                onClick = { settingsViewModel.setWeightUnit(com.example.pawnatal.data.model.WeightUnit.LBS) },
                                label = { Text("lbs") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PawPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = !isLbs,
                                onClick = { settingsViewModel.setWeightUnit(com.example.pawnatal.data.model.WeightUnit.KG) },
                                label = { Text("kg") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PawPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Notification Preferences
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Care Reminders & Alerts", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Daily routines (08:30), evening temp check (20:00), and < 37.2°C alerts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Active even if app is closed or killed", fontSize = 11.sp, color = PawPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { settingsViewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PawPrimary, checkedTrackColor = PawPrimaryContainer)
                        )
                    }

                    if (settings.notificationsEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        OutlinedButton(
                            onClick = {
                                com.example.pawnatal.data.notification.AlarmScheduler.triggerTestNotificationNow(ctx)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = PawPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Notification (Verify Background Delivery)", fontSize = 12.sp, color = PawPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sample Profile Presets
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Quick Dam Profile Presets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Switch between pre-calibrated sample profiles for demonstration",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pregnancyViewModel.loadPreset("GOLDEN_RETRIEVER") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Bella (Golden)", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { pregnancyViewModel.loadPreset("FRENCH_BULLDOG") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Luna (Frenchie)", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { pregnancyViewModel.loadPreset("LABRADOR") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Daisy (Lab)", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About / Version Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PawNatal Pet Pregnancy & Gestation Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Engineered with Modern Native Android, Jetpack Compose, Material 3 & Clinical Obstetric AI Harness.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
