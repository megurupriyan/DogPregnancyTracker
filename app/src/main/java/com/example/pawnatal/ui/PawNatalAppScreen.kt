package com.example.pawnatal.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pawnatal.ui.components.AppNavTab
import com.example.pawnatal.ui.components.PawBottomNavBar
import com.example.pawnatal.ui.components.PawTopAppBar
import com.example.pawnatal.ui.home.HomeDashboardScreen
import com.example.pawnatal.ui.schedule.ScheduleAndMedicalScreen
import com.example.pawnatal.ui.settings.SettingsScreen
import com.example.pawnatal.ui.setup.SetupAndAiPlanScreen
import com.example.pawnatal.ui.tips.TipsAndWhelpingScreen
import com.example.pawnatal.ui.weekly.WeeklyGuideScreen
import com.example.pawnatal.ui.viewmodel.PregnancyViewModel
import com.example.pawnatal.ui.viewmodel.SettingsViewModel

@Composable
fun PawNatalAppScreen(
    pregnancyViewModel: PregnancyViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    // Default entry point is PLAN (Pregnancy Care & AI Plan screen)
    var currentTab by remember { mutableStateOf(AppNavTab.PLAN) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    val damProfile by pregnancyViewModel.damProfile.collectAsState()
    val carePlan by pregnancyViewModel.carePlan.collectAsState()

    if (isSettingsOpen) {
        SettingsScreen(
            settingsViewModel = settingsViewModel,
            pregnancyViewModel = pregnancyViewModel,
            onBackClick = { isSettingsOpen = false },
            modifier = modifier
        )
    } else {
        Scaffold(
            topBar = {
                PawTopAppBar(
                    dam = damProfile,
                    insights = carePlan.insights,
                    onSettingsClick = { isSettingsOpen = true }
                )
            },
            bottomBar = {
                PawBottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Crossfade(
                targetState = currentTab,
                modifier = Modifier.padding(paddingValues),
                label = "TabCrossfade"
            ) { tab ->
                when (tab) {
                    AppNavTab.PLAN -> SetupAndAiPlanScreen(
                        viewModel = pregnancyViewModel,
                        onNavigateToSettings = { isSettingsOpen = true }
                    )
                    AppNavTab.HOME -> HomeDashboardScreen(
                        viewModel = pregnancyViewModel
                    )
                    AppNavTab.WEEKLY -> WeeklyGuideScreen(
                        viewModel = pregnancyViewModel
                    )
                    AppNavTab.SCHEDULE -> ScheduleAndMedicalScreen(
                        viewModel = pregnancyViewModel
                    )
                    AppNavTab.TIPS -> TipsAndWhelpingScreen(
                        viewModel = pregnancyViewModel
                    )
                }
            }
        }
    }
}
