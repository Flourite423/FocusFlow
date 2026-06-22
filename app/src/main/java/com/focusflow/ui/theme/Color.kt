package com.focusflow.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightPrimary = Color(0xFF4F46E5)    // Indigo 600
private val LightSecondary = Color(0xFF0D9488)  // Teal 600
private val LightTertiary = Color(0xFFF59E0B)   // Amber 500

private val DarkPrimary = Color(0xFF818CF8)     // Indigo 300
private val DarkSecondary = Color(0xFF2DD4BF)   // Teal 300
private val DarkTertiary = Color(0xFFFBBF24)    // Amber 400

@Composable
fun lightColorScheme(): ColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),  // Indigo 100
    onPrimaryContainer = Color(0xFF1E1B4B), // Indigo 950
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1), // Teal 100
    onSecondaryContainer = Color(0xFF042F2E), // Teal 950
    tertiary = LightTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7), // Amber 100
    onTertiaryContainer = Color(0xFF451A03), // Amber 950
    background = Color(0xFFFAFAFA), // Neutral 50
    onBackground = Color(0xFF18181B), // Zinc 950
    surface = Color.White,
    onSurface = Color(0xFF18181B), // Zinc 950
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF334155), // Slate 700
    error = Color(0xFFDC2626), // Red 600
    onError = Color.White,
    errorContainer = Color(0xFFFEF2F2), // Red 50
    onErrorContainer = Color(0xFF7F1D1D), // Red 900
    outline = Color(0xFF94A3B8), // Slate 400
    outlineVariant = Color(0xFFCBD5E1), // Slate 300
    shadow = Color(0xFF000000),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF18181B),
    inverseOnSurface = Color(0xFFFAFAFA),
    inversePrimary = DarkPrimary,
    surfaceTint = LightPrimary,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE4E4E7), // Zinc 200
    surfaceContainer = Color(0xFFFAFAFA), // Zinc 50
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerHigh = Color(0xFFF4F4F5), // Zinc 100
    surfaceContainerHighest = Color(0xFFE4E4E7), // Zinc 200
)

@Composable
fun darkColorScheme(): ColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF1E1B4B), // Indigo 950
    primaryContainer = Color(0xFF312E81), // Indigo 800
    onPrimaryContainer = Color(0xFFE0E7FF), // Indigo 100
    secondary = DarkSecondary,
    onSecondary = Color(0xFF042F2E), // Teal 950
    secondaryContainer = Color(0xFF115E59), // Teal 800
    onSecondaryContainer = Color(0xFFCCFBF1), // Teal 100
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF451A03), // Amber 950
    tertiaryContainer = Color(0xFF92400E), // Amber 700
    onTertiaryContainer = Color(0xFFFEF3C7), // Amber 100
    background = Color(0xFF09090B), // Zinc 950
    onBackground = Color(0xFFFAFAFA), // Zinc 50
    surface = Color(0xFF18181B), // Zinc 950
    onSurface = Color(0xFFFAFAFA), // Zinc 50
    surfaceVariant = Color(0xFF334155), // Slate 700
    onSurfaceVariant = Color(0xFFCBD5E1), // Slate 300
    error = Color(0xFFF87171), // Red 400
    onError = Color(0xFF7F1D1D), // Red 900
    errorContainer = Color(0xFF7F1D1D), // Red 900
    onErrorContainer = Color(0xFFFEF2F2), // Red 50
    outline = Color(0xFF64748B), // Slate 500
    outlineVariant = Color(0xFF475569), // Slate 600
    shadow = Color(0xFF000000),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFFAFAFA),
    inverseOnSurface = Color(0xFF18181B),
    inversePrimary = LightPrimary,
    surfaceTint = DarkPrimary,
    surfaceBright = Color(0xFF27272A), // Zinc 800
    surfaceDim = Color(0xFF09090B), // Zinc 950
    surfaceContainer = Color(0xFF18181B), // Zinc 950
    surfaceContainerLow = Color(0xFF09090B), // Zinc 950
    surfaceContainerLowest = Color(0xFF040405), // Zinc 950
    surfaceContainerHigh = Color(0xFF27272A), // Zinc 800
    surfaceContainerHighest = Color(0xFF3F3F46), // Zinc 700
)