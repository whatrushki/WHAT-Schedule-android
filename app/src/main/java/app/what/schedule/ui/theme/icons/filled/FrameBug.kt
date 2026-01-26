package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.FrameBug: ImageVector
    get() {
        if (_FrameBug != null) {
            return _FrameBug!!
        }
        _FrameBug = ImageVector.Builder(
            name = "FrameBug",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE3E3E3))) {
                moveTo(480f, 760f)
                quadToRelative(66f, 0f, 113f, -47f)
                reflectiveQuadToRelative(47f, -113f)
                verticalLineToRelative(-160f)
                quadToRelative(0f, -66f, -47f, -113f)
                reflectiveQuadToRelative(-113f, -47f)
                quadToRelative(-66f, 0f, -113f, 47f)
                reflectiveQuadToRelative(-47f, 113f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 66f, 47f, 113f)
                reflectiveQuadToRelative(113f, 47f)
                close()
                moveTo(400f, 640f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(-80f)
                lineTo(400f, 560f)
                verticalLineToRelative(80f)
                close()
                moveTo(400f, 480f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(-80f)
                lineTo(400f, 400f)
                verticalLineToRelative(80f)
                close()
                moveTo(480f, 520f)
                close()
                moveTo(480f, 840f)
                quadToRelative(-65f, 0f, -120.5f, -32f)
                reflectiveQuadTo(272f, 720f)
                lineTo(160f, 720f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(84f)
                quadToRelative(-3f, -20f, -3.5f, -40f)
                reflectiveQuadToRelative(-0.5f, -40f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(80f)
                quadToRelative(0f, -20f, 0.5f, -40f)
                reflectiveQuadToRelative(3.5f, -40f)
                horizontalLineToRelative(-84f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(112f)
                quadToRelative(14f, -23f, 31.5f, -43f)
                reflectiveQuadToRelative(40.5f, -35f)
                lineToRelative(-64f, -66f)
                lineToRelative(56f, -56f)
                lineToRelative(86f, 86f)
                quadToRelative(28f, -9f, 57f, -9f)
                reflectiveQuadToRelative(57f, 9f)
                lineToRelative(88f, -86f)
                lineToRelative(56f, 56f)
                lineToRelative(-66f, 66f)
                quadToRelative(23f, 15f, 41.5f, 34.5f)
                reflectiveQuadTo(688f, 320f)
                horizontalLineToRelative(112f)
                verticalLineToRelative(80f)
                horizontalLineToRelative(-84f)
                quadToRelative(3f, 20f, 3.5f, 40f)
                reflectiveQuadToRelative(0.5f, 40f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(80f)
                horizontalLineToRelative(-80f)
                quadToRelative(0f, 20f, -0.5f, 40f)
                reflectiveQuadToRelative(-3.5f, 40f)
                horizontalLineToRelative(84f)
                verticalLineToRelative(80f)
                lineTo(688f, 720f)
                quadToRelative(-32f, 56f, -87.5f, 88f)
                reflectiveQuadTo(480f, 840f)
                close()
                moveTo(40f, 240f)
                verticalLineToRelative(-120f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(120f, 40f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(80f)
                lineTo(120f, 120f)
                verticalLineToRelative(120f)
                lineTo(40f, 240f)
                close()
                moveTo(240f, 920f)
                lineTo(120f, 920f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(40f, 840f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(120f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(80f)
                close()
                moveTo(720f, 920f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(120f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(840f, 920f)
                lineTo(720f, 920f)
                close()
                moveTo(840f, 240f)
                verticalLineToRelative(-120f)
                lineTo(720f, 120f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(120f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(920f, 120f)
                verticalLineToRelative(120f)
                horizontalLineToRelative(-80f)
                close()
            }
        }.build()

        return _FrameBug!!
    }

@Suppress("ObjectPropertyName")
private var _FrameBug: ImageVector? = null
