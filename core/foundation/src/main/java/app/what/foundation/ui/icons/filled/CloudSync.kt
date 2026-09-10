package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.CloudSync: ImageVector
    get() {
        if (_CloudSync != null) {
            return _CloudSync!!
        }
        _CloudSync = ImageVector.Builder(
            name = "CloudSync",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE3E3E3))) {
                moveTo(160f, 800f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(109f)
                quadToRelative(-51f, -44f, -80f, -106f)
                reflectiveQuadToRelative(-29f, -134f)
                quadToRelative(0f, -112f, 68f, -197.5f)
                reflectiveQuadTo(400f, 170f)
                verticalLineToRelative(84f)
                quadToRelative(-70f, 25f, -115f, 86.5f)
                reflectiveQuadTo(240f, 480f)
                quadToRelative(0f, 54f, 21.5f, 99.5f)
                reflectiveQuadTo(320f, 658f)
                verticalLineToRelative(-98f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(240f)
                lineTo(160f, 800f)
                close()
                moveTo(600f, 800f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadToRelative(-35f, -85f)
                quadToRelative(0f, -48f, 33f, -82.5f)
                reflectiveQuadToRelative(81f, -36.5f)
                quadToRelative(17f, -36f, 50.5f, -58.5f)
                reflectiveQuadTo(720f, 480f)
                quadToRelative(53f, 0f, 91.5f, 34.5f)
                reflectiveQuadTo(858f, 600f)
                quadToRelative(42f, 0f, 72f, 29f)
                reflectiveQuadToRelative(30f, 70f)
                quadToRelative(0f, 42f, -29f, 71.5f)
                reflectiveQuadTo(860f, 800f)
                lineTo(600f, 800f)
                close()
                moveTo(716f, 440f)
                quadToRelative(-7f, -41f, -27f, -76f)
                reflectiveQuadToRelative(-49f, -62f)
                verticalLineToRelative(98f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(240f)
                verticalLineToRelative(80f)
                lineTo(691f, 240f)
                quadToRelative(43f, 38f, 70.5f, 89f)
                reflectiveQuadTo(797f, 440f)
                horizontalLineToRelative(-81f)
                close()
                moveTo(600f, 720f)
                horizontalLineToRelative(260f)
                quadToRelative(8f, 0f, 14f, -6f)
                reflectiveQuadToRelative(6f, -14f)
                quadToRelative(0f, -8f, -6f, -14f)
                reflectiveQuadToRelative(-14f, -6f)
                horizontalLineToRelative(-70f)
                verticalLineToRelative(-50f)
                quadToRelative(0f, -29f, -20.5f, -49.5f)
                reflectiveQuadTo(720f, 560f)
                quadToRelative(-29f, 0f, -49.5f, 20.5f)
                reflectiveQuadTo(650f, 630f)
                verticalLineToRelative(10f)
                horizontalLineToRelative(-50f)
                quadToRelative(-17f, 0f, -28.5f, 11.5f)
                reflectiveQuadTo(560f, 680f)
                quadToRelative(0f, 17f, 11.5f, 28.5f)
                reflectiveQuadTo(600f, 720f)
                close()
                moveTo(720f, 640f)
                close()
            }
        }.build()
        
        return _CloudSync!!
    }

@Suppress("ObjectPropertyName")
private var _CloudSync: ImageVector? = null
