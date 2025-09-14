package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Room: ImageVector
    get() {
        if (_Room != null) {
            return _Room!!
        }
        _Room = ImageVector.Builder(
            name = "Room",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 6f)
                verticalLineToRelative(20f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                horizontalLineToRelative(4f)
                verticalLineTo(5f)
                horizontalLineTo(6f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1f, 1f)
                close()
                moveTo(26.225f, 5.025f)
                lineToRelative(-13f, -3f)
                arcToRelative(
                    1.014f,
                    1.014f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.848f,
                    0.193f
                )
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 3f)
                verticalLineToRelative(26f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                arcToRelative(
                    1.037f,
                    1.037f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.225f,
                    -0.025f
                )
                lineToRelative(13f, -3f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 27f, 26f)
                verticalLineTo(6f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.775f,
                    -0.975f
                )
                close()
                moveTo(17f, 18f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 0f)
                verticalLineToRelative(-4f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 0f)
                close()
            }
        }.build()

        return _Room!!
    }

@Suppress("ObjectPropertyName")
private var _Room: ImageVector? = null
