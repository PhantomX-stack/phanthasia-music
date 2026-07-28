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

// ── Phantasia Midnight Purple Palette ─────────────────────────────────────────
object PhantasiaColors {
    // Core backgrounds
    val Midnight   = Color(0xFF050510)   // deepest black-purple
    val Midnight2  = Color(0xFF0A0A1A)   // slightly lighter
    val PurpleInk  = Color(0xFF0F0F24)   // nav/surface base
    val Surface    = Color(0xFF12122A)   // card surfaces
    val SurfaceHigh= Color(0xFF1A1A3A)   // elevated cards
    val SurfaceCard= Color(0xFF181830)   // list items

    // Brand purples
    val Primary    = Color(0xFF9B59FF)   // main accent
    val PrimaryDim = Color(0xFF7B3FEF)   // pressed/container
    val PrimaryGlow= Color(0xFFBB86FC)   // text on dark
    val Secondary  = Color(0xFF6C63FF)   // indigo accent
    val Tertiary   = Color(0xFFE040FB)   // magenta pop

    // Gradient stops
    val GradTop    = Color(0xFF0D0520)   // deep violet
    val GradMid    = Color(0xFF080818)   // near black
    val GradBot    = Color(0xFF050510)   // pitch black

    // Text
    val OnBg       = Color(0xFFEDE8FF)
    val OnSurface  = Color(0xFFDDD8F8)
    val OnDim      = Color(0xFF8880A8)
    val OnHint     = Color(0xFF555575)

    // Utility
    val Outline    = Color(0xFF252545)
    val Error      = Color(0xFFFF5252)
    val Success    = Color(0xFF4CAF50)
    val Warning    = Color(0xFFFFB74D)
}

private val PhantasiaDark = darkColorScheme(
    primary            = PhantasiaColors.Primary,
    onPrimary          = Color.White,
    primaryContainer   = PhantasiaColors.PrimaryDim,
    onPrimaryContainer = PhantasiaColors.PrimaryGlow,
    secondary          = PhantasiaColors.Secondary,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFF1C1950),
    onSecondaryContainer = PhantasiaColors.PrimaryGlow,
    tertiary           = PhantasiaColors.Tertiary,
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFF3A0050),
    onTertiaryContainer= Color(0xFFF8AAFF),
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
    outlineVariant     = Color(0xFF1E1E40),
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
