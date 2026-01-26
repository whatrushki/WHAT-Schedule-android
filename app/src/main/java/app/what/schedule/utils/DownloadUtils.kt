package app.what.schedule.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.net.toUri
import app.what.foundation.services.AppLogger.Companion.Auditor

object DownloadUtils {
    fun downloadImage(
        context: Context,
        url: String,
        fileName: String? = null,
        onResult: (Long?) -> Unit = {}
    ) = download(
        context,
        url,
        fileName,
        Environment.DIRECTORY_PICTURES,
        title = "Загрузка изображения",
        additional = {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setMimeType("image/jpeg")
        },
        onResult = onResult
    )

    fun download(
        context: Context,
        url: String,
        fileName: String? = null,
        directory: String = Environment.DIRECTORY_DOWNLOADS,
        title: String? = null,
        description: String? = null,
        additional: DownloadManager.Request.() -> Unit = {},
        onResult: (Long?) -> Unit = {}
    ) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            onResult(null)
            return
        }

        try {
            if (url.startsWith("http://")) {
                Auditor.warn(
                    "DownloadUtils",
                    "Внимание: используется незащищенное соединение (HTTP)"
                )
            }

            val uri = url.toUri()
            val finalFileName = fileName ?: URLUtil.guessFileName(url, null, null)

            val request = DownloadManager.Request(uri).apply {
                setTitle(title ?: finalFileName)
                description?.let { setDescription(it) }
                setDestinationInExternalPublicDir(directory, finalFileName)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    @Suppress("DEPRECATION")
                    allowScanningByMediaScanner()
                }

                additional()
            }

            val id = downloadManager.enqueue(request)
            onResult(if (id != -1L) id else null)
        } catch (e: Exception) {
            onResult(null)
        }
    }
}