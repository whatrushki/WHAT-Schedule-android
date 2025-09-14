package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Export: ImageVector
    get() {
        if (_Export != null) {
            return _Export!!
        }
        _Export = ImageVector.Builder(
            name = "Export",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(2.51f, 79.29f)
                verticalLineTo(20.71f)
                curveTo(2.51f, 10.6f, 10.72f, 2.5f, 20.72f, 2.5f)
                horizontalLineToRelative(19.28f)
                curveToRelative(3.93f, 0f, 7.14f, 3.21f, 7.14f, 7.14f)
                reflectiveCurveToRelative(-3.21f, 7.14f, -7.14f, 7.14f)
                horizontalLineTo(20.72f)
                curveToRelative(-2.14f, 0f, -3.93f, 1.78f, -3.93f, 3.93f)
                verticalLineToRelative(58.57f)
                curveToRelative(0f, 2.14f, 1.79f, 3.93f, 3.93f, 3.93f)
                horizontalLineToRelative(58.57f)
                curveToRelative(2.14f, 0f, 3.93f, -1.78f, 3.93f, -3.93f)
                verticalLineTo(60f)
                curveToRelative(0f, -3.93f, 3.21f, -7.14f, 7.14f, -7.14f)
                reflectiveCurveToRelative(7.14f, 3.21f, 7.14f, 7.14f)
                verticalLineToRelative(19.29f)
                curveToRelative(0f, 10.12f, -8.21f, 18.21f, -18.21f, 18.21f)
                horizontalLineTo(20.72f)
                arcTo(
                    18.14f,
                    18.14f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    2.51f,
                    79.29f
                )
                moveTo(65.59f, 2.5f)
                curveToRelative(-3.93f, 0f, -7.14f, 3.21f, -7.14f, 7.14f)
                reflectiveCurveToRelative(3.21f, 7.14f, 7.14f, 7.14f)
                horizontalLineToRelative(7.5f)
                lineToRelative(-27.14f, 27.14f)
                curveToRelative(-2.74f, 2.74f, -2.74f, 7.26f, 0f, 10.12f)
                reflectiveCurveToRelative(7.26f, 2.74f, 10.12f, 0f)
                lineToRelative(27.14f, -27.14f)
                verticalLineToRelative(7.5f)
                curveToRelative(0f, 3.93f, 3.21f, 7.14f, 7.14f, 7.14f)
                reflectiveCurveToRelative(7.14f, -3.21f, 7.14f, -7.14f)
                verticalLineTo(10f)
                curveToRelative(0f, -4.17f, -3.33f, -7.5f, -7.5f, -7.5f)
                close()
            }
        }.build()

        return _Export!!
    }

@Suppress("ObjectPropertyName")
private var _Export: ImageVector? = null
