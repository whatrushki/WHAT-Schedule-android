package app.what.foundation.services.auto_update


import android.content.Context
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

fun Context.isInstalledFromRuStore() =
    packageManager.getInstallerPackageName(packageName).also {
        Auditor.debug("d", "installer: $it")
    } == "ru.vk.store"

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
    data class Completed(val file: File? = null) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

interface AppUpdateManager {
    val updateInfo: UpdateInfo?
    val downloadState: DownloadState

    /**
     * Запускает проверку обновлений (обычно вызывается один раз при старте)
     */
    suspend fun checkForUpdates(): UpdateResult

    /**
     * Основное действие пользователя: скачать / установить
     */
    fun handleAction()

    /**
     * Отмена загрузки, если поддерживается
     */
    fun cancelDownload()

    /**
     * Очистка (при необходимости)
     */
    fun release()
}

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String,
    val body: String,
    val assets: List<GitHubAsset>,
    @SerialName("published_at") val publishedAt: String,
    val prerelease: Boolean,
    val draft: Boolean
)

@Serializable
data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long,
    @SerialName("download_count") val downloadCount: Int
)


data class DownloadProgress(
    val progress: Float,
    val downloadedBytes: Long,
    val totalBytes: Long
)
