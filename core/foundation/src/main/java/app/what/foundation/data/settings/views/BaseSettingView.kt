package app.what.foundation.data.settings.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick

@Composable
fun BaseSettingRow(
    value: PreferenceStorage.Value<*>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    action: @Composable () -> Unit = {}
) = Box(
    modifier
        .clip(shapes.medium)
        .bclick(block = onClick)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 8.dp)
    ) {
        value.icon?.let {
            it.Show(Modifier.size(28.dp), colorScheme.primary)
            Gap(16)
        }

        Column(Modifier.weight(1f)) {
            Text(value.title, fontSize = 18.sp, color = colorScheme.onBackground)
            value.description?.let {
                Text(
                    it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.secondary
                )
            }
        }

        Gap(16)

        action()
    }
}
