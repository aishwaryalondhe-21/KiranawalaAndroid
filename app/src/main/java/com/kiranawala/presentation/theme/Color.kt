package com.kiranawala.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Modern 2025 Color Palette - Inspired by Fresh/Health Psychology
 * Following Material 3 Design System with custom brand colors
 */
object KiranaColors {
    // Primary Colors
    val PrimaryLight = Color(0xFF2E7D32)  // Light mode primary
    val PrimaryDark = Color(0xFF34C759)   // Dark mode primary
    val Primary = Color(0xFF34C759)       // Default primary (backward compat)
    val PrimaryVariant = Color(0xFF00A341)
    val PrimaryContainer = Color(0xFFE8F5E9)
    
    // Secondary Colors
    val SecondaryLight = Color(0xFFFFB300)  // Light mode secondary
    val SecondaryDark = Color(0xFFFDD835)   // Dark mode secondary
    val Secondary = Color(0xFFFFB300)      // Default secondary (backward compat)
    val SecondaryVariant = Color(0xFFFF8F00)
    
    // Light Mode Colors
    val BackgroundLight = Color(0xFFF9FAFB)  // Light background
    val SurfaceLight = Color(0xFFFFFFFF)     // Light card
    val OnBackgroundLight = Color(0xFF1B1B1B) // Light text primary
    val OnSurfaceLight = Color(0xFF1B1B1B)
    val OnSurfaceVariantLight = Color(0xFF606060) // Light text secondary
    val DividerLight = Color(0xFFE0E0E0)     // Light divider
    
    // Dark Mode Colors
    val BackgroundDark = Color(0xFF0C0F0A)   // Dark background
    val SurfaceDark = Color(0xFF162016)      // Dark card
    val OnBackgroundDark = Color(0xFFFFFFFF) // Dark text primary
    val OnSurfaceDark = Color(0xFFFFFFFF)
    val OnSurfaceVariantDark = Color(0xFFB0B0B0) // Dark text secondary
    val DividerDark = Color(0xFF2A2A2A)      // Dark divider
    
    // Status Colors
    val Success = Color(0xFF34C759)
    val Error = Color(0xFFD32F2F)
    val Warning = Color(0xFFFFB300)
    val Info = Color(0xFF2196F3)
    
    // Delivery Time Color Coding
    val SpeedFast = Color(0xFF34C759)   // Green - Under 30 min
    val SpeedMedium = Color(0xFFFDD835) // Amber - 30-45 min
    val SpeedSlow = Color(0xFFFF6B6B)   // Red - 45+ min
    
    // Neutrals/Grays
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)
    
    // Glassmorphism Colors
    val GlassLight = Color(0xB3FFFFFF)  // 70% opacity white
    val GlassDark = Color(0xB31C1C1C)  // 70% opacity dark surface
    
    // Overlay Colors
    val Scrim = Color(0x99000000)  // 60% black overlay
    val ShimmerBase = Color(0xFFE0E0E0)
    val ShimmerHighlight = Color(0xFFF5F5F5)
}
