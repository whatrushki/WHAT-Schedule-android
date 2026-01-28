package app.what.foundation.ui.animations

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun AdvancedLiquidBackground(
    layers: List<Pair<Color, Color>>
) {
    val infiniteTransition = rememberInfiniteTransition()

    Box(modifier = Modifier.fillMaxSize()) {
        layers.forEachIndexed { index, (startColor, endColor) ->
            val animatedColor by infiniteTransition.animateColor(
                initialValue = startColor,
                targetValue = endColor,
                animationSpec = infiniteRepeatable(
                    animation = tween(7000 + index * 2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            LiquidLayer(
                color = animatedColor,
                amplitude = 30f + index * 10f,
                frequency = 0.01f + index * 0.005f,
                speed = 10000 + index * 3000,
                offsetY = 0.4f + index * 0.1f,
                alpha = 0.6f + index * 0.1f,
                blur = (20 + index * 10).dp
            )
        }
    }
}

@Composable
fun LiquidLayer(
    color: Color,
    amplitude: Float,
    frequency: Float,
    speed: Int,
    offsetY: Float,
    alpha: Float = 1f,
    blur: Dp = 10.dp,
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Анимация фазы для волны
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4 * Math.PI.toFloat(), // Увеличил диапазон для плавности
        animationSpec = infiniteRepeatable(
            animation = tween(speed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Дополнительная анимация для сложной волны
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(speed / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(blur, BlurredEdgeTreatment.Unbounded)
    ) {
        val path = Path().apply {
            moveTo(0f, size.height * offsetY)

            for (x in 0 until size.width.toInt() step 4) {
                val normalizedX = x.toFloat() / size.width

                val y = size.height * offsetY +
                        sin(phase + x * frequency) * amplitude +
                        sin(phase2 + x * frequency * 0.7f) * amplitude * 0.7f +
                        cos(phase * 0.5f + normalizedX * 10f) * amplitude * 0.3f

                lineTo(x.toFloat(), y)
            }

            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = path,
            color = color,
            alpha = alpha
        )
    }
}