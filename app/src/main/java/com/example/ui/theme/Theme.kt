package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElegantDarkPrimary,
    secondary = ElegantDarkSecondary,
    tertiary = ElegantDarkTertiary,
    background = ElegantDarkBg,
    surface = ElegantDarkSurface,
    onPrimary = ElegantDarkOnPrimary,
    onSecondary = ElegantDarkOnSecondary,
    onTertiary = ElegantDarkOnTertiary,
    onBackground = ElegantDarkTextPrimary,
    onSurface = ElegantDarkTextPrimary,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantDarkTextSecondary,
    outline = ElegantDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ElegantLightPrimary,
    secondary = ElegantLightSecondary,
    tertiary = ElegantLightTertiary,
    background = ElegantLightBg,
    surface = ElegantLightSurface,
    onPrimary = ElegantLightOnPrimary,
    onSecondary = ElegantLightOnSecondary,
    onTertiary = ElegantLightOnTertiary,
    onBackground = ElegantLightTextPrimary,
    onSurface = ElegantLightTextPrimary,
    surfaceVariant = ElegantLightSurfaceVariant,
    onSurfaceVariant = ElegantLightTextSecondary,
    outline = ElegantLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to light theme as requested!
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce our beautiful cohesive theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
