package app.what.foundation.services.auto_update

import app.what.foundation.services.AppLogger.Companion.Auditor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class GitHubUpdateService(
    private val httpClient: HttpClient
) {

    suspend fun checkForUpdates(
        owner: String,
        repo: String,
        currentVersion: String
    ): UpdateResult {
        return try {
            val releases = httpClient.get {
                url("https://api.github.com/repos/$owner/$repo/releases")
                parameter("per_page", 10)
            }.body<List<GitHubRelease>>()

            val latestRelease = releases
                .filter { !it.draft && !it.prerelease }
                .maxByOrNull { parseVersion(it.tagName) }

            if (latestRelease == null) {
                return UpdateResult.NotAvailable
            }

            val latestVersion = parseVersion(latestRelease.tagName)
            val currentVersionParsed = parseVersion(currentVersion)

            Auditor.debug("d", "$latestVersion $currentVersionParsed")
            Auditor.debug("d", "${latestRelease.tagName} $currentVersion")

            if (latestVersion > currentVersionParsed) {
                val apkAsset = latestRelease.assets.firstOrNull { it.name.endsWith(".apk") }

                if (apkAsset == null) {
                    return UpdateResult.Error("APK not found in release assets")
                }

                val updateInfo = UpdateInfo(
                    version = latestRelease.tagName,
                    releaseNotes = latestRelease.body,
                    downloadUrl = apkAsset.browserDownloadUrl,
                    fileSize = apkAsset.size,
                    publishDate = latestRelease.publishedAt,
                    isPreRelease = latestRelease.prerelease
                )

                UpdateResult.Available(updateInfo)
            } else {
                UpdateResult.NotAvailable
            }

        } catch (e: Exception) {
            UpdateResult.Error("Failed to check for updates: ${e.message}")
        }
    }

    suspend fun downloadUpdate(
        downloadUrl: String,
        destination: File,
        onProgress: ((DownloadProgress) -> Unit)? = null
    ): Result<File> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(downloadUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(Exception("HTTP error: ${connection.responseCode}"))
                }

                val contentLength = connection.contentLength.toLong()
                val inputStream = connection.inputStream
                var downloadedBytes = 0L
                val buffer = ByteArray(8192)

                destination.outputStream().buffered().use { output ->
                    var bytesRead: Int
                    var iter = 0
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val progressValue = if (contentLength <= 0) 0f
                        else downloadedBytes.toFloat() / contentLength

                        if (iter++ > 30) {
                            iter = 0
                            withContext(Dispatchers.Main) {
                                onProgress?.invoke(
                                    DownloadProgress(
                                        progress = progressValue,
                                        downloadedBytes = downloadedBytes,
                                        totalBytes = contentLength
                                    )
                                )
                            }
                        }
                    }
                }

                Result.success(destination)

            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun parseVersion(versionString: String): Version {
        val cleanVersion = versionString.replace("v", "").replace("V", "").trim()

        try {
            val mainAndPreRelease = cleanVersion.split("-")
            val mainPart = mainAndPreRelease[0]

            val mainParts = mainPart.split(".").map { it.toIntOrNull() ?: 0 }

            var preReleaseType = PreReleaseType.STABLE
            var preReleaseNumber = 0

            if (mainAndPreRelease.size > 1) {
                val preReleasePart = mainAndPreRelease[1]
                val preReleaseParts = preReleasePart.split(".")

                preReleaseType = when (preReleaseParts[0].lowercase()) {
                    "alpha" -> PreReleaseType.ALPHA
                    "beta" -> PreReleaseType.BETA
                    "rc" -> PreReleaseType.RELEASE_CANDIDATE
                    "stable" -> PreReleaseType.STABLE
                    else -> PreReleaseType.STABLE // по умолчанию
                }

                preReleaseNumber = preReleaseParts.getOrElse(1) { "0" }.toIntOrNull() ?: 0
            }

            return Version(
                major = mainParts.getOrElse(0) { 0 },
                minor = mainParts.getOrElse(1) { 0 },
                patch = mainParts.getOrElse(2) { 0 },
                preReleaseType = preReleaseType,
                preReleaseNumber = preReleaseNumber
            )
        } catch (e: Exception) {
            // В случае ошибки парсинга возвращаем версию 0.0.0
            return Version(0, 0, 0, PreReleaseType.ALPHA, 0)
        }
    }
}

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preReleaseType: PreReleaseType = PreReleaseType.STABLE,
    val preReleaseNumber: Int = 0
) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        return compareValuesBy(
            this, other,
            { it.major },
            { it.minor },
            { it.patch },
            { it.preReleaseType.ordinal }, // STABLE имеет наибольший ordinal
            { it.preReleaseNumber }
        )
    }
}

enum class PreReleaseType {
    ALPHA,      // 0
    BETA,       // 1
    RELEASE_CANDIDATE, // 2
    STABLE      // 3 - самый высокий приоритет
}