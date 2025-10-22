package com.kiranawala.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = KiranaColors.Primary,
    onPrimary = Color.White,
    primaryContainer = KiranaColors.PrimaryLight,
    onPrimaryContainer = KiranaColors.PrimaryVariant,
    secondary = KiranaColors.Secondary,
    onSecondary = Color.White,
    error = KiranaColors.Error,
    onError = Color.White,
    background = KiranaColors.Background,
    onBackground = KiranaColors.OnBackground,
    surface = KiranaColors.Surface,
    onSurface = KiranaColors.OnSurface
)

private val DarkColors = darkColorScheme(
    primary = KiranaColors.PrimaryLight,
    onPrimary = KiranaColors.PrimaryVariant,
    primaryContainer = KiranaColors.Primary,
    onPrimaryContainer = Color.White,
    secondary = KiranaColors.Secondary,
    onSecondary = Color.Black,
    error = KiranaColors.Error,
    onError = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
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