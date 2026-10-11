package com.example.pawnatal.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PawPrimary,
    onPrimary = PawOnPrimary,
    primaryContainer = PawPrimaryContainer,
    onPrimaryContainer = PawOnPrimaryContainer,
    secondary = PawSecondary,
    onSecondary = PawOnSecondary,
    secondaryContainer = PawSecondaryContainer,
    onSecondaryContainer = PawOnSecondaryContainer,
    tertiary = PawTertiary,
    onTertiary = PawOnTertiary,
    tertiaryContainer = PawTertiaryContainer,
    onTertiaryContainer = PawOnTertiaryContainer,
    background = PawBackground,
    onBackground = PawOnBackground,
    surface = PawSurface,
    onSurface = PawOnSurface,
    surfaceVariant = PawSurfaceVariant,
    onSurfaceVariant = PawOnSurfaceVariant,
    outline = PawOutline,
    outlineVariant = PawOutlineVariant,
    error = PawAlertRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PawPrimary,
    onPrimary = PawOnPrimary,
    primaryContainer = PawOnPrimaryContainer,
    onPrimaryContainer = PawPrimaryContainer,
    secondary = PawSecondary,
    onSecondary = PawOnSecondary,
    secondaryContainer = PawOnSecondaryContainer,
    onSecondaryContainer = PawSecondaryContainer,
    tertiary = PawTertiary,
    onTertiary = PawOnTertiary,
    tertiaryContainer = PawOnTertiaryContainer,
    onTertiaryContainer = PawTertiaryContainer,
    background = PawDarkBackground,
    onBackground = PawDarkOnBackground,
    surface = PawDarkSurface,
    onSurface = PawDarkOnSurface,
    surfaceVariant = PawDarkSurfaceVariant,
    onSurfaceVariant = PawDarkOnSurface,
    outline = PawDarkOutline,
    error = PawAlertRed
)

@Composable
fun PawNatalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
