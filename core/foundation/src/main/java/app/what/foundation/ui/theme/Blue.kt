package app.what.foundation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val blueLightScheme by lazy {
    lightColorScheme(
        primary = Color(0xFF555A92),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE0E0FF),
        onPrimaryContainer = Color(0xFF3D4279),
        secondary = Color(0xFF5C5D72),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1E0F9),
        onSecondaryContainer = Color(0xFF444559),
        tertiary = Color(0xFF78536B),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD7EE),
        onTertiaryContainer = Color(0xFF5E3C53),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFFBF8FF),
        onBackground = Color(0xFF1B1B21),
        surface = Color(0xFFFBF8FF),
        onSurface = Color(0xFF1B1B21),
        surfaceVariant = Color(0xFFE3E1EC),
        onSurfaceVariant = Color(0xFF46464F),
        outline = Color(0xFF777680),
        outlineVariant = Color(0xFFC7C5D0),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF303036),
        inverseOnSurface = Color(0xFFF2EFF7),
        inversePrimary = Color(0xFFBEC2FF),
        surfaceDim = Color(0xFFDBD9E0),
        surfaceBright = Color(0xFFFBF8FF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F2FA),
        surfaceContainer = Color(0xFFEFEDF4),
        surfaceContainerHigh = Color(0xFFEAE7EF),
        surfaceContainerHighest = Color(0xFFE4E1E9)
    )
}

internal val blueDarkScheme by lazy {
    darkColorScheme(
        primary = Color(0xFFBEC2FF),
        onPrimary = Color(0xFF262B61),
        primaryContainer = Color(0xFF3D4279),
        onPrimaryContainer = Color(0xFFE0E0FF),
        secondary = Color(0xFFC5C4DD),
        onSecondary = Color(0xFF2E2F42),
        secondaryContainer = Color(0xFF444559),
        onSecondaryContainer = Color(0xFFE1E0F9),
        tertiary = Color(0xFFE7B9D5),
        onTertiary = Color(0xFF45263C),
        tertiaryContainer = Color(0xFF5E3C53),
        onTertiaryContainer = Color(0xFFFFD7EE),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF131318),
        onBackground = Color(0xFFE4E1E9),
        surface = Color(0xFF131318),
        onSurface = Color(0xFFE4E1E9),
        surfaceVariant = Color(0xFF46464F),
        onSurfaceVariant = Color(0xFFC7C5D0),
        outline = Color(0xFF91909A),
        outlineVariant = Color(0xFF46464F),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE4E1E9),
        inverseOnSurface = Color(0xFF303036),
        inversePrimary = Color(0xFF555A92),
        surfaceDim = Color(0xFF131318),
        surfaceBright = Color(0xFF39393F),
        surfaceContainerLowest = Color(0xFF0E0E13),
        surfaceContainerLow = Color(0xFF1B1B21),
        surfaceContainer = Color(0xFF1F1F25),
        surfaceContainerHigh = Color(0xFF2A292F),
        surfaceContainerHighest = Color(0xFF34343A)
    )
}