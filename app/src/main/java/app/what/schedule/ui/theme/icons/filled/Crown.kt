package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Crown: ImageVector
    get() {
        if (_Crown != null) {
            return _Crown!!
        }
        _Crown = ImageVector.Builder(
            name = "Crown",
            defaultWidth = 21.543.dp,
            defaultHeight = 15.949.dp,
            viewportWidth = 21.543f,
            viewportHeight = 15.949f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.758f, 4.869f)
                arcToRelative(
                    1.72f,
                    1.72f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.93f,
                    0f
                )
                lineToRelative(-3f, 1.86f)
                arcToRelative(
                    0.23f,
                    0.23f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.19f,
                    0f
                )
                arcToRelative(
                    0.27f,
                    0.27f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.16f,
                    -0.12f
                )
                lineToRelative(-3.2f, -5.76f)
                arcToRelative(
                    1.75f,
                    1.75f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -3f,
                    0f
                )
                lineTo(5.958f, 6.599f)
                arcToRelative(
                    0.27f,
                    0.27f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.16f,
                    0.12f
                )
                arcToRelative(
                    0.23f,
                    0.23f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.19f,
                    0f
                )
                lineTo(2.658f, 4.829f)
                arcToRelative(
                    1.72f,
                    1.72f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.9f,
                    0.04f
                )
                arcToRelative(
                    1.74f,
                    1.74f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.72f,
                    1.8f
                )
                lineToRelative(1.68f, 7.89f)
                arcToRelative(
                    1.75f,
                    1.75f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.71f,
                    1.39f
                )
                horizontalLineToRelative(14.69f)
                arcToRelative(
                    1.75f,
                    1.75f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.71f,
                    -1.39f
                )
                lineToRelative(1.68f, -7.89f)
                arcToRelative(
                    1.74f,
                    1.74f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.75f,
                    -1.8f
                )
                close()
            }
        }.build()

        return _Crown!!
    }

@Suppress("ObjectPropertyName")
private var _Crown: ImageVector? = null
