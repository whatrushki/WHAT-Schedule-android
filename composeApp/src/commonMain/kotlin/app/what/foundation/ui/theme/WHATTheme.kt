package app.what.foundation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalThemeIsDark = compositionLocalOf<Boolean> { error("LocalThemeIsDark is not provided") }

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
    surfaceContainerLow = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerHighest = Color(0xFF2D2D2D),
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFF333333),
    error = WhatRed,
    onError = Color(0xFF000000)
)

val DarkMonochromeScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF262626),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFDDDDDD),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFBBBBBB),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF3A3A3A),
    onTertiaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0E0E0E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFD4D4D4),
    surfaceContainer = Color(0xFF171717),
    surfaceContainerLow = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerHighest = Color(0xFF2D2D2D),
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFF404040),
    error = Color(0xFFFF5555),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF4A1010),
    onErrorContainer = Color(0xFFFFB4AB)
)

val LightMonochromeScheme = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF262626),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF404040),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD4D4D4),
    onTertiaryContainer = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF404040),
    surfaceContainer = Color(0xFFF7F7F7),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainerHigh = Color(0xFFEEEEEE),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFFCCCCCC),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
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
    theme: ColorScheme = WHATDarkColorScheme,
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkTheme
    ) {
        MaterialTheme(
            colorScheme = theme,
            shapes = WHATShapes,
            content = content
        )
    }
}
