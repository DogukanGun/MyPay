package com.dag.mypayandroid.feature.jarvis.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun VoiceWaveAnimation(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    waveColor: Color = Color(0xFF8EC5FC),
    backgroundColor: Color = Color(0xFF8EC5FC).copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    val animatedPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    val amplitude by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amplitude"
    )
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val centerY = size.height / 2
        val waveWidth = size.width
        val waveCount = 3
        
        if (isActive) {
            repeat(waveCount) { index ->
                val phaseOffset = (index * PI / 3).toFloat()
                val currentAmplitude = amplitude * (0.6f + index * 0.2f)
                val alpha = 1f - (index * 0.3f)
                
                drawWave(
                    phase = animatedPhase + phaseOffset,
                    amplitude = currentAmplitude * 80,
                    frequency = 0.01f + (index * 0.002f),
                    centerY = centerY,
                    width = waveWidth,
                    color = waveColor.copy(alpha = alpha),
                    strokeWidth = 6f - (index * 1f)
                )
            }
        } else {
            drawWave(
                phase = 0f,
                amplitude = 40f,
                frequency = 0.01f,
                centerY = centerY,
                width = waveWidth,
                color = backgroundColor,
                strokeWidth = 4f
            )
        }
    }
}

private fun DrawScope.drawWave(
    phase: Float,
    amplitude: Float,
    frequency: Float,
    centerY: Float,
    width: Float,
    color: Color,
    strokeWidth: Float
) {
    val points = mutableListOf<Offset>()
    val stepSize = 2f
    
    for (x in 0..width.toInt() step stepSize.toInt()) {
        val y = centerY + amplitude * sin(frequency * x + phase)
        points.add(Offset(x.toFloat(), y))
    }
    
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = points[i],
            end = points[i + 1],
            strokeWidth = strokeWidth
        )
    }
}