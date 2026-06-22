package com.phantasia.music.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

@Composable
fun DynamicBackground(artworkUrl: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    var dominant by remember { mutableStateOf(Color(0xFF1C1B1F)) }
    var vibrant  by remember { mutableStateOf(Color(0xFF4F378A)) }
    LaunchedEffect(artworkUrl) {
        if (artworkUrl.isBlank()) return@LaunchedEffect
        runCatching {
            val r = ctx.imageLoader.execute(ImageRequest.Builder(ctx).data(artworkUrl).allowHardware(false).build())
            if (r is SuccessResult) {
                val bmp: Bitmap = (r.drawable as? BitmapDrawable)?.bitmap ?: return@runCatching
                Palette.from(bmp).generate { p ->
                    p?.dominantSwatch?.rgb?.let { dominant = Color(it) }
                    p?.vibrantSwatch?.rgb?.let  { vibrant  = Color(it) }
                }
            }
        }
    }
    val animDom by animateColorAsState(dominant, tween(800), label = "dom")
    val animVib by animateColorAsState(vibrant,  tween(800), label = "vib")
    Box(modifier = modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(animVib.copy(alpha = 0.65f), animDom.copy(alpha = 0.95f), Color.Black))))
}
