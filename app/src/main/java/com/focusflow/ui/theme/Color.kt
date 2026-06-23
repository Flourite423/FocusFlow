package com.focusflow.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Flat Colorful Palette — vibrant but not overwhelming
// Each functional area has its own color identity

// Primary colors
val Blue500 = Color(0xFF3B82F6)
val Blue600 = Color(0xFF2563EB)
val Blue100 = Color(0xFFDBEAFE)
val Blue900 = Color(0xFF1E3A5F)

// Accent colors for functional areas
val Green500 = Color(0xFF22C55E)
val Green600 = Color(0xFF16A34A)
val Green100 = Color(0xFFDCFCE7)
val Green900 = Color(0xFF14532D)

val Orange500 = Color(0xFFF97316)
val Orange600 = Color(0xFFEA580C)
val Orange100 = Color(0xFFFED7AA)
val Orange900 = Color(0xFF7C2D12)

val Purple500 = Color(0xFFA855F7)
val Purple600 = Color(0xFF9333EA)
val Purple100 = Color(0xFFF3E8FF)
val Purple900 = Color(0xFF581C87)

val Pink500 = Color(0xFFEC4899)
val Pink600 = Color(0xFFDB2777)
val Pink100 = Color(0xFFFCE7F3)
val Pink900 = Color(0xFF831843)

val Teal500 = Color(0xFF14B8A6)
val Teal600 = Color(0xFF0D9488)
val Teal100 = Color(0xFFCCFBF1)
val Teal900 = Color(0xFF134E4A)

// Neutral
val Gray50 = Color(0xFFF8FAFC)
val Gray100 = Color(0xFFF1F5F9)
val Gray200 = Color(0xFFE2E8F0)
val Gray300 = Color(0xFFCBD5E1)
val Gray400 = Color(0xFF94A3B8)
val Gray500 = Color(0xFF64748B)
val Gray600 = Color(0xFF475569)
val Gray700 = Color(0xFF334155)
val Gray800 = Color(0xFF1E293B)
val Gray900 = Color(0xFF0F172A)
val Gray950 = Color(0xFF020617)

// Light theme — white background, colorful cards with left accent bars
val FocusFlowLightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = Teal600,
    onSecondary = Color.White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal900,
    tertiary = Orange500,
    onTertiary = Color.White,
    tertiaryContainer = Orange100,
    onTertiaryContainer = Orange900,
    background = Color.White,
    onBackground = Gray900,
    surface = Gray50,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    outline = Gray300,
    outlineVariant = Gray200,
    scrim = Color.Black,
    inverseSurface = Gray900,
    inverseOnSurface = Gray50,
    inversePrimary = Blue500,
    surfaceTint = Blue600,
)

// Dark theme — dark gray background, vibrant accents
val FocusFlowDarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue900,
    onPrimaryContainer = Blue100,
    secondary = Teal500,
    onSecondary = Color.White,
    secondaryContainer = Teal900,
    onSecondaryContainer = Teal100,
    tertiary = Orange500,
    onTertiary = Color.White,
    tertiaryContainer = Orange900,
    onTertiaryContainer = Orange100,
    background = Gray950,
    onBackground = Gray100,
    surface = Gray900,
    onSurface = Gray100,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray400,
    error = Color(0xFFF87171),
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    outline = Gray600,
    outlineVariant = Gray700,
    scrim = Color.Black,
    inverseSurface = Gray100,
    inverseOnSurface = Gray900,
    inversePrimary = Blue600,
    surfaceTint = Blue500,
)

// Functional color extensions for cards/indicators
object FocusFlowColors {
    // Light theme functional colors
    val planColor = Blue500
    val timerColor = Green500
    val reviewColor = Purple500
    val streakColor = Orange500
    val settingsColor = Gray500
    val taskColor = Teal500
    val milestoneColor = Pink500

    // Heatmap gradient
    val heatmapEmpty = Color(0xFFF1F5F9)
    val heatmapLow = Color(0xFFBBF7D0)
    val heatmapMedium = Color(0xFF86EFAC)
    val heatmapHigh = Color(0xFF4ADE80)
    val heatmapMax = Color(0xFF22C55E)

    // Dark heatmap
    val heatmapEmptyDark = Color(0xFF1E293B)
    val heatmapLowDark = Color(0xFF14532D)
    val heatmapMediumDark = Color(0xFF166534)
    val heatmapHighDark = Color(0xFF15803D)
    val heatmapMaxDark = Color(0xFF22C55E)
}
