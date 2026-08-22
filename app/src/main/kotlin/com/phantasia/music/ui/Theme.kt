package com.phantasia.music.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Phantasia Funky Pop palette ────────────────────────────────────────────
// Same object name, same property names as before — every existing screen
// picks this up automatically with zero other file changes needed.
object PhantasiaColors {
    // Base — neutral near-black, no purple tint anymore
    val Midnight    = Color(0xFF0B0B0D)
    val Midnight2   = Color(0xFF100F14)
    val PurpleInk   = Color(0xFF121016)
    val GradTop     = Color(0xFF121014)
    val GradMid     = Color(0xFF0D0C0F)
    val GradBot     = Color(0xFF08070A)

    // Glass surfaces — translucent, layered on the dark base
    val Surface     = Color(0xFF16151A)
    val SurfaceHigh = Color(0xFF201F26)
    val SurfaceCard = Color(0xFF1B1A20)

    // Funky pop accents — one dominant, two supporting, never all three
    // saturated at once on the same element
    val Primary     = Color(0xFFFF4D6D)   // hot coral-pink — primary actions
    val PrimaryDim  = Color(0xFFCC3D57)
    val PrimaryGlow = Color(0xFFFF8FA3)
    val Secondary   = Color(0xFFC6F135)   // electric lime — secondary accents
    val Tertiary    = Color(0xFF3DDBFF)   // electric cyan — tertiary tags
    val ElectricBlue= Color(0xFF3DDBFF)

    // Dynamic accent gradient list
    val AuraGradient = listOf(Color(0xFFFF4D6D), Color(0xFFC6F135), Color(0xFF3DDBFF))
    val CyanVioletGrad = listOf(Color(0xFF3DDBFF), Color(0xFFFF4D6D))
    val VioletPinkGrad = listOf(Color(0xFFFF4D6D), Color(0xFFC6F135))
    val BlueCyanGrad = listOf(Color(0xFF3DDBFF), Color(0xFFC6F135))

    // Text
    val OnBg        = Color(0xFFF5F3F0)
    val OnSurface   = Color(0xFFEDEBE8)
    val OnDim       = Color(0xFFA8A5A0)
    val OnHint      = Color(0xFF6B6864)

    // Neobrutalist structural colors — always black regardless of fill
    val NeoBorder   = Color(0xFF000000)
    val ShadowHard  = Color(0xFF000000)

    // Utility
    val Outline     = Color(0xFF2A2830)
    val OutlineGlow = Color(0x4DFF4D6D)
    val Error       = Color(0xFFFF5252)
    val Success     = Color(0xFF4CAF50)
    val Warning     = Color(0xFFFFB74D)
}

private val PhantasiaDark = darkColorScheme(
    primary            = PhantasiaColors.Primary,
    onPrimary          = Color.White,
    primaryContainer   = PhantasiaColors.PrimaryDim,
    onPrimaryContainer = PhantasiaColors.PrimaryGlow,
    secondary          = PhantasiaColors.Secondary,
    onSecondary        = Color.Black,
    tertiary           = PhantasiaColors.Tertiary,
    onTertiary         = Color.Black,
    background         = PhantasiaColors.Midnight,
    onBackground       = PhantasiaColors.OnBg,
    surface            = PhantasiaColors.Surface,
    onSurface          = PhantasiaColors.OnSurface,
    surfaceVariant     = PhantasiaColors.SurfaceCard,
    onSurfaceVariant   = PhantasiaColors.OnDim,
    error              = PhantasiaColors.Error,
    outline            = PhantasiaColors.Outline,
    scrim              = Color.Black
)

val PhantasiaTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Black,    fontSize = 57.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 24.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 14.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 12.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 11.sp)
)

@Composable
fun PhantasiaTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(colorScheme = PhantasiaDark, typography = PhantasiaTypography, content = content)
}

// ── Reusable design-system modifiers ────────────────────────────────────────

/**
 * GLASS surface — for backgrounds, nav bars, big panels. Soft, translucent,
 * thin light border. Use this, never neoBlock, for anything that's mostly
 * a backdrop rather than something the user taps.
 */
fun Modifier.glassPanel(
    cornerRadius: Dp = 20.dp,
    tint: Color = Color.White.copy(alpha = 0.07f),
    borderAlpha: Float = 0.12f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(tint)
    .border(1.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(cornerRadius))

/**
 * BRUTALIST block — for buttons, chips, badges, anything tappable. Solid
 * fill color, black border, hard offset shadow (no blur — that's the
 * defining trait; a blurred shadow reads as "soft/glass", not brutalist).
 */
fun Modifier.neoBlock(
    fill: Color,
    cornerRadius: Dp = 12.dp,
    shadowOffset: Dp = 4.dp,
    borderWidth: Dp = 2.5.dp
): Modifier = this
    .drawBehind {
        val offsetPx = shadowOffset.toPx()
        drawRoundRect(
            color = PhantasiaColors.ShadowHard,
            topLeft = Offset(offsetPx, offsetPx),
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx())
        )
    }
    .clip(RoundedCornerShape(cornerRadius))
    .background(fill)
    .border(borderWidth, PhantasiaColors.NeoBorder, RoundedCornerShape(cornerRadius))
