package app.what.schedule.features.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.what.foundation.core.UIComponent
import app.what.foundation.services.auto_update.DownloadState
import app.what.foundation.services.auto_update.UpdateInfo
import app.what.foundation.services.auto_update.UpdateManager
import app.what.foundation.ui.Gap
import app.what.foundation.ui.animations.wiggle
import app.what.foundation.ui.bclick
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Download
import app.what.schedule.ui.theme.icons.filled.DownloadError
import app.what.schedule.ui.theme.icons.filled.ReleaseAlert
import org.koin.compose.koinInject


object SettingUpdateComponent : UIComponent {

    @Composable
    override fun content(modifier: Modifier) {
        val manager = koinInject<UpdateManager>()
        val updateInfo = manager.updateInfo
        val downloadState = manager.downloadState

        LaunchedEffect(Unit) {
            manager.checkForUpdates()
        }

        AnimatedVisibility(
            visible = updateInfo != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            val info = updateInfo ?: return@AnimatedVisibility

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp)
                    .clip(shapes.medium)
                    .background(colorScheme.primaryContainer)
                    .bclick { manager.handleAction(info) }
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UpdateHeader(info, downloadState)

                    if (downloadState is DownloadState.Downloading || downloadState is DownloadState.Preparing) {
                        val progress =
                            (downloadState as? DownloadState.Downloading)?.progress?.progress ?: 0f

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape),
                                color = colorScheme.primary,
                                trackColor = colorScheme.primary.copy(0.2f)
                            )
                            Text(
                                text = "Загрузка: ${(progress * 100).toInt()}%",
                                style = typography.labelSmall,
                                color = colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun UpdateHeader(info: UpdateInfo, state: DownloadState) {
        val (title, icon, subtitle) = when (state) {
            is DownloadState.Idle -> Triple(
                "Доступно обновление ${info.version}",
                WHATIcons.ReleaseAlert,
                "Нажмите, чтобы скачать"
            )

            is DownloadState.Preparing -> Triple(
                "Подготовка...",
                Icons.Rounded.Build,
                "Секунду..."
            )

            is DownloadState.Downloading -> Triple(
                "Скачивание...",
                Icons.Rounded.Refresh,
                "Файл загружается в Downloads"
            )

            is DownloadState.Completed -> Triple(
                "Обновление скачано!",
                WHATIcons.Download,
                "Нажмите, чтобы установить"
            )

            is DownloadState.Error -> Triple(
                "Ошибка загрузки",
                WHATIcons.DownloadError,
                "Нажмите, чтобы попробовать снова"
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier
                .size(28.dp)
                .wiggle(15f), colorScheme.primary)
            Gap(12)
            Column {
                Text(
                    title,
                    style = typography.titleMedium,
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(subtitle, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}