package app.what.foundation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalThemeIsDark = compositionLocalOf<Boolean> { error("LocalThemeIsDark is not provided") }

@Composable
fun WHATTheme(
    theme: ColorScheme,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        else -> theme
    }

    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}