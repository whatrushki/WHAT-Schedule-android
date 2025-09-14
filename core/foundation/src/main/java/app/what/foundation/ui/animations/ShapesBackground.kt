import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class BackgroundShapeConfig(
    val size: Dp = 120.dp,
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val rotationDuration: Int = 20000,
    val color: Color? = null, // if null using material colors instead
    val strokeColor: Color? = null, // if null using material colors instead
    val strokeWidth: Dp = 2.dp,
    val sides: Int = 6 // количество сторон многоугольника
)

private fun DrawScope.drawPolygon(
    size: Size,
    color: Color,
    strokeColor: Color,
    strokeWidth: Float,
    sides: Int
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.width / 2

    val path = Path().apply {
        for (i in 0 until sides) {
            val angle = 2.0 * PI / sides * i - PI / 2 // начинаем с верхней точки
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()

            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }

    // Заливка
    drawPath(path, color, style = Fill)

    // Обводка
    drawPath(path, strokeColor, style = Stroke(width = strokeWidth))
}

private fun DrawScope.drawSimpleRoundedStar(
    size: Size,
    color: Color,
    strokeColor: Color,
    strokeWidth: Float,
    points: Int
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val outerRadius = size.width / 2 * 0.9f
    val innerRadius = outerRadius * 0.4f

    val path = Path().apply {
        val totalPoints = points * 2

        for (i in 0 until totalPoints) {
            val angle = 2.0 * PI / totalPoints * i - PI / 2
            val radius = if (i % 2 == 0) outerRadius else innerRadius

            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()

            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }

    // Заливка
    drawPath(path, color, style = Fill)

    // Обводка
    drawPath(path, strokeColor, style = Stroke(width = strokeWidth))
}

@Composable
fun Modifier.animatedStarsBackground(
    shape1: BackgroundShapeConfig = BackgroundShapeConfig(
        size = 120.dp,
        offsetX = (45).dp,
        offsetY = 10.dp,
        rotationDuration = 20000,
        strokeWidth = 1.5.dp,
        sides = 6 // шестиугольник
    ),
    shape2: BackgroundShapeConfig = BackgroundShapeConfig(
        size = 250.dp,
        offsetX = (-10).dp,
        offsetY = (-10).dp,
        rotationDuration = 25000,
        strokeWidth = 1.5.dp,
        sides = 5 // пятиугольник
    )
): Modifier = composed {
    val localColorScheme = colorScheme
    val infiniteTransition = rememberInfiniteTransition()

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shape1.rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shape2.rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    this.then(
        Modifier.drawWithContent {
            val shapeSize1 = Size(shape1.size.toPx(), shape1.size.toPx())
            val shapeSize2 = Size(shape2.size.toPx(), shape2.size.toPx())

            // Первый shape - шестиугольник слева сверху
            translate(left = shape1.offsetX.toPx(), top = shape1.offsetY.toPx()) {
                rotate(rotation1, pivot = Offset(shapeSize1.width / 2, shapeSize1.height / 2)) {
                    drawSimpleRoundedStar(
                        size = shapeSize1,
                        color = shape2.color ?: localColorScheme.primary.copy(alpha = .5f),
                        strokeColor = shape2.strokeColor ?: localColorScheme.primary,
                        strokeWidth = shape1.strokeWidth.toPx(),
                        points = shape1.sides
                    )
                }
            }

            // Второй shape - пятиугольник справа снизу
            translate(
                left = size.width - shapeSize2.width + shape2.offsetX.toPx(),
                top = size.height - shapeSize2.height + shape2.offsetY.toPx()
            ) {
                rotate(rotation2, pivot = Offset(shapeSize2.width / 2, shapeSize2.height / 2)) {
                    drawSimpleRoundedStar(
                        size = shapeSize2,
                        color = shape2.color ?: localColorScheme.primary.copy(alpha = .5f),
                        strokeColor = shape2.strokeColor ?: localColorScheme.primary,
                        strokeWidth = shape2.strokeWidth.toPx(),
                        points = shape2.sides
                    )
                }
            }

            drawContent()
        }
    )
}