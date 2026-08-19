package com.phantasia.music.ui

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Phantasia Cyber-Obsidian & Neon Aura Palette ─────────────────────────────
object PhantasiaColors {
    // Core obsidian glass backgrounds
    val Midnight   = Color(0xFF08090E)   // deep obsidian base
    val Midnight2  = Color(0xFF0D1018)   // dark obsidian surface
    val PurpleInk  = Color(0xFF101422)   // navigation/topbar surface
    val Surface    = Color(0xCC131826)   // frosted card glass
    val SurfaceHigh= Color(0xE61B2236)   // elevated floating cards
    val SurfaceCard= Color(0x99161C2C)   // list item cards

    // Vibrant Electric & Neon Accents
    val Primary    = Color(0xFF00F5D4)   // Electric Mint / Cyan
    val PrimaryDim = Color(0xFF00BFA5)   // Pressed / dark mint
    val PrimaryGlow= Color(0xFF70FFF0)   // Text glow on dark
    val Secondary  = Color(0xFF8B5CF6)   // Neon Violet
    val Tertiary   = Color(0xFFFF3366)   // Coral Pink Pop
    val ElectricBlue = Color(0xFF38BDF8) // Bright Sky Blue

    // Atmospheric Gradients
    val GradTop    = Color(0xFF0F172A)   // deep navy slate
    val GradMid    = Color(0xFF0B0F19)   // midnight core
    val GradBot    = Color(0xFF08090E)   // pitch obsidian

    // Dynamic accent gradient list
    val AuraGradient = listOf(Color(0xFF00F5D4), Color(0xFF8B5CF6), Color(0xFFFF3366))
    val CyanVioletGrad = listOf(Color(0xFF00F5D4), Color(0xFF8B5CF6))
    val VioletPinkGrad = listOf(Color(0xFF8B5CF6), Color(0xFFFF3366))
    val BlueCyanGrad = listOf(Color(0xFF38BDF8), Color(0xFF00F5D4))

    // High-contrast modern typography colors
    val OnBg       = Color(0xFFF8FAFC)
    val OnSurface  = Color(0xFFE2E8F0)
    val OnDim      = Color(0xFF94A3B8)
    val OnHint     = Color(0xFF64748B)

    // Utility
    val Outline    = Color(0xFF243048)
    val OutlineGlow= Color(0x4D00F5D4)
    val Error      = Color(0xFFFF4B4B)
    val Success    = Color(0xFF00E676)
    val Warning    = Color(0xFFFFB74D)
}

private val PhantasiaDark = darkColorScheme(
    primary            = PhantasiaColors.Primary,
    onPrimary          = Color(0xFF051B17),
    primaryContainer   = Color(0xFF003830),
    onPrimaryContainer = PhantasiaColors.PrimaryGlow,
    secondary          = PhantasiaColors.Secondary,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFF261247),
    onSecondaryContainer = Color(0xFFD8B4FE),
    tertiary           = PhantasiaColors.Tertiary,
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFF4C0519),
    onTertiaryContainer= Color(0xFFFFD1DC),
    background         = PhantasiaColors.Midnight,
    onBackground       = PhantasiaColors.OnBg,
    surface            = PhantasiaColors.Surface,
    onSurface          = PhantasiaColors.OnSurface,
    surfaceVariant     = PhantasiaColors.SurfaceCard,
    onSurfaceVariant   = PhantasiaColors.OnDim,
    error              = PhantasiaColors.Error,
    onError            = Color.White,
    errorContainer     = Color(0xFF4A0010),
    onErrorContainer   = Color(0xFFFFDAD6),
    outline            = PhantasiaColors.Outline,
    outlineVariant     = Color(0xFF1E283C),
    surfaceTint        = PhantasiaColors.Primary,
    scrim              = Color.Black,
    inverseSurface     = PhantasiaColors.OnBg,
    inverseOnSurface   = PhantasiaColors.Midnight,
    inversePrimary     = PhantasiaColors.PrimaryDim,
)

val PhantasiaTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 57.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 45.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 36.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp)
)

@Composable
fun PhantasiaTheme(
    dark: Boolean = true,   // Phantasia is always dark
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = PhantasiaDark,   // Always dark — never light
        typography  = PhantasiaTypography,
        content     = content
    )
}
