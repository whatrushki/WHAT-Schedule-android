package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Telegram: ImageVector
    get() {
        if (_Telegram != null) {
            return _Telegram!!
        }
        _Telegram = ImageVector.Builder(
            name = "Telegram",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveToRelative(115.88f, 253.3f)
                curveToRelative(74.63f, -32.51f, 124.39f, -53.95f, 149.29f, -64.31f)
                curveToRelative(71.09f, -29.57f, 85.87f, -34.71f, 95.5f, -34.88f)
                curveToRelative(2.12f, -0.04f, 6.85f, 0.49f, 9.92f, 2.98f)
                curveToRelative(4.55f, 3.69f, 4.58f, 11.71f, 4.07f, 17.01f)
                curveToRelative(-3.85f, 40.48f, -20.52f, 138.71f, -29f, 184.05f)
                curveToRelative(-3.59f, 19.18f, -10.65f, 25.62f, -17.5f, 26.25f)
                curveToRelative(-14.87f, 1.37f, -26.16f, -9.82f, -40.55f, -19.26f)
                curveToRelative(-22.53f, -14.77f, -35.26f, -23.96f, -57.13f, -38.38f)
                curveToRelative(-25.27f, -16.66f, -8.89f, -25.81f, 5.51f, -40.77f)
                curveToRelative(3.77f, -3.91f, 69.27f, -63.49f, 70.54f, -68.9f)
                curveToRelative(0.16f, -0.68f, 0.31f, -3.2f, -1.19f, -4.53f)
                reflectiveCurveToRelative(-3.71f, -0.88f, -5.3f, -0.51f)
                curveToRelative(-2.26f, 0.51f, -38.25f, 24.3f, -107.98f, 71.37f)
                curveToRelative(-10.22f, 7.02f, -19.47f, 10.43f, -27.76f, 10.26f)
                curveToRelative(-9.14f, -0.2f, -26.72f, -5.17f, -39.79f, -9.42f)
                curveToRelative(-16.03f, -5.21f, -28.77f, -7.97f, -27.66f, -16.82f)
                curveToRelative(0.58f, -4.61f, 6.93f, -9.32f, 19.05f, -14.14f)
                close()
            }
        }.build()

        return _Telegram!!
    }

@Suppress("ObjectPropertyName")
private var _Telegram: ImageVector? = null
