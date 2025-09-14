package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Question: ImageVector
    get() {
        if (_Question != null) {
            return _Question!!
        }
        _Question = ImageVector.Builder(
            name = "Question",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(47.63f, 2.5f)
                curveToRelative(-2.13f, 0.24f, -4.6f, 0.37f, -7.02f, 0.83f)
                curveToRelative(-5.66f, 1.08f, -10.71f, 3.48f, -14.87f, 7.53f)
                arcToRelative(
                    10.29f,
                    10.29f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.55f,
                    14.23f
                )
                curveToRelative(2.95f, 3.35f, 7.32f, 3.34f, 11.99f, -0.03f)
                curveToRelative(0.32f, -0.23f, 0.65f, -0.46f, 0.96f, -0.7f)
                curveToRelative(3.48f, -2.64f, 7.47f, -3.67f, 11.73f, -3.25f)
                curveToRelative(3.52f, 0.35f, 6.53f, 1.86f, 7.78f, 5.49f)
                curveToRelative(1.21f, 3.51f, -0.48f, 6.15f, -2.95f, 8.39f)
                curveToRelative(-1.24f, 1.13f, -2.66f, 2.05f, -4.01f, 3.05f)
                curveToRelative(-8.1f, 6.02f, -11.62f, 15.48f, -9.13f, 24.57f)
                curveToRelative(1.18f, 4.32f, 3.2f, 6.59f, 5.85f, 6.59f)
                curveToRelative(2.59f, -0f, 4.56f, -2.04f, 5.72f, -6.33f)
                curveToRelative(1.2f, -4.46f, 3.49f, -8.2f, 7.01f, -11.1f)
                curveToRelative(2.82f, -2.33f, 5.9f, -4.32f, 8.74f, -6.62f)
                curveToRelative(8.88f, -7.19f, 11.04f, -18.31f, 5.4f, -28.2f)
                curveTo(68.49f, 6.77f, 59.07f, 3.13f, 47.63f, 2.5f)
                moveToRelative(1.49f, 73.87f)
                curveToRelative(-6.54f, 0.01f, -10.7f, 4.03f, -10.68f, 10.35f)
                curveToRelative(0.01f, 6.24f, 4.46f, 10.81f, 10.5f, 10.79f)
                curveToRelative(6.34f, -0.02f, 10.9f, -4.53f, 10.86f, -10.76f)
                curveToRelative(-0.03f, -6.34f, -4.2f, -10.38f, -10.69f, -10.37f)
            }
        }.build()

        return _Question!!
    }

@Suppress("ObjectPropertyName")
private var _Question: ImageVector? = null

