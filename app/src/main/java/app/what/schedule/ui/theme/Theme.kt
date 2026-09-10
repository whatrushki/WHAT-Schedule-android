package app.what.schedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import app.what.foundation.ui.theme.WHATTheme
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.local.settings.ThemeStyle
import app.what.schedule.data.local.settings.ThemeType
import app.what.schedule.data.local.settings.rememberAppValues
import com.materialkolor.ktx.DynamicScheme
import com.materialkolor.toColorScheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

private val DarkMonochromeScheme = darkColorScheme(
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

private val LightMonochromeScheme = lightColorScheme(
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

@Composable
fun AppTheme(
    settings: AppValues = rememberAppValues(),
    content: @Composable () -> Unit
) {
    val themeType by settings.themeType.collect()
    val themeStyle by settings.themeStyle.collect()
    val themeColor by settings.themeColor.collect()
    
    val isDarkTheme = when (themeType) {
        ThemeType.Dark -> true
        ThemeType.System -> isSystemInDarkTheme()
        else -> false
    }
    
    val theme = when (themeStyle) {
        ThemeStyle.Monochrome -> if (isDarkTheme) DarkMonochromeScheme else LightMonochromeScheme
        ThemeStyle.CustomColor -> DynamicScheme(Color(themeColor!!), isDarkTheme).toColorScheme()
        else -> DynamicScheme(Color(0xFF94FF28), isDarkTheme).toColorScheme()
    }
    
    WHATTheme(
        theme = theme,
        dynamicColor = themeStyle == ThemeStyle.Material,
        isDarkTheme = isDarkTheme,
        content = content
    )
}