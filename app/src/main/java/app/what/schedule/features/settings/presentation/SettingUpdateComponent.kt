package app.what.schedule.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.what.foundation.core.Monitor.Companion.monitored
import app.what.foundation.core.UIComponent
import app.what.foundation.services.auto_update.DownloadState
import app.what.foundation.services.auto_update.UpdateInfo
import app.what.foundation.services.auto_update.UpdateManager
import app.what.foundation.services.auto_update.UpdateResult
import app.what.foundation.ui.Gap
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.bclick
import app.what.foundation.ui.controllers.rememberDialogController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File
import java.util.Locale

object SettingUpdateComponent : UIComponent {
    private var updateInfo: UpdateInfo? by monitored(null)

    @Composable
    override fun content(modifier: Modifier) {
        val dialogController = rememberDialogController()
        val updateManager = koinInject<UpdateManager>()
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            scope.launch {
                val result = updateManager.checkForUpdates()
                if (result is UpdateResult.Available) updateInfo = result.updateInfo
            }
        }

        AnimatedEnter(updateInfo != null) {
            Box(
                modifier = modifier
                    .clip(shapes.medium)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.8f),
                                colorScheme.primary.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .bclick {
                        dialogController.open(false, UpdateDialog)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Доступно обновление",
                        modifier = Modifier.size(40.dp),
                        tint = colorScheme.onPrimary
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Доступно обновление",
                            style = typography.titleMedium,
                            color = colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Gap(4)

                        Text(
                            text = "Версия ${updateInfo!!.version} с новыми функциями",
                            style = typography.bodyMedium,
                            color = colorScheme.onPrimary.copy(alpha = 0.9f)
                        )

                        Gap(8)

                        Text(
                            text = "Нажмите для подробностей",
                            style = typography.labelSmall,
                            color = colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }

    private val UpdateDialog = @Composable
    fun() {
        updateInfo ?: return

        val updateManager = koinInject<UpdateManager>()
        val downloadState by updateManager.downloadState.collectAsState()
        val scope = rememberCoroutineScope()
        val controller = rememberDialogController()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            when (downloadState) {
                DownloadState.Idle -> {
                    UpdateInfoContent(
                        updateInfo = updateInfo!!,
                        onCancel = controller::close,
                        onUpdate = {
                            scope.launch {
                                updateManager.downloadUpdate(updateInfo!!)
                            }
                        }
                    )
                }

                is DownloadState.Downloading, DownloadState.Preparing -> {
                    DownloadingContent(
                        downloadState = downloadState,
                        onCancel = {
                            updateManager.cancelDownload()
                            controller.close()
                        }
                    )
                }

                is DownloadState.Completed -> {
                    CompleteContent(
                        file = (downloadState as DownloadState.Completed).file,
                        onInstall = { file ->
                            updateManager.installUpdate(file)
                            controller.close()
                        },
                        onCancel = controller::close
                    )
                }

                is DownloadState.Error -> {
                    ErrorContent(
                        errorMessage = (downloadState as DownloadState.Error).message,
                        onCancel = controller::close,
                        onRetry = {
                            scope.launch {
                                updateManager.downloadUpdate(updateInfo!!)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateInfoContent(
    updateInfo: UpdateInfo,
    onCancel: () -> Unit,
    onUpdate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Доступно обновление ${updateInfo.version}",
            style = typography.titleLarge
        )
        Gap(16)
        Text("Что нового:", fontWeight = FontWeight.Bold)
        Gap(8)
        Text(updateInfo.releaseNotes)
        Gap(16)
        Text("Размер: ${formatBytes(updateInfo.fileSize)}")
        Gap(24)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onUpdate) {
                Text("Обновить")
            }
            Gap(16)
            TextButton(onClick = onCancel) {
                Text("Позже")
            }
        }
    }
}

@Composable
private fun DownloadingContent(
    downloadState: DownloadState,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Загрузка обновления",
            style = typography.titleSmall
        )

        if (downloadState is DownloadState.Preparing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Подготовка к загрузке...")
        } else if (downloadState is DownloadState.Downloading) {
            val progress = downloadState.progress
            LinearProgressIndicator(
                progress = { progress.progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(progress.progress * 100).toInt()}%")
                Text("${formatBytes(progress.downloadedBytes)} / ${formatBytes(progress.totalBytes)}")
            }
        }

        Button(onClick = onCancel) {
            Text("Отмена")
        }
    }
}

@Composable
private fun CompleteContent(
    file: File,
    onInstall: (File) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Загрузка завершена",
            style = typography.titleSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "APK файл",
                    modifier = Modifier.size(48.dp)
                )

                Column {
                    Text(
                        text = file.name,
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Размер: ${formatBytes(file.length())}",
                        style = typography.bodySmall
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { onInstall(file) }) {
                Text("Установить")
            }

            TextButton(onClick = onCancel) {
                Text("Отмена")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Ошибка",
            modifier = Modifier.size(48.dp),
            tint = colorScheme.error
        )

        Text(
            text = "Ошибка загрузки",
            style = typography.titleSmall
        )

        Text(
            text = errorMessage,
            style = typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onRetry) {
                Text("Повторить")
            }

            TextButton(onClick = onCancel) {
                Text("Отмена")
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${
            String.format(
                Locale.getDefault(),
                "%.1f",
                bytes / (1024.0 * 1024.0)
            )
        } MB"

        bytes >= 1024 -> "${String.format(Locale.getDefault(), "%.1f", bytes / 1024.0)} KB"
        else -> "$bytes B"
    }
}