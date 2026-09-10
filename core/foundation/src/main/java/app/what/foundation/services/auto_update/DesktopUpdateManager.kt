package app.what.foundation.services.auto_update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DesktopUpdateManager(
    private val gitHubService: GitHubUpdateService,
    private val config: UpdateConfig,
    private val openUrl: (String) -> Unit,
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
        val result = gitHubService.checkForUpdates(
            owner = config.githubOwner,
            repo = config.githubRepo,
            currentVersion = config.currentVersion,
            assetMatcher = {
                it.endsWith(".jar") || it.endsWith(".zip") || it.endsWith(".msi") ||
                    it.endsWith(".deb") || it.endsWith(".exe") || it.endsWith(".dmg")
            }
        )
        if (result is UpdateResult.Available) {
            updateInfo = result.updateInfo
        }
        return result
    }

    override fun handleAction() {
        val info = updateInfo ?: return
        if (info.downloadUrl.isNotBlank()) {
            openUrl(info.downloadUrl)
        }
    }

    override fun cancelDownload() {}
    override fun release() {}
}
