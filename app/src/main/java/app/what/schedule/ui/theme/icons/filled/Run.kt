package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Run: ImageVector
    get() {
        if (_Run != null) {
            return _Run!!
        }
        _Run = ImageVector.Builder(
            name = "Run",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE3E3E3))) {
                moveTo(520f, 920f)
                verticalLineToRelative(-240f)
                lineToRelative(-84f, -80f)
                lineToRelative(-40f, 176f)
                lineToRelative(-276f, -56f)
                lineToRelative(16f, -80f)
                lineToRelative(192f, 40f)
                lineToRelative(64f, -324f)
                lineToRelative(-72f, 28f)
                verticalLineToRelative(136f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(-188f)
                lineToRelative(158f, -68f)
                quadToRelative(35f, -15f, 51.5f, -19.5f)
                reflectiveQuadTo(480f, 240f)
                quadToRelative(21f, 0f, 39f, 11f)
                reflectiveQuadToRelative(29f, 29f)
                lineToRelative(40f, 64f)
                quadToRelative(26f, 42f, 70.5f, 69f)
                reflectiveQuadTo(760f, 440f)
                verticalLineToRelative(80f)
                quadToRelative(-66f, 0f, -123.5f, -27.5f)
                reflectiveQuadTo(540f, 420f)
                lineToRelative(-24f, 120f)
                lineToRelative(84f, 80f)
                verticalLineToRelative(300f)
                horizontalLineToRelative(-80f)
                close()
                moveTo(483.5f, 196.5f)
                quadTo(460f, 173f, 460f, 140f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                quadTo(507f, 60f, 540f, 60f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                quadTo(620f, 107f, 620f, 140f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                quadTo(573f, 220f, 540f, 220f)
                reflectiveQuadToRelative(-56.5f, -23.5f)
                close()
            }
        }.build()

        return _Run!!
    }

@Suppress("ObjectPropertyName")
private var _Run: ImageVector? = null
