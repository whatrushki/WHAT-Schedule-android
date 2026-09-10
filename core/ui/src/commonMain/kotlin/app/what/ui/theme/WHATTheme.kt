package app.what.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val WhatLime = Color(0xFF94FF28)
val WhatDarkBg = Color(0xFF0E0E0E)
val WhatSurface = Color(0xFF171717)
val WhatSurfaceVariant = Color(0xFF1E1E1E)
val WhatTextPrimary = Color(0xFFFFFFFF)
val WhatTextSecondary = Color(0xFFB0B0B0)
val WhatOrange = Color(0xFFFFB300)
val WhatRed = Color(0xFFFF5252)

val WHATDarkColorScheme: ColorScheme = darkColorScheme(
    primary = WhatLime,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E3A00),
    onPrimaryContainer = Color(0xFFB8FF5C),
    secondary = Color(0xFFDDDDDD),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF262626),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = WhatOrange,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF3E2800),
    onTertiaryContainer = Color(0xFFFFD54F),
    background = WhatDarkBg,
    onBackground = WhatTextPrimary,
    surface = WhatSurface,
    onSurface = WhatTextPrimary,
    surfaceVariant = WhatSurfaceVariant,
    onSurfaceVariant = WhatTextSecondary,
    surfaceContainer = WhatSurface,
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerHighest = Color(0xFF2D2D2D),
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFF333333),
    error = WhatRed,
    onError = Color(0xFF000000)
)

val WHATShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun WHATTheme(
    colorScheme: ColorScheme = WHATDarkColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = WHATShapes,
        content = content
    )
}
