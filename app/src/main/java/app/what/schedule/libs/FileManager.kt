package app.what.schedule.libs

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import java.io.OutputStream

class FileManager(private val context: Context) {
    private val crashlytics = FirebaseCrashlytics.getInstance()

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
        val fileTag = buildTag(LogScope.FILE, LogCat.ERROR)
        return try {
            val file = getFile(type, fileName)
            file.parentFile?.mkdirs()
            Auditor.debug(buildTag(LogScope.FILE, LogCat.DB), "Открытие потока записи: $fileName, тип: $type")
            file.outputStream()
        } catch (e: Exception) {
            Auditor.err(fileTag, "Ошибка открытия потока записи: $fileName", e)
            crashlytics.setCustomKey("file_write_error", fileName)
            crashlytics.recordException(e)
            null
        }
    }

    fun readBytes(type: DirectoryType, fileName: String): Result<ByteArray> = runCatching {
        val fileTag = buildTag(LogScope.FILE, LogCat.DB)
        Auditor.debug(fileTag, "Чтение файла: $fileName, тип: $type")
        getFile(type, fileName).readBytes()
    }.onFailure { e ->
        val fileTag = buildTag(LogScope.FILE, LogCat.ERROR)
        Auditor.err(fileTag, "Ошибка чтения файла: $fileName", e)
        crashlytics.setCustomKey("file_read_error", fileName)
        crashlytics.recordException(e)
    }

    fun writeBytes(type: DirectoryType, fileName: String, data: ByteArray): Result<Unit> =
        runCatching {
            val fileTag = buildTag(LogScope.FILE, LogCat.DB)
            Auditor.debug(fileTag, "Запись файла: $fileName, размер: ${data.size} байт, тип: $type")
            val file = getFile(type, fileName)
            file.parentFile?.mkdirs()
            file.writeBytes(data)
        }.onFailure { e ->
            val fileTag = buildTag(LogScope.FILE, LogCat.ERROR)
            Auditor.err(fileTag, "Ошибка записи файла: $fileName", e)
            crashlytics.setCustomKey("file_write_error", fileName)
            crashlytics.recordException(e)
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