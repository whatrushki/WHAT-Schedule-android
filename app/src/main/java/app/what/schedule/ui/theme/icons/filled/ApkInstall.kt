package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.ApkInstall: ImageVector
    get() {
        if (_ApkInstall != null) {
            return _ApkInstall!!
        }
        _ApkInstall = ImageVector.Builder(
            name = "ApkInstall",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE3E3E3))) {
                moveTo(160f, 880f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 800f)
                verticalLineToRelative(-640f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(160f, 80f)
                horizontalLineToRelative(320f)
                lineToRelative(240f, 240f)
                verticalLineToRelative(170f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(-130f)
                lineTo(440f, 360f)
                verticalLineToRelative(-200f)
                lineTo(160f, 160f)
                verticalLineToRelative(640f)
                horizontalLineToRelative(440f)
                verticalLineToRelative(80f)
                lineTo(160f, 880f)
                close()
                moveTo(160f, 800f)
                verticalLineToRelative(-640f)
                verticalLineToRelative(640f)
                close()
                moveTo(200f, 760f)
                quadToRelative(4f, -49f, 30f, -90f)
                reflectiveQuadToRelative(68f, -65f)
                lineToRelative(-38f, -68f)
                quadToRelative(0f, -1f, 4f, -15f)
                quadToRelative(5f, -2f, 9.5f, -2f)
                reflectiveQuadToRelative(6.5f, 5f)
                lineToRelative(39f, 70f)
                quadToRelative(20f, -8f, 40f, -12.5f)
                reflectiveQuadToRelative(41f, -4.5f)
                quadToRelative(21f, 0f, 41f, 4.5f)
                reflectiveQuadToRelative(40f, 12.5f)
                lineToRelative(39f, -70f)
                lineToRelative(15f, -4f)
                quadToRelative(5f, 2f, 6f, 7f)
                reflectiveQuadToRelative(-1f, 9f)
                lineToRelative(-38f, 68f)
                quadToRelative(42f, 24f, 68f, 65f)
                reflectiveQuadToRelative(30f, 90f)
                lineTo(200f, 760f)
                close()
                moveTo(324f, 694f)
                quadToRelative(6f, -6f, 6f, -14f)
                reflectiveQuadToRelative(-6f, -14f)
                quadToRelative(-6f, -6f, -14f, -6f)
                reflectiveQuadToRelative(-14f, 6f)
                quadToRelative(-6f, 6f, -6f, 14f)
                reflectiveQuadToRelative(6f, 14f)
                quadToRelative(6f, 6f, 14f, 6f)
                reflectiveQuadToRelative(14f, -6f)
                close()
                moveTo(504f, 694f)
                quadToRelative(6f, -6f, 6f, -14f)
                reflectiveQuadToRelative(-6f, -14f)
                quadToRelative(-6f, -6f, -14f, -6f)
                reflectiveQuadToRelative(-14f, 6f)
                quadToRelative(-6f, 6f, -6f, 14f)
                reflectiveQuadToRelative(6f, 14f)
                quadToRelative(6f, 6f, 14f, 6f)
                reflectiveQuadToRelative(14f, -6f)
                close()
                moveTo(800f, 880f)
                lineTo(640f, 720f)
                lineToRelative(56f, -57f)
                lineToRelative(64f, 63f)
                verticalLineToRelative(-166f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(166f)
                lineToRelative(64f, -63f)
                lineToRelative(56f, 57f)
                lineTo(800f, 880f)
                close()
            }
        }.build()

        return _ApkInstall!!
    }

@Suppress("ObjectPropertyName")
private var _ApkInstall: ImageVector? = null
