package com.phantasia.music.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun PhantasiaProgressBar(
    style: ProgressBarStyle,
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier
) {
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(fraction, tween(300), label = "progress_fraction")

    when (style) {
        ProgressBarStyle.GLOW_LINEAR -> GlowLinearBar(animatedFraction, modifier)
        ProgressBarStyle.WAVEFORM    -> WaveformBar(animatedFraction, modifier)
        ProgressBarStyle.NEO_CHUNKY  -> NeoChunkyBar(animatedFraction, modifier)
        ProgressBarStyle.LIQUID_GLASS -> LiquidGlassBar(animatedFraction, modifier)
        ProgressBarStyle.DOT_TRAIL   -> DotTrailBar(animatedFraction, modifier)
    }
}

// ── 1. GLOW LINEAR — thin line, soft coral glow around the filled portion,
//      gentle pulse while actively playing ─────────────────────────────────
@Composable
private fun GlowLinearBar(fraction: Float, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "glow_pulse")
    val pulse by transition.animateFloat(
        0.7f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_pulse_alpha"
    )
    Canvas(modifier.fillMaxWidth().height(4.dp)) {
        val h = size.height
        drawRoundRect(color = Color.White.copy(alpha = 0.15f), cornerRadius = CornerRadius(h / 2))
        val fillWidth = size.width * fraction
        // soft glow behind the fill
        drawRoundRect(
            color = PhantasiaColors.Primary.copy(alpha = 0.35f * pulse),
            size = Size(fillWidth, h * 2.5f),
            topLeft = Offset(0f, -h * 0.75f),
            cornerRadius = CornerRadius(h)
        )
        drawRoundRect(
            color = PhantasiaColors.Primary,
            size = Size(fillWidth, h),
            cornerRadius = CornerRadius(h / 2)
        )
    }
}

// ── 2. WAVEFORM — a row of animated bars whose heights are seeded from a
//      deterministic pattern (no real amplitude data available), filled
//      bars show the accent color, unfilled bars stay dim ──────────────────
@Composable
private fun WaveformBar(fraction: Float, modifier: Modifier) {
    val barCount = 40
    val transition = rememberInfiniteTransition(label = "waveform_motion")
    val phase by transition.animateFloat(
        0f, 2f * Math.PI.toFloat(), infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "waveform_phase"
    )
    Canvas(modifier.fillMaxWidth().height(24.dp)) {
        val barWidth = size.width / (barCount * 1.6f)
        val gap = barWidth * 0.6f
        for (i in 0 until barCount) {
            val seed = (i * 37 % 17) / 17f
            val heightFraction = 0.25f + 0.6f * ((sin(phase + i * 0.4f) + 1f) / 2f) * (0.5f + seed * 0.5f)
            val barHeight = size.height * heightFraction
            val x = i * (barWidth + gap)
            val isFilled = (x / size.width) <= fraction
            drawRoundRect(
                color = if (isFilled) PhantasiaColors.Primary else Color.White.copy(alpha = 0.18f),
                topLeft = Offset(x, (size.height - barHeight) / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2)
            )
        }
    }
}

// ── 3. NEO_CHUNKY — Thick 10dp bar, hard black 2dp border around track,
//      solid Secondary (lime) fill with NO gradient/glow, square-ish corners (4dp).
//      Thumb: small solid black square positioned at fraction.
@Composable
private fun NeoChunkyBar(fraction: Float, modifier: Modifier) {
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        val h = size.height
        val w = size.width
        val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        val fillWidth = (w * fraction).coerceIn(0f, w)

        // Track background
        drawRoundRect(
            color = Color(0xFF1E1D24),
            size = Size(w, h),
            cornerRadius = corner
        )

        // Filled secondary color
        if (fillWidth > 0) {
            drawRoundRect(
                color = PhantasiaColors.Secondary,
                size = Size(fillWidth, h),
                cornerRadius = corner
            )
        }

        // Hard black 2dp border around whole track
        drawRoundRect(
            color = PhantasiaColors.NeoBorder,
            size = Size(w, h),
            cornerRadius = corner,
            style = Stroke(width = 2.dp.toPx())
        )

        // Thumb: small solid black square at fraction position
        val thumbSize = 12.dp.toPx()
        val thumbX = (fillWidth - thumbSize / 2f).coerceIn(0f, w - thumbSize)
        val thumbY = (h - thumbSize) / 2f
        drawRect(
            color = PhantasiaColors.NeoBorder,
            topLeft = Offset(thumbX, thumbY),
            size = Size(thumbSize, thumbSize)
        )
        drawRect(
            color = PhantasiaColors.Secondary,
            topLeft = Offset(thumbX + 1.5.dp.toPx(), thumbY + 1.5.dp.toPx()),
            size = Size(thumbSize - 3.dp.toPx(), thumbSize - 3.dp.toPx())
        )
    }
}

// ── 4. LIQUID_GLASS — Pill-shaped capsule track with translucent white glass fill.
//      Filled portion is slightly more opaque with diagonal highlight sweep.
@Composable
private fun LiquidGlassBar(fraction: Float, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "liquid_sweep")
    val sweepOffset by transition.animateFloat(
        initialValue = -150f, targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep_offset"
    )
    Canvas(modifier.fillMaxWidth().height(6.dp)) {
        val h = size.height
        val w = size.width
        val corner = CornerRadius(h / 2f, h / 2f)
        val fillWidth = (w * fraction).coerceIn(0f, w)

        // Base glass track
        drawRoundRect(
            color = Color.White.copy(alpha = 0.08f),
            size = Size(w, h),
            cornerRadius = corner
        )
        // Thin glass border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.15f),
            size = Size(w, h),
            cornerRadius = corner,
            style = Stroke(width = 1.dp.toPx())
        )

        // Filled portion with liquid sweep
        if (fillWidth > 0) {
            val sweepBrush = Brush.linearGradient(
                colors = listOf(
                    PhantasiaColors.PrimaryDim.copy(alpha = 0.6f),
                    PhantasiaColors.PrimaryGlow.copy(alpha = 0.95f),
                    PhantasiaColors.Primary.copy(alpha = 0.8f)
                ),
                start = Offset(sweepOffset, 0f),
                end = Offset(sweepOffset + 120f, h)
            )
            drawRoundRect(
                brush = sweepBrush,
                size = Size(fillWidth, h),
                cornerRadius = corner
            )
        }
    }
}

// ── 5. DOT_TRAIL — Minimal 1.5dp track with glowing dot + fading comet trail.
@Composable
private fun DotTrailBar(fraction: Float, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "dot_pulse")
    val pulse by transition.animateFloat(
        0.75f, 1f, infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot_pulse_alpha"
    )
    Canvas(modifier.fillMaxWidth().height(12.dp)) {
        val h = size.height
        val w = size.width
        val lineY = h / 2f
        val dotX = (w * fraction).coerceIn(0f, w)

        // Minimal track line
        drawLine(
            color = Color.White.copy(alpha = 0.12f),
            start = Offset(0f, lineY),
            end = Offset(w, lineY),
            strokeWidth = 1.5.dp.toPx()
        )

        // Trail copies
        val trailAlphas = listOf(0.15f, 0.35f, 0.60f)
        val trailSpacings = listOf(24.dp.toPx(), 16.dp.toPx(), 8.dp.toPx())
        for (i in trailAlphas.indices) {
            val tx = dotX - trailSpacings[i]
            if (tx >= 0) {
                drawCircle(
                    color = PhantasiaColors.Tertiary.copy(alpha = trailAlphas[i] * pulse),
                    radius = (2.dp + (i * 0.5f).dp).toPx(),
                    center = Offset(tx, lineY)
                )
            }
        }

        // Main glowing dot
        drawCircle(
            color = PhantasiaColors.Tertiary.copy(alpha = 0.35f * pulse),
            radius = 6.dp.toPx(),
            center = Offset(dotX, lineY)
        )
        drawCircle(
            color = PhantasiaColors.Tertiary,
            radius = 3.dp.toPx(),
            center = Offset(dotX, lineY)
        )
    }
}
