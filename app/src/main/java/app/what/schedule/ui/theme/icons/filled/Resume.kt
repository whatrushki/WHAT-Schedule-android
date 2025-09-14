package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Resume: ImageVector
    get() {
        if (_Resume != null) {
            return _Resume!!
        }
        _Resume = ImageVector.Builder(
            name = "Resume",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 163.86f,
            viewportHeight = 163.86f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(34.86f, 3.61f)
                curveTo(20.08f, -4.86f, 8.11f, 2.08f, 8.11f, 19.11f)
                verticalLineToRelative(125.64f)
                curveToRelative(0f, 17.04f, 11.98f, 23.98f, 26.75f, 15.51f)
                lineTo(144.67f, 97.28f)
                curveToRelative(14.78f, -8.48f, 14.78f, -22.21f, 0f, -30.69f)
                lineTo(34.86f, 3.61f)
                close()
            }
        }.build()

        return _Resume!!
    }

@Suppress("ObjectPropertyName")
private var _Resume: ImageVector? = null
