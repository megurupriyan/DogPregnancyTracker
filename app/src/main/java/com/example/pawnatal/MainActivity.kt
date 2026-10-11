package com.example.pawnatal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pawnatal.data.notification.DateChangeReceiver
import com.example.pawnatal.data.repository.PregnancyRepository
import com.example.pawnatal.data.repository.SettingsRepository
import com.example.pawnatal.theme.PawNatalTheme
import com.example.pawnatal.ui.PawNatalAppScreen
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel
import com.example.pawnatal.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private val pregnancyRepository by lazy { PregnancyRepository(applicationContext) }

    private val pregnancyViewModel: PregnancyViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PregnancyViewModel(pregnancyRepository, settingsRepository) as T
            }
        }
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(settingsRepository) as T
            }
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                com.example.pawnatal.data.notification.AlarmScheduler.scheduleDailyReminders(applicationContext)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request runtime notification permission on Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Wire listener for system date and time updates
        DateChangeReceiver.onDateChangedListener = {
            pregnancyViewModel.refreshForToday()
        }

        setContent {
            PawNatalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PawNatalAppScreen(
                        pregnancyViewModel = pregnancyViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure pregnancy timeline is fresh and reactive to daily date changes
        pregnancyViewModel.refreshForToday()
    }

    override fun onDestroy() {
        super.onDestroy()
        DateChangeReceiver.onDateChangedListener = null
    }
}
