package app.what.schedule.presentation.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.presentation.theme.icons.WHATIcons

val WHATIcons.MeetingRoom: ImageVector
    get() {
        if (_MeetingRoom != null) {
            return _MeetingRoom!!
        }
        _MeetingRoom = ImageVector.Builder(
            name = "MeetingRoom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE8EAED))) {
                moveTo(120f, 840f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-640f)
                horizontalLineToRelative(400f)
                verticalLineToRelative(40f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(600f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(80f)
                lineTo(680f, 840f)
                verticalLineToRelative(-600f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(600f)
                lineTo(120f, 840f)
                close()
                moveTo(440f, 520f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(480f, 480f)
                quadToRelative(0f, -17f, -11.5f, -28.5f)
                reflectiveQuadTo(440f, 440f)
                quadToRelative(-17f, 0f, -28.5f, 11.5f)
                reflectiveQuadTo(400f, 480f)
                quadToRelative(0f, 17f, 11.5f, 28.5f)
                reflectiveQuadTo(440f, 520f)
                close()
            }
        }.build()

        return _MeetingRoom!!
    }

@Suppress("ObjectPropertyName")
private var _MeetingRoom: ImageVector? = null
