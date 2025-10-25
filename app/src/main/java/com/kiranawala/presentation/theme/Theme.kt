package com.kiranawala.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light Mode Color Scheme
 * Optimized for daytime viewing with warm, comfortable colors
 */
private val LightColors = lightColorScheme(
    primary = KiranaColors.PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = KiranaColors.PrimaryContainer,
    onPrimaryContainer = KiranaColors.PrimaryVariant,
    secondary = KiranaColors.SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF9E6),
    onSecondaryContainer = KiranaColors.SecondaryVariant,
    error = KiranaColors.Error,
    onError = Color.White,
    background = KiranaColors.BackgroundLight,
    onBackground = KiranaColors.OnBackgroundLight,
    surface = KiranaColors.SurfaceLight,
    onSurface = KiranaColors.OnSurfaceLight,
    onSurfaceVariant = KiranaColors.OnSurfaceVariantLight,
    outline = KiranaColors.DividerLight,
    outlineVariant = KiranaColors.Gray200
)

/**
 * Dark Mode Color Scheme  
 * OLED-optimized true black for battery savings and reduced eye strain
 */
private val DarkColors = darkColorScheme(
    primary = KiranaColors.PrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1A3A1A),
    onPrimaryContainer = Color.White,
    secondary = KiranaColors.SecondaryDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3A3316),
    onSecondaryContainer = Color.White,
    error = KiranaColors.Error,
    onError = Color.White,
    background = KiranaColors.BackgroundDark,
    onBackground = KiranaColors.OnBackgroundDark,
    surface = KiranaColors.SurfaceDark,
    onSurface = KiranaColors.OnSurfaceDark,
    onSurfaceVariant = KiranaColors.OnSurfaceVariantDark,
    outline = KiranaColors.DividerDark,
    outlineVariant = Color(0xFF1A1A1A)
)

@Composable
fun KiranaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colors,
        typography = KiranaTypography,
        content = content
    )
}