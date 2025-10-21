package app.what.foundation.services.auto_update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File


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

data class UpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long,
    val publishDate: String,
    val isPreRelease: Boolean
)

sealed class UpdateResult {
    data class Available(val updateInfo: UpdateInfo) : UpdateResult()
    object NotAvailable : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

data class DownloadProgress(
    val progress: Float,
    val downloadedBytes: Long,
    val totalBytes: Long
)

sealed class DownloadState {
    object Idle : DownloadState()
    object Preparing : DownloadState()
    data class Downloading(val progress: DownloadProgress) : DownloadState()
    data class Completed(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}