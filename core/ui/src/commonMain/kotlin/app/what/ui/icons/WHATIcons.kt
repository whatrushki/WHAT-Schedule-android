package app.what.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object WHATIcons {

    val Building: ImageVector
        get() {
            if (_Building != null) return _Building!!
            _Building = ImageVector.Builder(
                name = "Building",
                defaultWidth = 20.dp,
                defaultHeight = 24.dp,
                viewportWidth = 20f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.White)) {
                    moveTo(12.012f, 0f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 1f)
                    verticalLineToRelative(22f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.987f, 1f)
                    lineTo(1f, 24f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, -1f)
                    lineTo(0f, 6.401f)
                    arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.975f, -2.82f)
                    lineToRelative(9.683f, -3.521f)
                    curveToRelative(0.106f, -0.038f, 0.215f, -0.058f, 0.324f, -0.06f)
                    horizontalLineToRelative(0.015f)
                    close()
                    moveTo(15f, 8.198f)
                    lineToRelative(3.182f, 1.363f)
                    arcTo(3.001f, 3.001f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 12.319f)
                    lineTo(20f, 23f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 1f)
                    horizontalLineToRelative(-4.171f)
                    curveToRelative(0.111f, -0.313f, 0.171f, -0.649f, 0.171f, -1f)
                    close()
                    moveTo(8.013f, 17f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 15f)
                    horizontalLineToRelative(-3f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 2f)
                    horizontalLineToRelative(3f)
                    close()
                    moveTo(8.013f, 13f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 11f)
                    horizontalLineToRelative(-3f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 2f)
                    horizontalLineToRelative(3f)
                    close()
                    moveTo(8.013f, 9f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 7f)
                    horizontalLineToRelative(-3f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 2f)
                    horizontalLineToRelative(3f)
                    close()
                }
            }.build()
            return _Building!!
        }

    val Room: ImageVector
        get() {
            if (_Room != null) return _Room!!
            _Room = ImageVector.Builder(
                name = "Room",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 32f,
                viewportHeight = 32f
            ).apply {
                path(fill = SolidColor(Color.White)) {
                    moveTo(5f, 6f)
                    verticalLineToRelative(20f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                    horizontalLineToRelative(4f)
                    verticalLineTo(5f)
                    horizontalLineTo(6f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1f, 1f)
                    close()
                    moveTo(26.225f, 5.025f)
                    lineToRelative(-13f, -3f)
                    arcToRelative(1.014f, 1.014f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.848f, 0.193f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 3f)
                    verticalLineToRelative(26f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                    arcToRelative(1.037f, 1.037f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.225f, -0.025f)
                    lineToRelative(13f, -3f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 27f, 26f)
                    verticalLineTo(6f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.775f, -0.975f)
                    close()
                    moveTo(17f, 18f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 0f)
                    verticalLineToRelative(-4f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 0f)
                    close()
                }
            }.build()
            return _Room!!
        }

    val Person: ImageVector
        get() {
            if (_Person != null) return _Person!!
            _Person = ImageVector.Builder(
                name = "Person",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 36f,
                viewportHeight = 36f
            ).apply {
                path(fill = SolidColor(Color.White)) {
                    moveTo(27.5f, 34f)
                    horizontalLineToRelative(-19f)
                    curveToRelative(-1.3f, 0f, -2.6f, -0.7f, -3.3f, -1.8f)
                    curveToRelative(-0.8f, -1.1f, -0.9f, -2.5f, -0.4f, -3.8f)
                    curveToRelative(2f, -5.1f, 7.3f, -8.5f, 13.2f, -8.5f)
                    reflectiveCurveToRelative(11.2f, 3.4f, 13.2f, 8.5f)
                    curveToRelative(0.5f, 1.2f, 0.4f, 2.7f, -0.4f, 3.8f)
                    curveToRelative(-0.7f, 1.1f, -2f, 1.8f, -3.3f, 1.8f)
                    close()
                    moveTo(18f, 18f)
                    curveToRelative(-4.4f, 0f, -8f, -3.6f, -8f, -8f)
                    reflectiveCurveToRelative(3.6f, -8f, 8f, -8f)
                    reflectiveCurveToRelative(8f, 3.6f, 8f, 8f)
                    reflectiveCurveToRelative(-3.6f, 8f, -8f, 8f)
                    close()
                }
            }.build()
            return _Person!!
        }

    val Group: ImageVector
        get() {
            if (_Group != null) return _Group!!
            _Group = ImageVector.Builder(
                name = "Group",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 30f,
                viewportHeight = 24.43f
            ).apply {
                path(fill = SolidColor(Color.White)) {
                    moveTo(10.429f, 11.43f)
                    arcToRelative(5.715f, 5.715f, 0f, isMoreThanHalf = true, isPositiveArc = false, -5.715f, -5.714f)
                    arcTo(5.72f, 5.72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10.429f, 11.43f)
                    moveToRelative(7.476f, 4.721f)
                    arcTo(10.434f, 10.434f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 23.43f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                    horizontalLineToRelative(18.86f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, -1f)
                    arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.485f, -3.124f)
                    arcToRelative(10.36f, 10.36f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.47f, -4.155f)
                }
                path(fill = SolidColor(Color.White)) {
                    moveTo(22.5f, 7.68f)
                    moveToRelative(-4.25f, 0f)
                    arcToRelative(4.25f, 4.25f, 0f, isMoreThanHalf = true, isPositiveArc = true, 8.5f, 0f)
                    arcToRelative(4.25f, 4.25f, 0f, isMoreThanHalf = true, isPositiveArc = true, -8.5f, 0f)
                }
                path(fill = SolidColor(Color.White)) {
                    moveTo(22.5f, 13.1f)
                    arcToRelative(7.5f, 7.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.806f, 1.057f)
                    curveToRelative(0.217f, 0.194f, 0.436f, 0.385f, 0.641f, 0.595f)
                    arcToRelative(12.4f, 12.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.952f, 4.966f)
                    arcToRelative(11.5f, 11.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.437f, 1.882f)
                    lineTo(29f, 21.6f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, -1f)
                    arcToRelative(7.51f, 7.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, -7.5f, -7.5f)
                }
            }.build()
            return _Group!!
        }

    val Run: ImageVector
        get() {
            if (_Run != null) return _Run!!
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

    private var _Building: ImageVector? = null
    private var _Room: ImageVector? = null
    private var _Person: ImageVector? = null
    private var _Group: ImageVector? = null
    private var _Run: ImageVector? = null
}
