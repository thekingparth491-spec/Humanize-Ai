package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimaryLight,
    onPrimary = NavyBackground,
    primaryContainer = VioletSurface,
    onPrimaryContainer = VioletPrimaryLight,
    secondary = CyanAccent,
    onSecondary = NavyBackground,
    secondaryContainer = NavySurfaceElevated,
    onSecondaryContainer = CyanAccentLight,
    tertiary = CyanAccentLight,
    background = NavyBackground,
    onBackground = NavyTextPrimary,
    surface = NavySurface,
    onSurface = NavyTextPrimary,
    surfaceVariant = NavySurfaceCard,
    onSurfaceVariant = NavyTextSecondary,
    outline = NavyBorder,
    outlineVariant = NavySurfaceElevated
)

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = LightSurface,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = VioletPrimaryDark,
    secondary = CyanAccentDark,
    onSecondary = LightSurface,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = CyanAccentDark,
    tertiary = VioletPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun HumanizeAiTheme(
    darkTheme: Boolean = true, // Default to Dark Navy per master prompt design
    dynamicColor: Boolean = false, // Keep consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
