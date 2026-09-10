package app.what.schedule.desktop.updater

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.what.domain.services.AppUpdateManager
import app.what.domain.services.DownloadState
import app.what.domain.services.GitHubRelease
import app.what.domain.services.UpdateConfig
import app.what.domain.services.UpdateInfo
import app.what.domain.services.UpdateResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI

class DesktopAppUpdateManager(
    private val httpClient: HttpClient,
    private val config: UpdateConfig = UpdateConfig(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AppUpdateManager {
    override var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    private val _downloadState = mutableStateOf<DownloadState>(DownloadState.Idle)
    override val downloadState: DownloadState get() = _downloadState.value

    init {
        scope.launch {
            try {
                checkForUpdates()
            } catch (_: Exception) {}
        }
    }

    override suspend fun checkForUpdates(): UpdateResult {
        return try {
            val releases = httpClient.get("https://api.github.com/repos/${config.githubOwner}/${config.githubRepo}/releases") {
                parameter("per_page", 5)
            }.body<List<GitHubRelease>>()

            val latest = releases.firstOrNull { !it.draft && !it.prerelease } ?: return UpdateResult.NotAvailable
            val cleanLatest = latest.tagName.trimStart('v', 'V')
            val cleanCurrent = config.currentVersion.trimStart('v', 'V')

            if (cleanLatest > cleanCurrent) {
                val asset = latest.assets.firstOrNull {
                    it.name.endsWith(".jar") || it.name.endsWith(".msi") ||
                    it.name.endsWith(".deb") || it.name.endsWith(".zip") || it.name.endsWith(".exe")
                }
                val info = UpdateInfo(
                    version = latest.tagName,
                    fileSize = asset?.size ?: 0L,
                    downloadUrl = asset?.browserDownloadUrl ?: "https://github.com/${config.githubOwner}/${config.githubRepo}/releases/tag/${latest.tagName}",
                    releaseNotes = latest.body
                )
                updateInfo = info
                UpdateResult.Available(info)
            } else {
                UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Failed to check updates")
        }
    }

    override fun handleAction() {
        val url = updateInfo?.downloadUrl ?: return
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        } catch (_: Exception) {}
    }

    override fun cancelDownload() {}
    override fun release() {}
}
