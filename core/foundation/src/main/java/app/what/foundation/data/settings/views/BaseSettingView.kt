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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick
import app.what.foundation.ui.controllers.rememberDialogController

@Composable
fun BaseSettingView(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null
) {
    val dialogController = rememberDialogController()

    Box(
        modifier
            .clip(shapes.medium)
            .bclick(supportingContent != null && enabled) {
                dialogController.open(content = supportingContent!!)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp, 12.dp)
        ) {
            icon.Show(Modifier.size(28.dp), colorScheme.primary)

            Gap(18)

            Column(Modifier.weight(1f)) {
                Text(title, style = typography.titleLarge, color = colorScheme.onBackground)
                Text(
                    description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.secondary
                )
            }

            trailing ?: return

            Gap(16)

            trailing()
        }
    }
}