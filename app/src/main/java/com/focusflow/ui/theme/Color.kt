package com.focusflow.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// === Fresh Gradient Palette — Notion/Linear inspired ===

// Primary: Soft indigo-violet
val Indigo400 = Color(0xFF818CF8)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)
val Indigo100 = Color(0xFFE0E7FF)
val Indigo50 = Color(0xFFEEF2FF)
val Indigo900 = Color(0xFF312E81)

// Accent: Warm coral
val Coral400 = Color(0xFFFB7185)
val Coral500 = Color(0xFFF43F5E)
val Coral100 = Color(0xFFFFE4E6)
val Coral900 = Color(0xFF881337)

// Success: Fresh mint
val Mint400 = Color(0xFF34D399)
val Mint500 = Color(0xFF10B981)
val Mint100 = Color(0xFFD1FAE5)
val Mint900 = Color(0xFF064E3B)

// Warning: Soft amber
val Amber400 = Color(0xFFFBBF24)
val Amber500 = Color(0xFFF59E0B)
val Amber100 = Color(0xFFFEF3C7)
val Amber900 = Color(0xFF78350F)

// Purple accent
val Violet400 = Color(0xFFA78BFA)
val Violet500 = Color(0xFF8B5CF6)
val Violet100 = Color(0xFFEDE9FE)
val Violet900 = Color(0xFF4C1D95)

// Sky blue
val Sky400 = Color(0xFF38BDF8)
val Sky500 = Color(0xFF0EA5E9)
val Sky100 = Color(0xFFE0F2FE)
val Sky900 = Color(0xFF0C4A6E)

// Neutral: Warm grays (slightly blue-tinted)
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val Slate950 = Color(0xFF020617)

// Gradient presets
val GradientIndigo = Brush.linearGradient(listOf(Indigo500, Violet500))
val GradientMint = Brush.linearGradient(listOf(Mint400, Sky400))
val GradientCoral = Brush.linearGradient(listOf(Coral400, Amber400))
val GradientSoft = Brush.linearGradient(listOf(Indigo50, Violet100))
val GradientSurface = Brush.verticalGradient(listOf(Color.White, Slate50))

// Light theme — airy, warm white background
val FocusFlowLightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo900,
    secondary = Violet500,
    onSecondary = Color.White,
    secondaryContainer = Violet100,
    onSecondaryContainer = Violet900,
    tertiary = Mint500,
    onTertiary = Color.White,
    tertiaryContainer = Mint100,
    onTertiaryContainer = Mint900,
    background = Color(0xFFFCFCFD),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate500,
    error = Coral500,
    onError = Color.White,
    errorContainer = Coral100,
    onErrorContainer = Coral900,
    outline = Slate200,
    outlineVariant = Slate100,
    scrim = Color.Black.copy(alpha = 0.3f),
    inverseSurface = Slate900,
    inverseOnSurface = Slate50,
    inversePrimary = Indigo400,
    surfaceTint = Indigo500,
)

// Dark theme — deep navy, soft glows
val FocusFlowDarkColorScheme = darkColorScheme(
    primary = Indigo400,
    onPrimary = Indigo900,
    primaryContainer = Indigo900,
    onPrimaryContainer = Indigo100,
    secondary = Violet400,
    onSecondary = Violet900,
    secondaryContainer = Violet900,
    onSecondaryContainer = Violet100,
    tertiary = Mint400,
    onTertiary = Mint900,
    tertiaryContainer = Mint900,
    onTertiaryContainer = Mint100,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    error = Coral400,
    onError = Coral900,
    errorContainer = Coral900,
    onErrorContainer = Coral100,
    outline = Slate700,
    outlineVariant = Slate800,
    scrim = Color.Black.copy(alpha = 0.5f),
    inverseSurface = Slate100,
    inverseOnSurface = Slate900,
    inversePrimary = Indigo600,
    surfaceTint = Indigo400,
)

// Functional color extensions
object FocusFlowColors {
    // Functional areas
    val planColor get() = Indigo500
    val timerColor get() = Mint500
    val streakColor get() = Amber500
    val reviewColor get() = Violet500
    val settingsColor get() = Slate500

    // Light heatmap
    val heatmapEmpty = Color(0xFFF1F5F9)
    val heatmapLow = Color(0xFFC7D2FE)
    val heatmapMedium = Color(0xFFA5B4FC)
    val heatmapHigh = Color(0xFF818CF8)
    val heatmapMax = Color(0xFF6366F1)

    // Dark heatmap
    val heatmapEmptyDark = Color(0xFF1E293B)
    val heatmapLowDark = Color(0xFF312E81)
    val heatmapMediumDark = Color(0xFF3730A3)
    val heatmapHighDark = Color(0xFF4338CA)
    val heatmapMaxDark = Color(0xFF6366F1)
}
