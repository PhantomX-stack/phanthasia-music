package com.phantasia.music.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
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

// ── Phantasia palette ─────────────────────────────────────────────────────────
object PhantasiaColors {
    val Midnight      = Color(0xFF070113)
    val Midnight2     = Color(0xFF100021)
    val PurpleInk     = Color(0xFF1A0738)
    val PurpleDeep    = Color(0xFF2B0A57)
    val PurpleDark    = Color(0xFF4B168F)
    val PurpleGlow    = Color(0xFFA855F7)
    val PurpleHot     = Color(0xFFD946EF)
    val PurpleSoft    = Color(0xFFE9D5FF)
    val LavenderMist  = Color(0xFFF7EDFF)
    val SurfaceDark   = Color(0xFF12081F)
    val SurfaceMid    = Color(0xFF1C0F2E)
    val SurfaceCard   = Color(0xFF28133F)
    val OnSurfaceDim  = Color(0xFFC9B6E4)
    val White         = Color(0xFFFFFFFF)
    val ErrorPink     = Color(0xFFFF6BBA)
}

private val Dark = darkColorScheme(
    primary = PhantasiaColors.PurpleGlow,
    onPrimary = PhantasiaColors.White,
    primaryContainer = PhantasiaColors.PurpleDeep,
    onPrimaryContainer = PhantasiaColors.PurpleSoft,
    secondary = PhantasiaColors.PurpleHot,
    onSecondary = PhantasiaColors.White,
    secondaryContainer = PhantasiaColors.PurpleDark,
    onSecondaryContainer = PhantasiaColors.PurpleSoft,
    tertiary = PhantasiaColors.PurpleSoft,
    onTertiary = PhantasiaColors.Midnight,
    tertiaryContainer = PhantasiaColors.SurfaceCard,
    onTertiaryContainer = PhantasiaColors.LavenderMist,
    background = PhantasiaColors.Midnight,
    onBackground = PhantasiaColors.White,
    surface = PhantasiaColors.SurfaceMid,
    onSurface = PhantasiaColors.White,
    surfaceVariant = PhantasiaColors.SurfaceCard,
    onSurfaceVariant = PhantasiaColors.OnSurfaceDim,
    surfaceTint = PhantasiaColors.PurpleGlow,
    inverseSurface = PhantasiaColors.LavenderMist,
    inverseOnSurface = PhantasiaColors.PurpleInk,
    error = PhantasiaColors.ErrorPink,
    outline = Color(0xFF6F4A99),
    outlineVariant = Color(0xFF3B2258),
)

private val Light = lightColorScheme(
    primary = PhantasiaColors.PurpleDark,
    onPrimary = PhantasiaColors.White,
    primaryContainer = PhantasiaColors.PurpleSoft,
    onPrimaryContainer = PhantasiaColors.PurpleInk,
    secondary = PhantasiaColors.PurpleGlow,
    onSecondary = PhantasiaColors.White,
    secondaryContainer = Color(0xFFEBD7FF),
    onSecondaryContainer = PhantasiaColors.PurpleInk,
    tertiary = PhantasiaColors.PurpleHot,
    onTertiary = PhantasiaColors.White,
    background = Color(0xFFFFF7FF),
    onBackground = PhantasiaColors.PurpleInk,
    surface = Color(0xFFF8EEFF),
    onSurface = PhantasiaColors.PurpleInk,
    surfaceVariant = Color(0xFFEEDFFF),
    onSurfaceVariant = Color(0xFF5C4772),
    surfaceTint = PhantasiaColors.PurpleGlow,
    error = Color(0xFFB0005A),
    outline = Color(0xFF8D6BA8),
    outlineVariant = Color(0xFFD8C2E8),
)

val PhantasiaTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 57.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 45.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 36.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 12.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 11.sp, lineHeight = 16.sp)
)

@Composable
fun PhantasiaTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (dark) Dark else Light

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = PhantasiaTypography,
        content     = content
    )
}
