package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.Quote: ImageVector
    get() {
        if (_Quote != null) {
            return _Quote!!
        }
        _Quote = ImageVector.Builder(
            name = "Share",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE3E3E3))) {
                moveToRelative(228f, 720f)
                lineToRelative(92f, -160f)
                quadToRelative(-66f, 0f, -113f, -47f)
                reflectiveQuadToRelative(-47f, -113f)
                quadToRelative(0f, -66f, 47f, -113f)
                reflectiveQuadToRelative(113f, -47f)
                quadToRelative(66f, 0f, 113f, 47f)
                reflectiveQuadToRelative(47f, 113f)
                quadToRelative(0f, 23f, -5.5f, 42.5f)
                reflectiveQuadTo(458f, 480f)
                lineTo(320f, 720f)
                horizontalLineToRelative(-92f)
                close()
                moveTo(588f, 720f)
                lineTo(680f, 560f)
                quadToRelative(-66f, 0f, -113f, -47f)
                reflectiveQuadToRelative(-47f, -113f)
                quadToRelative(0f, -66f, 47f, -113f)
                reflectiveQuadToRelative(113f, -47f)
                quadToRelative(66f, 0f, 113f, 47f)
                reflectiveQuadToRelative(47f, 113f)
                quadToRelative(0f, 23f, -5.5f, 42.5f)
                reflectiveQuadTo(818f, 480f)
                lineTo(680f, 720f)
                horizontalLineToRelative(-92f)
                close()
                moveTo(320f, 460f)
                quadToRelative(25f, 0f, 42.5f, -17.5f)
                reflectiveQuadTo(380f, 400f)
                quadToRelative(0f, -25f, -17.5f, -42.5f)
                reflectiveQuadTo(320f, 340f)
                quadToRelative(-25f, 0f, -42.5f, 17.5f)
                reflectiveQuadTo(260f, 400f)
                quadToRelative(0f, 25f, 17.5f, 42.5f)
                reflectiveQuadTo(320f, 460f)
                close()
                moveTo(680f, 460f)
                quadToRelative(25f, 0f, 42.5f, -17.5f)
                reflectiveQuadTo(740f, 400f)
                quadToRelative(0f, -25f, -17.5f, -42.5f)
                reflectiveQuadTo(680f, 340f)
                quadToRelative(-25f, 0f, -42.5f, 17.5f)
                reflectiveQuadTo(620f, 400f)
                quadToRelative(0f, 25f, 17.5f, 42.5f)
                reflectiveQuadTo(680f, 460f)
                close()
                moveTo(680f, 400f)
                close()
                moveTo(320f, 400f)
                close()
            }
        }.build()

        return _Quote!!
    }

@Suppress("ObjectPropertyName")
private var _Quote: ImageVector? = null
