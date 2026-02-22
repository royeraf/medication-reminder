package com.medicationreminder.presentation.theme

import androidx.compose.ui.graphics.Color

// ─── Primary palette – Teal Expressive ───────────────────────────────────────
// M3E: Primary is the main brand personality color
val Primary = Color(0xFF007A74)          // Rich teal – brand authority
val PrimaryVariant = Color(0xFF005F5A)   // Darker teal for gradients
val PrimaryContainer = Color(0xFF9EF0EA) // Light teal container
val OnPrimary = Color(0xFFFFFFFF)        // White text on primary
val OnPrimaryContainer = Color(0xFF002020)

// ─── Secondary – Aqua/Mint (calm, health-forward) ────────────────────────────
val Secondary = Color(0xFF4ECDC4)        // Vibrant aqua
val SecondaryContainer = Color(0xFFCCF6F4)
val OnSecondary = Color(0xFF003735)
val OnSecondaryContainer = Color(0xFF004D40)

// ─── Tertiary – Warm Coral (emotional warmth, urgency) ───────────────────────
// M3E: Tertiary creates emotional contrast with the cool teal/aqua palette
val Tertiary = Color(0xFFFF6B6B)         // Vibrant coral – missed dose / urgency
val TertiaryContainer = Color(0xFFFFDAD6)
val OnTertiary = Color(0xFFFFFFFF)
val OnTertiaryContainer = Color(0xFF5C0000)

// ─── Accent – Lavender (settings, calm screens, AI insights) ─────────────────
val Quaternary = Color(0xFF9C8FFF)
val QuaternaryContainer = Color(0xFFE8E4FF)
val OnQuaternaryContainer = Color(0xFF1A0070)

// ─── Warm Amber (morning doses, highlights) ───────────────────────────────────
val Amber = Color(0xFFFFA000)
val AmberContainer = Color(0xFFFFECB3)
val OnAmberContainer = Color(0xFF5D4037)

// ─── Error ────────────────────────────────────────────────────────────────────
val Error = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// ─── Success ──────────────────────────────────────────────────────────────────
val Success = Color(0xFF00897B)
val SuccessContainer = Color(0xFFD0F2EE)
val OnSuccessContainer = Color(0xFF003731)

val SuccessDark = Color(0xFF6BD8CE)
val SuccessContainerDark = Color(0xFF00504A)
val OnSuccessContainerDark = Color(0xFFB2F0EC)

// ─── Neutral – Light theme ────────────────────────────────────────────────────
val Background = Color(0xFFF5FAFA)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFDBEBEB)
val SurfaceElevated = Color(0xFFFFFFFF)
val Outline = Color(0xFF6F9997)
val OutlineVariant = Color(0xFFBDD8D7)

val OnBackground = Color(0xFF171D1D)
val OnSurface = Color(0xFF171D1D)
val OnSurfaceVariant = Color(0xFF3A5050)

// ─── Neutral – Dark theme ─────────────────────────────────────────────────────
val BackgroundDark = Color(0xFF0E1514)
val SurfaceDark = Color(0xFF131C1B)
val SurfaceVariantDark = Color(0xFF3B4948)
val OutlineDark = Color(0xFF859F9E)
val OutlineVariantDark = Color(0xFF3B4948)

val OnBackgroundDark = Color(0xFFDCE5E4)
val OnSurfaceDark = Color(0xFFDCE5E4)
val OnSurfaceVariantDark = Color(0xFFB9CFCE)

// ─── M3 Expressive – Surface Container tokens (Light) ────────────────────────
val SurfaceBright         = Color(0xFFF6FCFB)
val SurfaceDim            = Color(0xFFD4DADA)
val SurfaceContainerLowest  = Color(0xFFFFFFFF)
val SurfaceContainerLow     = Color(0xFFEEF5F5)
val SurfaceContainer        = Color(0xFFE5EFEF)
val SurfaceContainerHigh    = Color(0xFFDDE9E8)
val SurfaceContainerHighest = Color(0xFFD5E3E2)
val SurfaceTint             = Primary

// ─── M3 Expressive – Surface Container tokens (Dark) ─────────────────────────
val SurfaceBrightDark         = Color(0xFF343B3A)
val SurfaceDimDark            = Color(0xFF060D0C)
val SurfaceContainerLowestDark  = Color(0xFF020807)
val SurfaceContainerLowDark     = Color(0xFF101716)
val SurfaceContainerDark        = Color(0xFF141B1A)
val SurfaceContainerHighDark    = Color(0xFF1E2625)
val SurfaceContainerHighestDark = Color(0xFF293130)

// ─── M3 Expressive – Dose Time emotional colors ───────────────────────────────
// Each time-of-day communicates via colour + shape for instant recognition
val DoseMorning     = Color(0xFFFFF3E0)   // Warm amber morning
val DoseMorningIcon = Color(0xFFE65100)
val DoseAfternoon     = Color(0xFFE0F7FA) // Light cyan afternoon
val DoseAfternoonIcon = Color(0xFF006064)
val DoseEvening     = Color(0xFFF3E5F5)   // Soft lavender evening
val DoseEveningIcon = Color(0xFF6A1B9A)
val DoseNight       = Color(0xFFE8EAF6)   // Deep indigo night
val DoseNightIcon   = Color(0xFF1A237E)

// ─── Neutral shades ───────────────────────────────────────────────────────────
val Gray50  = Color(0xFFF8F9FA)
val Gray100 = Color(0xFFF1F3F4)
val Gray200 = Color(0xFFE8EAED)
val Gray300 = Color(0xFFDADCE0)
val Gray400 = Color(0xFFBDC1C6)
val Gray500 = Color(0xFF9AA0A6)
val Gray600 = Color(0xFF80868B)
val Gray700 = Color(0xFF5F6368)
val Gray800 = Color(0xFF3C4043)
val Gray900 = Color(0xFF202124)
