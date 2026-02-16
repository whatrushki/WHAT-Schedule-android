package app.what.foundation.services.auto_update

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import app.what.foundation.services.AppLogger.Companion.Auditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

data class UpdateConfig(
    val githubOwner: String,
    val githubRepo: String,
    val currentVersion: String
)

class GitHubUpdateManager(
    private val gitHubService: GitHubUpdateService,
    private val context: Context,
    private val config: UpdateConfig,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : AppUpdateManager {
    override var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    private val _downloadState = mutableStateOf<DownloadState>(DownloadState.Idle)
    override val downloadState: DownloadState get() = _downloadState.value

    private var downloadJob: Job? = null

    init {
        scope.launch(Dispatchers.IO) {
            Auditor.debug("d", "start check github")
            try {
                checkForUpdates()
            } catch (e: Exception) {
                Auditor.err("d", "d", e)
            }
        }
    }

    override suspend fun checkForUpdates(): UpdateResult {
        val result = gitHubService.checkForUpdates(
            config.githubOwner,
            config.githubRepo,
            config.currentVersion
        )

        return when (result) {
            is UpdateResult.Available -> {
                updateInfo = result.updateInfo
                checkIfAlreadyDownloaded(result.updateInfo)
                result
            }

            else -> result
        }.also {
            Auditor.debug("d", it.toString())
        }
    }

    private fun checkIfAlreadyDownloaded(info: UpdateInfo) {
        val file = getPublicFile(getFileName(info.version))
        if (file?.exists() == true && file.length() >= info.fileSize) {
            _downloadState.value = DownloadState.Completed(file)
        }
    }

    private fun getFileName(version: String) = "${config.githubRepo}-$version.apk"

    private fun getPublicFile(fileName: String): File? {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, fileName)
        return file.takeIf { it.exists() }
    }

    override fun handleAction() {
        val info = updateInfo ?: return
        when (val state = downloadState) {
            is DownloadState.Idle,
            is DownloadState.Error -> downloadUpdate(info)

            is DownloadState.Completed -> {
                state.file?.let { installUpdate(it) }
            }

            else -> { /* уже качается / устанавливается */
            }
        }
    }

    private fun downloadUpdate(info: UpdateInfo) {
        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            try {
                _downloadState.value = DownloadState.Preparing

                val fileName = getFileName(info.version)
                val destination = preparePublicFile(fileName)

                val result = gitHubService.downloadUpdate(
                    downloadUrl = info.downloadUrl,
                    destination = destination,
                    onProgress = { progress ->
                        _downloadState.value = DownloadState.Downloading(progress.progress.toInt())
                    }
                )

                _downloadState.value = result.fold(
                    onSuccess = { DownloadState.Completed(it) },
                    onFailure = { DownloadState.Error(it.message ?: "Ошибка загрузки") }
                )
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    private fun preparePublicFile(fileName: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        dir.mkdirs()
        return File(dir, fileName)
    }

    private fun installUpdate(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun cancelDownload() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Idle
    }

    override fun release() {
        downloadJob?.cancel()
        scope.cancel()
    }
}