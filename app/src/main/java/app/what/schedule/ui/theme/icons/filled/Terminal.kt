package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Terminal: ImageVector
    get() {
        if (_Terminal != null) {
            return _Terminal!!
        }
        _Terminal = ImageVector.Builder(
            name = "Terminal",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 64f,
            viewportHeight = 64f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(18f, 2f)
                curveTo(9.163f, 2f, 2f, 9.163f, 2f, 18f)
                verticalLineToRelative(28f)
                curveToRelative(0f, 8.837f, 7.163f, 16f, 16f, 16f)
                horizontalLineToRelative(28f)
                curveToRelative(8.837f, 0f, 16f, -7.163f, 16f, -16f)
                lineTo(62f, 18f)
                curveToRelative(0f, -8.837f, -7.163f, -16f, -16f, -16f)
                close()
                moveTo(28f, 32f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.662f,
                    1.487f
                )
                lineToRelative(-10f, 9f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -2.676f,
                    -2.974f
                )
                lineTo(23.01f, 32f)
                lineToRelative(-8.348f, -7.513f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    2.676f,
                    -2.974f
                )
                lineToRelative(10f, 9f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28f, 32f)
                moveToRelative(20f, 11f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 0f, -4f)
                lineTo(32f, 39f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 0f, 4f)
                close()
            }
        }.build()

        return _Terminal!!
    }

@Suppress("ObjectPropertyName")
private var _Terminal: ImageVector? = null
