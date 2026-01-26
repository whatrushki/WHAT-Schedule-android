package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Whatsapp: ImageVector
    get() {
        if (_Whatsapp != null) {
            return _Whatsapp!!
        }
        _Whatsapp = ImageVector.Builder(
            name = "Whatsapp",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF4CAF50))) {
                moveTo(256.06f, 0f)
                horizontalLineToRelative(-0.13f)
                lineToRelative(0f, 0f)
                curveTo(114.78f, 0f, 0f, 114.82f, 0f, 256f)
                curveToRelative(0f, 56f, 18.05f, 107.9f, 48.74f, 150.05f)
                lineToRelative(-31.9f, 95.1f)
                lineToRelative(98.4f, -31.46f)
                curveTo(155.71f, 496.51f, 204f, 512f, 256.06f, 512f)
                curveTo(397.22f, 512f, 512f, 397.15f, 512f, 256f)
                reflectiveCurveTo(397.22f, 0f, 256.06f, 0f)
                close()
            }
            path(fill = SolidColor(Color.Transparent)) {
                moveTo(405.02f, 361.5f)
                curveToRelative(-6.18f, 17.44f, -30.69f, 31.9f, -50.24f, 36.13f)
                curveToRelative(-13.38f, 2.85f, -30.85f, 5.12f, -89.66f, -19.26f)
                curveTo(189.89f, 347.2f, 141.44f, 270.75f, 137.66f, 265.79f)
                curveToRelative(-3.62f, -4.96f, -30.4f, -40.48f, -30.4f, -77.22f)
                reflectiveCurveToRelative(18.66f, -54.62f, 26.18f, -62.3f)
                curveToRelative(6.18f, -6.3f, 16.38f, -9.18f, 26.18f, -9.18f)
                curveToRelative(3.17f, 0f, 6.02f, 0.16f, 8.58f, 0.29f)
                curveToRelative(7.52f, 0.32f, 11.3f, 0.77f, 16.26f, 12.64f)
                curveToRelative(6.18f, 14.88f, 21.22f, 51.62f, 23.01f, 55.39f)
                curveToRelative(1.82f, 3.78f, 3.65f, 8.9f, 1.09f, 13.86f)
                curveToRelative(-2.4f, 5.12f, -4.51f, 7.39f, -8.29f, 11.74f)
                curveToRelative(-3.78f, 4.35f, -7.36f, 7.68f, -11.14f, 12.35f)
                curveToRelative(-3.46f, 4.06f, -7.36f, 8.42f, -3.01f, 15.94f)
                curveToRelative(4.35f, 7.36f, 19.39f, 31.9f, 41.54f, 51.62f)
                curveToRelative(28.58f, 25.44f, 51.74f, 33.57f, 60.03f, 37.02f)
                curveToRelative(6.18f, 2.56f, 13.54f, 1.95f, 18.05f, -2.85f)
                curveToRelative(5.73f, -6.18f, 12.8f, -16.42f, 20f, -26.5f)
                curveToRelative(5.12f, -7.23f, 11.58f, -8.13f, 18.37f, -5.57f)
                curveToRelative(6.91f, 2.4f, 43.49f, 20.48f, 51.01f, 24.22f)
                curveToRelative(7.52f, 3.78f, 12.48f, 5.57f, 14.3f, 8.74f)
                curveTo(411.2f, 329.15f, 411.2f, 344.03f, 405.02f, 361.5f)
                close()
            }
        }.build()

        return _Whatsapp!!
    }

@Suppress("ObjectPropertyName")
private var _Whatsapp: ImageVector? = null
