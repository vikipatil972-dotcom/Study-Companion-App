package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CosmicColorScheme = darkColorScheme(
    primary = CosmicPrimary,
    secondary = CosmicSecondary,
    tertiary = CosmicTertiary,
    background = CosmicBackground,
    surface = CosmicSurface,
    onPrimary = CosmicOnPrimary,
    onSecondary = CosmicOnSecondary,
    onBackground = CosmicOnBackground,
    onSurface = CosmicOnSurface,
    error = AccentRed
)

private val StudyColorScheme = lightColorScheme(
    primary = StudyPrimary,
    secondary = StudySecondary,
    tertiary = StudyTertiary,
    background = StudyBackground,
    surface = StudySurface,
    onPrimary = StudyOnPrimary,
    onSecondary = StudyOnSecondary,
    onBackground = StudyOnBackground,
    onSurface = StudyOnSurface,
    error = AccentRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We prefer the Dark (Cosmic) theme for focus, but respect user preferences or keep it Dark-first.
    // Let's toggle between Cosmic and Study color schemes based on preference.
    val colorScheme = if (darkTheme) CosmicColorScheme else StudyColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
