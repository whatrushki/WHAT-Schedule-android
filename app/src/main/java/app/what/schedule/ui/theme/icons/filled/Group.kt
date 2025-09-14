package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Group: ImageVector
    get() {
        if (_Group != null) {
            return _Group!!
        }
        _Group = ImageVector.Builder(
            name = "Group",
            defaultWidth = 30.dp,
            defaultHeight = 24.43.dp,
            viewportWidth = 30f,
            viewportHeight = 24.43f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10.429f, 11.43f)
                arcToRelative(
                    5.715f,
                    5.715f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -5.715f,
                    -5.714f
                )
                arcTo(
                    5.72f,
                    5.72f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    10.429f,
                    11.43f
                )
                moveToRelative(7.476f, 4.721f)
                arcTo(
                    10.434f,
                    10.434f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    23.43f
                )
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                horizontalLineToRelative(18.86f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, -1f)
                arcToRelative(
                    10f,
                    10f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.485f,
                    -3.124f
                )
                arcToRelative(
                    10.36f,
                    10.36f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -2.47f,
                    -4.155f
                )
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(22.5f, 7.68f)
                moveToRelative(-4.25f, 0f)
                arcToRelative(
                    4.25f,
                    4.25f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    8.5f,
                    0f
                )
                arcToRelative(
                    4.25f,
                    4.25f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -8.5f,
                    0f
                )
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(22.5f, 13.1f)
                arcToRelative(
                    7.5f,
                    7.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -3.806f,
                    1.057f
                )
                curveToRelative(0.217f, 0.194f, 0.436f, 0.385f, 0.641f, 0.595f)
                arcToRelative(
                    12.4f,
                    12.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    2.952f,
                    4.966f
                )
                arcToRelative(
                    11.5f,
                    11.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0.437f,
                    1.882f
                )
                lineTo(29f, 21.6f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, -1f)
                arcToRelative(
                    7.51f,
                    7.51f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -7.5f,
                    -7.5f
                )
            }
        }.build()

        return _Group!!
    }

@Suppress("ObjectPropertyName")
private var _Group: ImageVector? = null
