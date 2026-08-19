package com.medtrackpro.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val DarkColorScheme = darkColorScheme(
    primary = MedTrackPrimary,
    onPrimary = Color.White,
    primaryContainer = MedTrackHeroIcon,
    onPrimaryContainer = MedTrackPrimaryDark,
    secondary = MedTrackHeroStart,
    onSecondary = MedTrackPrimaryDark,
    secondaryContainer = MedTrackHeroEnd,
    onSecondaryContainer = MedTrackPrimaryDark,
    tertiary = MedTrackMuted,
    background = MedTrackBackground,
    onBackground = MedTrackPrimaryDark,
    surface = MedTrackSurface,
    onSurface = MedTrackPrimaryDark,
    surfaceVariant = MedTrackHeroStart,
    onSurfaceVariant = MedTrackMuted,
    outline = MedTrackOutline,
    error = MedTrackError,
    onError = Color.White
)
private val LightColorScheme = lightColorScheme(
    primary = MedTrackPrimary,
    onPrimary = Color.White,
    primaryContainer = MedTrackHeroIcon,
    onPrimaryContainer = MedTrackPrimaryDark,
    secondary = MedTrackHeroStart,
    onSecondary = MedTrackPrimaryDark,
    secondaryContainer = MedTrackHeroEnd,
    onSecondaryContainer = MedTrackPrimaryDark,
    tertiary = MedTrackMuted,
    background = MedTrackBackground,
    onBackground = MedTrackPrimaryDark,
    surface = MedTrackSurface,
    onSurface = MedTrackPrimaryDark,
    surfaceVariant = MedTrackHeroStart,
    onSurfaceVariant = MedTrackMuted,
    outline = MedTrackOutline,
    error = MedTrackError,
    onError = Color.White
)

@Composable
fun MedTrackTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
