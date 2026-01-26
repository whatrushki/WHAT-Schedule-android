package app.what.schedule.ui.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons

val WHATIcons.ReleaseAlert: ImageVector
    get() {
        if (_ReleaseAlert != null) {
            return _ReleaseAlert!!
        }
        _ReleaseAlert = ImageVector.Builder(
            name = "ReleaseAlert",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE3E3E3))) {
                moveToRelative(344f, 900f)
                lineToRelative(-76f, -128f)
                lineToRelative(-144f, -32f)
                lineToRelative(14f, -148f)
                lineToRelative(-98f, -112f)
                lineToRelative(98f, -112f)
                lineToRelative(-14f, -148f)
                lineToRelative(144f, -32f)
                lineToRelative(76f, -128f)
                lineToRelative(136f, 58f)
                lineToRelative(136f, -58f)
                lineToRelative(76f, 128f)
                lineToRelative(144f, 32f)
                lineToRelative(-14f, 148f)
                lineToRelative(98f, 112f)
                lineToRelative(-98f, 112f)
                lineToRelative(14f, 148f)
                lineToRelative(-144f, 32f)
                lineToRelative(-76f, 128f)
                lineToRelative(-136f, -58f)
                lineToRelative(-136f, 58f)
                close()
                moveTo(378f, 798f)
                lineTo(480f, 754f)
                lineTo(584f, 798f)
                lineTo(640f, 702f)
                lineTo(750f, 676f)
                lineTo(740f, 564f)
                lineTo(814f, 480f)
                lineTo(740f, 394f)
                lineTo(750f, 282f)
                lineTo(640f, 258f)
                lineTo(582f, 162f)
                lineTo(480f, 206f)
                lineTo(376f, 162f)
                lineTo(320f, 258f)
                lineTo(210f, 282f)
                lineTo(220f, 394f)
                lineTo(146f, 480f)
                lineTo(220f, 564f)
                lineTo(210f, 678f)
                lineTo(320f, 702f)
                lineTo(378f, 798f)
                close()
                moveTo(480f, 480f)
                close()
                moveTo(480f, 680f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(520f, 640f)
                quadToRelative(0f, -17f, -11.5f, -28.5f)
                reflectiveQuadTo(480f, 600f)
                quadToRelative(-17f, 0f, -28.5f, 11.5f)
                reflectiveQuadTo(440f, 640f)
                quadToRelative(0f, 17f, 11.5f, 28.5f)
                reflectiveQuadTo(480f, 680f)
                close()
                moveTo(440f, 520f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(240f)
                close()
            }
        }.build()

        return _ReleaseAlert!!
    }

@Suppress("ObjectPropertyName")
private var _ReleaseAlert: ImageVector? = null
