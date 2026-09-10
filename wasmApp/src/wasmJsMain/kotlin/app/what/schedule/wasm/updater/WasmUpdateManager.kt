package app.what.schedule.wasm.updater

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
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WasmUpdateManager(
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
                val info = UpdateInfo(
                    version = latest.tagName,
                    fileSize = 0L,
                    downloadUrl = window.location.href,
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
        window.location.reload()
    }

    override fun cancelDownload() {}
    override fun release() {}
}
