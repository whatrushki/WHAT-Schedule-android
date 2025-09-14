package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Warn: ImageVector
    get() {
        if (_Warn != null) {
            return _Warn!!
        }
        _Warn = ImageVector.Builder(
            name = "Warn",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 486.46f,
            viewportHeight = 486.46f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(243.23f, 333.38f)
                curveToRelative(-13.6f, 0f, -25f, 11.4f, -25f, 25f)
                reflectiveCurveToRelative(11.4f, 25f, 25f, 25f)
                curveToRelative(13.1f, 0f, 25f, -11.4f, 24.4f, -24.4f)
                curveToRelative(0.6f, -14.3f, -10.7f, -25.6f, -24.4f, -25.6f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(474.63f, 421.98f)
                curveToRelative(15.7f, -27.1f, 15.8f, -59.4f, 0.2f, -86.4f)
                lineToRelative(-156.6f, -271.2f)
                curveToRelative(-15.5f, -27.3f, -43.5f, -43.5f, -74.9f, -43.5f)
                reflectiveCurveToRelative(-59.4f, 16.3f, -74.9f, 43.4f)
                lineToRelative(-156.8f, 271.5f)
                curveToRelative(-15.6f, 27.3f, -15.5f, 59.8f, 0.3f, 86.9f)
                curveToRelative(15.6f, 26.8f, 43.5f, 42.9f, 74.7f, 42.9f)
                horizontalLineToRelative(312.8f)
                curveToRelative(31.3f, 0f, 59.4f, -16.3f, 75.2f, -43.6f)
                moveToRelative(-34f, -19.6f)
                curveToRelative(-8.7f, 15f, -24.1f, 23.9f, -41.3f, 23.9f)
                horizontalLineToRelative(-312.8f)
                curveToRelative(-17f, 0f, -32.3f, -8.7f, -40.8f, -23.4f)
                curveToRelative(-8.6f, -14.9f, -8.7f, -32.7f, -0.1f, -47.7f)
                lineToRelative(156.8f, -271.4f)
                curveToRelative(8.5f, -14.9f, 23.7f, -23.7f, 40.9f, -23.7f)
                curveToRelative(17.1f, 0f, 32.4f, 8.9f, 40.9f, 23.8f)
                lineToRelative(156.7f, 271.4f)
                curveToRelative(8.4f, 14.6f, 8.3f, 32.2f, -0.3f, 47.1f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(237.02f, 157.88f)
                curveToRelative(-11.9f, 3.4f, -19.3f, 14.2f, -19.3f, 27.3f)
                curveToRelative(0.6f, 7.9f, 1.1f, 15.9f, 1.7f, 23.8f)
                curveToRelative(1.7f, 30.1f, 3.4f, 59.6f, 5.1f, 89.7f)
                curveToRelative(0.6f, 10.2f, 8.5f, 17.6f, 18.7f, 17.6f)
                reflectiveCurveToRelative(18.2f, -7.9f, 18.7f, -18.2f)
                curveToRelative(0f, -6.2f, 0f, -11.9f, 0.6f, -18.2f)
                curveToRelative(1.1f, -19.3f, 2.3f, -38.6f, 3.4f, -57.9f)
                curveToRelative(0.6f, -12.5f, 1.7f, -25f, 2.3f, -37.5f)
                curveToRelative(0f, -4.5f, -0.6f, -8.5f, -2.3f, -12.5f)
                curveToRelative(-5.1f, -11.2f, -17f, -16.9f, -28.9f, -14.1f)
            }
        }.build()

        return _Warn!!
    }

@Suppress("ObjectPropertyName")
private var _Warn: ImageVector? = null
