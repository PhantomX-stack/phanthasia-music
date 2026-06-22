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

private val Dark = darkColorScheme(primary=Color(0xFFCFBCFF), onPrimary=Color(0xFF381E72),
    primaryContainer=Color(0xFF4F378A), background=Color(0xFF1C1B1F), surface=Color(0xFF1C1B1F))
private val Light = lightColorScheme(primary=Color(0xFF6650A4), onPrimary=Color(0xFFFFFFFF),
    primaryContainer=Color(0xFFEADDFF), background=Color(0xFFFFFBFE), surface=Color(0xFFFFFBFE))
private val Type = Typography(
    bodyLarge=TextStyle(fontWeight=FontWeight.Normal,   fontSize=16.sp, lineHeight=24.sp),
    titleMedium=TextStyle(fontWeight=FontWeight.Medium, fontSize=16.sp, lineHeight=24.sp),
    titleLarge=TextStyle(fontWeight=FontWeight.SemiBold,fontSize=22.sp, lineHeight=28.sp),
    labelSmall=TextStyle(fontWeight=FontWeight.Medium,  fontSize=11.sp, lineHeight=16.sp))

@Composable
fun PhantasiaTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val cs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val ctx = LocalContext.current; if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else if (dark) Dark else Light
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val w = (view.context as Activity).window; w.statusBarColor = cs.background.toArgb()
        WindowCompat.getInsetsController(w, view).isAppearanceLightStatusBars = !dark
    }
    MaterialTheme(colorScheme = cs, typography = Type, content = content)
}
