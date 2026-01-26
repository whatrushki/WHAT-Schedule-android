package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.News: ImageVector
    get() {
        if (_News != null) {
            return _News!!
        }
        _News = ImageVector.Builder(
            name = "News",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE3E3E3))) {
                moveTo(160f, 840f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 760f)
                verticalLineToRelative(-640f)
                lineToRelative(67f, 67f)
                lineToRelative(66f, -67f)
                lineToRelative(67f, 67f)
                lineToRelative(67f, -67f)
                lineToRelative(66f, 67f)
                lineToRelative(67f, -67f)
                lineToRelative(67f, 67f)
                lineToRelative(66f, -67f)
                lineToRelative(67f, 67f)
                lineToRelative(67f, -67f)
                lineToRelative(66f, 67f)
                lineToRelative(67f, -67f)
                verticalLineToRelative(640f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(800f, 840f)
                lineTo(160f, 840f)
                close()
                moveTo(160f, 760f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(-240f)
                lineTo(160f, 520f)
                verticalLineToRelative(240f)
                close()
                moveTo(520f, 760f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(-80f)
                lineTo(520f, 680f)
                verticalLineToRelative(80f)
                close()
                moveTo(520f, 600f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(-80f)
                lineTo(520f, 520f)
                verticalLineToRelative(80f)
                close()
                moveTo(160f, 440f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(-120f)
                lineTo(160f, 320f)
                verticalLineToRelative(120f)
                close()
            }
        }.build()

        return _News!!
    }

@Suppress("ObjectPropertyName")
private var _News: ImageVector? = null
