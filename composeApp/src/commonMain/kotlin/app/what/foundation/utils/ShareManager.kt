package app.what.foundation.utils

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Copy
import app.what.schedule.ui.theme.icons.filled.Telegram
import app.what.schedule.ui.theme.icons.filled.VK
import app.what.schedule.ui.theme.icons.filled.Whatsapp

sealed interface ShareData {
    val title: String?

    data class Text(
        val text: String,
        override val title: String? = null
    ) : ShareData
}

enum class ShareChannel(
    val title: String,
    val icon: ImageVector?
) {
    COPY("Скопировать", WHATIcons.Copy),
    TELEGRAM("Telegram", WHATIcons.Telegram),
    VK("ВКонтакте", WHATIcons.VK),
    WHATSAPP("WhatsApp", WHATIcons.Whatsapp),
    MORE("Ещё", Icons.Default.MoreVert);

    companion object {
        val defaultChannels = entries
    }
}

@Composable
fun ShareChannelsRow(
    channels: List<ShareChannel>,
    onChannelClick: (ShareChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        channels.forEach { channel ->
            FilterChip(
                selected = false,
                onClick = { onChannelClick(channel) },
                label = { Text(channel.title) },
                leadingIcon = channel.icon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = channel.title,
                            tint = colorScheme.primary
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colorScheme.surfaceVariant,
                    labelColor = colorScheme.onSurface
                )
            )
        }
    }
}

fun executeShare(context: Any?, channel: ShareChannel, data: ShareData) {
    // Multiplatform share handler (e.g. clipboard / web share API)
}
