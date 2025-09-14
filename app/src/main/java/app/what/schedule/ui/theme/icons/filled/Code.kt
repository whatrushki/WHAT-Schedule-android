package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Code: ImageVector
    get() {
        if (_Code != null) {
            return _Code!!
        }
        _Code = ImageVector.Builder(
            name = "Code",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 60f,
            viewportHeight = 60f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(14f, 44f)
                arcToRelative(
                    2.98f,
                    2.98f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -2.122f,
                    -0.88f
                )
                lineTo(0.879f, 32.121f)
                curveTo(0.313f, 31.557f, 0f, 30.803f, 0f, 30f)
                reflectiveCurveToRelative(0.313f, -1.557f, 0.88f, -2.122f)
                lineToRelative(10.999f, -10.999f)
                arcTo(2.98f, 2.98f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 16f)
                curveToRelative(1.654f, 0f, 3f, 1.346f, 3f, 3f)
                curveToRelative(0f, 0.803f, -0.313f, 1.557f, -0.88f, 2.122f)
                lineTo(7.242f, 30f)
                lineToRelative(8.879f, 8.879f)
                curveToRelative(0.567f, 0.564f, 0.879f, 1.318f, 0.879f, 2.121f)
                curveToRelative(0f, 1.654f, -1.346f, 3f, -3f, 3f)
                moveToRelative(32f, 1f)
                curveToRelative(-1.654f, 0f, -3f, -1.346f, -3f, -3f)
                curveToRelative(0f, -0.803f, 0.313f, -1.557f, 0.88f, -2.122f)
                lineTo(52.758f, 31f)
                lineToRelative(-8.879f, -8.879f)
                arcTo(2.98f, 2.98f, 0f, isMoreThanHalf = false, isPositiveArc = true, 43f, 20f)
                curveToRelative(0f, -1.654f, 1.346f, -3f, 3f, -3f)
                curveToRelative(0.803f, 0f, 1.557f, 0.313f, 2.122f, 0.88f)
                lineToRelative(10.999f, 10.999f)
                curveToRelative(0.567f, 0.566f, 0.879f, 1.32f, 0.879f, 2.121f)
                curveToRelative(0f, 0.803f, -0.313f, 1.557f, -0.88f, 2.122f)
                lineTo(48.121f, 44.121f)
                arcTo(2.97f, 2.97f, 0f, isMoreThanHalf = false, isPositiveArc = true, 46f, 45f)
                moveToRelative(-25f, 8f)
                arcToRelative(
                    3.004f,
                    3.004f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -2.77f,
                    -4.155f
                )
                lineTo(36.231f, 8.843f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39f, 7f)
                arcToRelative(
                    3.004f,
                    3.004f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    2.77f,
                    4.155f
                )
                lineTo(23.769f, 51.157f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 53f)
            }
        }.build()

        return _Code!!
    }

@Suppress("ObjectPropertyName")
private var _Code: ImageVector? = null
