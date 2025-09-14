package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Person: ImageVector
    get() {
        if (_Person != null) {
            return _Person!!
        }
        _Person = ImageVector.Builder(
            name = "Person",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 36f,
            viewportHeight = 36f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(27.5f, 34f)
                horizontalLineToRelative(-19f)
                curveToRelative(-1.3f, 0f, -2.6f, -0.7f, -3.3f, -1.8f)
                curveToRelative(-0.8f, -1.1f, -0.9f, -2.5f, -0.4f, -3.8f)
                curveToRelative(2f, -5.1f, 7.3f, -8.5f, 13.2f, -8.5f)
                reflectiveCurveToRelative(11.2f, 3.4f, 13.2f, 8.5f)
                curveToRelative(0.5f, 1.2f, 0.4f, 2.7f, -0.4f, 3.8f)
                curveToRelative(-0.7f, 1.1f, -2f, 1.8f, -3.3f, 1.8f)
                close()
                moveTo(18f, 18f)
                curveToRelative(-4.4f, 0f, -8f, -3.6f, -8f, -8f)
                reflectiveCurveToRelative(3.6f, -8f, 8f, -8f)
                reflectiveCurveToRelative(8f, 3.6f, 8f, 8f)
                reflectiveCurveToRelative(-3.6f, 8f, -8f, 8f)
                close()
            }
        }.build()

        return _Person!!
    }

@Suppress("ObjectPropertyName")
private var _Person: ImageVector? = null
