package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Primary Violet / Indigo
val VioletPrimary = Color(0xFF8B5CF6)
val VioletPrimaryDark = Color(0xFF6D28D9)
val VioletPrimaryLight = Color(0xFFA78BFA)
val VioletSurface = Color(0xFF1E1B4B)

// Accent Cyan / Sky Blue
val CyanAccent = Color(0xFF06B6D4)
val CyanAccentLight = Color(0xFF38BDF8)
val CyanAccentDark = Color(0xFF0284C7)

// Status & Indicators
val AiScoreRed = Color(0xFFEF4444)
val AiScoreOrange = Color(0xFFF97316)
val MixedScoreAmber = Color(0xFFF59E0B)
val HumanScoreEmerald = Color(0xFF10B981)
val HumanScoreTeal = Color(0xFF14B8A6)

// Dark Navy Palette
val NavyBackground = Color(0xFF0A0F1D)
val NavySurface = Color(0xFF111827)
val NavySurfaceCard = Color(0xFF161F33)
val NavySurfaceElevated = Color(0xFF1E293B)
val NavyBorder = Color(0xFF2E384D)
val NavyTextPrimary = Color(0xFFF1F5F9)
val NavyTextSecondary = Color(0xFF94A3B8)
val NavyTextMuted = Color(0xFF64748B)

// Light Palette
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceCard = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF06B6D4))
)

val HeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF06B6D4))
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x338B5CF6), Color(0x1A06B6D4), Color(0x000A0F1D))
)

val AiScoreGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFEF4444), Color(0xFFF97316))
)

val HumanScoreGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
)
