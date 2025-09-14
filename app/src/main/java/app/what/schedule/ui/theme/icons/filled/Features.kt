package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Features: ImageVector
    get() {
        if (_Features != null) {
            return _Features!!
        }
        _Features = ImageVector.Builder(
            name = "Features",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 3f)
                lineTo(12f, 3f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 6f)
                lineTo(15f, 12f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 15f)
                lineTo(6f, 15f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 12f)
                lineTo(3f, 6f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 3f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(10.4f, 17.036f)
                arcToRelative(
                    1.04f,
                    1.04f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.732f,
                    0f
                )
                lineTo(3.206f, 26.5f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.866f,
                    1.5f
                )
                horizontalLineTo(15f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.866f,
                    -1.5f
                )
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(23f, 9f)
                moveToRelative(-6f, 0f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 0f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, 0f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(27.282f, 18.025f)
                arcToRelative(
                    3.91f,
                    3.91f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -4.852f,
                    0.456f
                )
                arcToRelative(
                    3.863f,
                    3.863f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -6.552f,
                    2.393f
                )
                arcToRelative(
                    3.86f,
                    3.86f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.115f,
                    3.1f
                )
                lineTo(21.016f, 28f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.829f, 0f)
                lineToRelative(4.022f, -4.023f)
                arcToRelative(
                    3.87f,
                    3.87f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.585f,
                    -5.952f
                )
            }
        }.build()

        return _Features!!
    }

@Suppress("ObjectPropertyName")
private var _Features: ImageVector? = null
