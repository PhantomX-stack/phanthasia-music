package com.phantasia.music.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Phantasia palette ─────────────────────────────────────────────────────────
object PhantasiaColors {
    val PurpleGlow    = Color(0xFF9B59FF)
    val PurpleSoft    = Color(0xFFCFBCFF)
    val PurpleDark    = Color(0xFF381E72)
    val PurpleDeep    = Color(0xFF1A0A3D)
    val SurfaceDark   = Color(0xFF0D0D14)
    val SurfaceMid    = Color(0xFF16161F)
    val SurfaceCard   = Color(0xFF1E1E2C)
    val OnSurfaceDim  = Color(0xFF8A8AA0)
    val White         = Color(0xFFFFFFFF)
    val AccentCyan    = Color(0xFF00D4FF)
    val AccentPink    = Color(0xFFFF6B9D)
}

private val Dark = darkColorScheme(
    primary          = PhantasiaColors.PurpleGlow,
    onPrimary        = PhantasiaColors.White,
    primaryContainer = PhantasiaColors.PurpleDark,
    onPrimaryContainer = PhantasiaColors.PurpleSoft,
    secondary        = PhantasiaColors.AccentCyan,
    onSecondary      = PhantasiaColors.SurfaceDark,
    background       = PhantasiaColors.SurfaceDark,
    onBackground     = PhantasiaColors.White,
    surface          = PhantasiaColors.SurfaceMid,
    onSurface        = PhantasiaColors.White,
    surfaceVariant   = PhantasiaColors.SurfaceCard,
    onSurfaceVariant = PhantasiaColors.OnSurfaceDim,
    error            = Color(0xFFFF5252),
    outline          = Color(0xFF3A3A50)
)

// Light stays Material default — app is dark-first
private val Light = lightColorScheme(
    primary          = PhantasiaColors.PurpleDark,
    onPrimary        = PhantasiaColors.White,
    primaryContainer = PhantasiaColors.PurpleSoft,
    background       = Color(0xFFFAF7FF),
    surface          = Color(0xFFF0EDF8),
    secondary        = Color(0xFF006A6A),
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
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        dark  -> Dark
        else  -> Light
    }

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
