package app.what.schedule.presentation.theme.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.what.schedule.presentation.theme.icons.WHATIcons

val WHATIcons.Person: ImageVector
    get() {
        if (_Person24DpE8EAEDFILL1Wght400GRAD0Opsz24 != null) {
            return _Person24DpE8EAEDFILL1Wght400GRAD0Opsz24!!
        }
        _Person24DpE8EAEDFILL1Wght400GRAD0Opsz24 = ImageVector.Builder(
            name = "Filled.Person24DpE8EAEDFILL1Wght400GRAD0Opsz24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFFE8EAED))) {
                moveTo(480f, 480f)
                quadToRelative(-66f, 0f, -113f, -47f)
                reflectiveQuadToRelative(-47f, -113f)
                quadToRelative(0f, -66f, 47f, -113f)
                reflectiveQuadToRelative(113f, -47f)
                quadToRelative(66f, 0f, 113f, 47f)
                reflectiveQuadToRelative(47f, 113f)
                quadToRelative(0f, 66f, -47f, 113f)
                reflectiveQuadToRelative(-113f, 47f)
                close()
                moveTo(160f, 800f)
                verticalLineToRelative(-112f)
                quadToRelative(0f, -34f, 17.5f, -62.5f)
                reflectiveQuadTo(224f, 582f)
                quadToRelative(62f, -31f, 126f, -46.5f)
                reflectiveQuadTo(480f, 520f)
                quadToRelative(66f, 0f, 130f, 15.5f)
                reflectiveQuadTo(736f, 582f)
                quadToRelative(29f, 15f, 46.5f, 43.5f)
                reflectiveQuadTo(800f, 688f)
                verticalLineToRelative(112f)
                lineTo(160f, 800f)
                close()
            }
        }.build()

        return _Person24DpE8EAEDFILL1Wght400GRAD0Opsz24!!
    }

@Suppress("ObjectPropertyName")
private var _Person24DpE8EAEDFILL1Wght400GRAD0Opsz24: ImageVector? = null
