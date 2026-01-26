package app.what.schedule.libs

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import app.what.foundation.services.AppLogger.Companion.Auditor
import java.io.File
import java.io.OutputStream

class FileManager(private val context: Context) {

    enum class DirectoryType {
        PRIVATE, PUBLIC, CACHE
    }

    fun getFile(type: DirectoryType, fileName: String): File {
        return when (type) {
            DirectoryType.PRIVATE -> File(context.filesDir, fileName)
            DirectoryType.CACHE -> File(context.cacheDir, fileName)
            DirectoryType.PUBLIC -> {
                val downloads =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloads.exists()) downloads.mkdirs()
                File(downloads, fileName)
            }
        }
    }

    fun exists(type: DirectoryType, fileName: String): Boolean = getFile(type, fileName).exists()

    fun delete(type: DirectoryType, fileName: String): Boolean = getFile(type, fileName).delete()

    fun writeStream(type: DirectoryType, fileName: String): OutputStream? {
        return try {
            val file = getFile(type, fileName)
            file.parentFile?.mkdirs()
            file.outputStream()
        } catch (e: Exception) {
            Auditor.err("FileManager", "Failed to open output stream", e)
            null
        }
    }

    fun readBytes(type: DirectoryType, fileName: String): Result<ByteArray> = runCatching {
        getFile(type, fileName).readBytes()
    }

    fun writeBytes(type: DirectoryType, fileName: String, data: ByteArray): Result<Unit> =
        runCatching {
            val file = getFile(type, fileName)
            file.parentFile?.mkdirs()
            file.writeBytes(data)
        }

    /**
     * Специальный метод для Android 10+ для регистрации файла в системе (MediaStore),
     * чтобы он появился в приложении "Загрузки" сразу.
     */
    fun scanFile(file: File, mimeType: String = "application/vnd.android.package-archive") {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf(mimeType),
            null
        )
    }
}