package app.what.foundation.services.auto_update

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class UpdateManager(
    private val gitHubService: GitHubUpdateService,
    private val context: Context,
    private val config: UpdateConfig
) {

    private var currentDownloadJob: Job? = null
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    suspend fun checkForUpdates(): UpdateResult {
        return gitHubService.checkForUpdates(
            owner = config.githubOwner,
            repo = config.githubRepo,
            currentVersion = config.currentVersion
        )
    }

    fun downloadUpdate(updateInfo: UpdateInfo) {
        currentDownloadJob?.cancel()

        currentDownloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                _downloadState.value = DownloadState.Preparing

                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir

                val fileName = "${config.githubRepo}-${updateInfo.version}.apk"
                val outputFile = File(downloadsDir, fileName)

                outputFile.delete()

                val result = if (outputFile.exists()) Result.success(outputFile)
                else gitHubService.downloadUpdate(
                    downloadUrl = updateInfo.downloadUrl,
                    destination = outputFile,
                    onProgress = { progress ->
                        _downloadState.value = DownloadState.Downloading(progress)
                    }
                )

                result.onSuccess { file ->
                    _downloadState.value = DownloadState.Completed(file)
                }.onFailure { error ->
                    _downloadState.value = DownloadState.Error("Ошибка загрузки: ${error.message}")
                }

            } catch (e: CancellationException) {
                _downloadState.value = DownloadState.Error("Загрузка отменена")
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    fun cancelDownload() {
        currentDownloadJob?.cancel()
        _downloadState.value = DownloadState.Idle
    }

    @SuppressLint("NewApi")
    fun installUpdate(file: File) = with(context) {
        if (file.exists()) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        "package:$packageName".toUri()
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } else {
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        }
    }
}

data class UpdateConfig(
    val githubOwner: String,
    val githubRepo: String,
    val currentVersion: String
)