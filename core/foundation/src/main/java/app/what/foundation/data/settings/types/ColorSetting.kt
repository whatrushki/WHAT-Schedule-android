package app.what.foundation.data.settings.types

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.foundation.ui.capplyIf
import app.what.foundation.ui.theme.LocalThemeIsDark
import com.materialkolor.ktx.DynamicScheme
import com.materialkolor.toColorScheme

fun PreferenceStorage.Value<ULong>.asColorPalette(
    colors: List<Color> = listOf(
        Color(0xFFA1FF00), Color(0xFFFF0600), Color(0xFF586BFF),
        Color(0xFFFF68A0), Color(0xFFDAEDFF), Color(0xFFFFFCF0),
        Color(0xFFFFECBE), Color(0xFFFFEEE0), Color(0xFF24FFEA)
    ),
    sideEffect: (Color) -> Unit
) = customSetting { modifier ->
    val selectedColorValue by collect()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Gap(16)
        colors.forEach { color ->
            PaletteItem(
                color = color,
                selected = selectedColorValue == color.value,
                onClick = { sideEffect(color); set(color.value) }
            )
        }
        Gap(16)
    }
}

@Composable
fun PaletteItem(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    MaterialTheme(
        colorScheme = DynamicScheme(color, LocalThemeIsDark.current).toColorScheme()
    ) {
        Box(
            Modifier
                .clip(shapes.medium)
                .background(colorScheme.surfaceContainer)
                .capplyIf(selected) { border(2.dp, colorScheme.primary, shapes.medium) }
                .padding(8.dp)
                .bclick(!selected, onClick)
        ) {
            Column {
                Spacer(
                    modifier = Modifier
                        .size(52.dp, 24.dp)
                        .clip(RoundedCornerShape(100.dp, 100.dp, 12.dp, 12.dp))
                        .background(colorScheme.primary)
                )

                Gap(4)

                Row {
                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp, 4.dp, 4.dp, 100.dp))
                            .background(colorScheme.secondary)
                    )

                    Gap(4)

                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp, 12.dp, 100.dp, 4.dp))
                            .background(colorScheme.tertiary)
                    )
                }
            }
        }
    }
}
