package app.what.domain.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface UpdateResult {
    object NotAvailable : UpdateResult
    object UpToDate : UpdateResult
    data class Available(val updateInfo: UpdateInfo) : UpdateResult
    data class Error(val message: String) : UpdateResult
}

data class UpdateInfo(
    val version: String,
    val fileSize: Long = 0L,
    val downloadUrl: String = "",
    val releaseNotes: String? = null,
)

sealed class DownloadState {
    data object Idle : DownloadState()
    data object Preparing : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data class Completed(val path: String? = null) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

interface AppUpdateManager {
    val updateInfo: UpdateInfo?
    val downloadState: DownloadState

    /**
     * Запускает проверку обновлений
     */
    suspend fun checkForUpdates(): UpdateResult

    /**
     * Основное действие пользователя: скачать / установить / обновить страницу
     */
    fun handleAction()

    /**
     * Отмена загрузки, если поддерживается
     */
    fun cancelDownload()

    /**
     * Очистка ресурсов
     */
    fun release()
}

data class UpdateConfig(
    val githubOwner: String = "whatrushki",
    val githubRepo: String = "WHAT-Schedule-android",
    val currentVersion: String = "1.1.0"
)

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAsset> = emptyList(),
    @SerialName("published_at") val publishedAt: String = "",
    val prerelease: Boolean = false,
    val draft: Boolean = false
)

@Serializable
data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long = 0L,
    @SerialName("download_count") val downloadCount: Int = 0
)
