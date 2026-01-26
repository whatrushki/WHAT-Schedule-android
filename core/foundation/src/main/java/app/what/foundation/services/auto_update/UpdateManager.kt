package app.what.foundation.services.auto_update

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class UpdateManager(
    private val gitHubService: GitHubUpdateService,
    private val context: Context,
    private val config: UpdateConfig,
    private val scope: CoroutineScope
) {
    var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    var downloadState by mutableStateOf<DownloadState>(DownloadState.Idle)
        private set

    private var downloadJob: Job? = null

    private fun getFileName(version: String) = "${config.githubRepo}-$version.apk"

    suspend fun checkForUpdates(): UpdateResult {
        val result = gitHubService.checkForUpdates(
            config.githubOwner,
            config.githubRepo,
            config.currentVersion
        )
        if (result is UpdateResult.Available) {
            updateInfo = result.updateInfo
            checkIfAlreadyDownloaded(result.updateInfo)
        }
        return result
    }

    private fun checkIfAlreadyDownloaded(info: UpdateInfo) {
        val file = getPublicFile(getFileName(info.version))
        if (file != null && file.exists() && file.length() >= info.fileSize) {
            downloadState = DownloadState.Completed(file)
        }
    }

    fun handleAction(info: UpdateInfo) {
        when (val state = downloadState) {
            is DownloadState.Idle, is DownloadState.Error -> downloadUpdate(info)
            is DownloadState.Completed -> installUpdate(state.file)
            else -> {}
        }
    }

    private fun downloadUpdate(info: UpdateInfo) {
        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            try {
                downloadState = DownloadState.Preparing

                // Создаем файл в публичной папке Downloads
                val fileName = getFileName(info.version)
                val destinationFile = preparePublicFile(fileName)

                val result = gitHubService.downloadUpdate(
                    downloadUrl = info.downloadUrl,
                    destination = destinationFile,
                    onProgress = { downloadState = DownloadState.Downloading(it) }
                )

                downloadState = result.fold(
                    onSuccess = { DownloadState.Completed(it) },
                    onFailure = { DownloadState.Error(it.message ?: "Ошибка") }
                )
            } catch (e: Exception) {
                downloadState = DownloadState.Error(e.message ?: "Ошибка")
            }
        }
    }

    private fun getPublicFile(fileName: String): File? {
        val downloadsDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDir, fileName).takeIf { it.exists() }
    }

    private fun preparePublicFile(fileName: String): File {
        val downloadsDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        return File(downloadsDir, fileName)
    }

    fun installUpdate(file: File) {
        val apkUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

data class UpdateConfig(
    val githubOwner: String,
    val githubRepo: String,
    val currentVersion: String
)