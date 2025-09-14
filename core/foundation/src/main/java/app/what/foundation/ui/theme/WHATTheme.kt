package app.what.foundation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun WHATTheme(
    theme: Theme = GreenTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> theme.dark
        else -> theme.light
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

class Theme(
    val light: ColorScheme,
    val dark: ColorScheme
)

val BrownTheme by lazy { Theme(brownLightScheme, brownDarkScheme) }
val GreenTheme by lazy { Theme(greenLightScheme, greenDarkScheme) }
val BlueTheme by lazy { Theme(blueLightScheme, blueDarkScheme) }