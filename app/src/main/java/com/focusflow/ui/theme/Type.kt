package com.focusflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intent.ParagraphStyle
import androidx.compose.ui.text.intent.SpanStyle
import androidx.compose.ui.unit.sp

val FocusFlowTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(64)),
        letterSpacing = SpanStyle.LetterSpacing(sp(-0.25))
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(52)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0))
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(44)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0))
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(40)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0))
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(36)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0))
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(32)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0))
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(28)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0))
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(24)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.15))
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(20)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.1))
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(24)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.5))
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(20)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.25))
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = ParagraphLineHeight(sp(16)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.4))
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(20)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.1))
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(16)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.5))
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = ParagraphStyle.LineHeight(sp(16)),
        letterSpacing = SpanStyle.LetterSpacing(sp(0.5))
    )
)