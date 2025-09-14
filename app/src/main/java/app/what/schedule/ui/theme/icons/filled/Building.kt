package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Building: ImageVector
    get() {
        if (_Building != null) {
            return _Building!!
        }
        _Building = ImageVector.Builder(
            name = "Building",
            defaultWidth = 20.dp,
            defaultHeight = 24.dp,
            viewportWidth = 20f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.012f, 0f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 1f)
                verticalLineToRelative(22f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.987f, 1f)
                lineTo(1f, 24f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, -1f)
                lineTo(0f, 6.401f)
                arcToRelative(
                    3f,
                    3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    1.975f,
                    -2.82f
                )
                lineToRelative(9.683f, -3.521f)
                curveToRelative(0.106f, -0.038f, 0.215f, -0.058f, 0.324f, -0.06f)
                horizontalLineToRelative(0.015f)
                close()
                moveTo(15f, 8.198f)
                lineToRelative(3.182f, 1.363f)
                arcTo(
                    3.001f,
                    3.001f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    20f,
                    12.319f
                )
                lineTo(20f, 23f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 1f)
                horizontalLineToRelative(-4.171f)
                curveToRelative(0.111f, -0.313f, 0.171f, -0.649f, 0.171f, -1f)
                close()
                moveTo(8.013f, 17f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 15f)
                horizontalLineToRelative(-3f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 2f)
                horizontalLineToRelative(3f)
                close()
                moveTo(8.013f, 13f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 11f)
                horizontalLineToRelative(-3f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 2f)
                horizontalLineToRelative(3f)
                close()
                moveTo(8.013f, 9f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 7f)
                horizontalLineToRelative(-3f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 2f)
                horizontalLineToRelative(3f)
                close()
            }
        }.build()

        return _Building!!
    }

@Suppress("ObjectPropertyName")
private var _Building: ImageVector? = null
