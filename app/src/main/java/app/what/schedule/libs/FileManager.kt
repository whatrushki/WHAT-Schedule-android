package app.what.schedule.libs

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException


class FileManager(private val context: Context) {
    enum class DirectoryType {
        PRIVATE,
        PUBLIC,
        CACHE
    }

    companion object {
        private const val TAG = "FileManager"
        private val PUBLIC_SUBDIR = Environment.DIRECTORY_DOWNLOADS
    }

    // Запись файла
    fun writeFile(type: DirectoryType, fileName: String, data: ByteArray): Boolean {
        return when (type) {
            DirectoryType.PRIVATE -> writeToPrivate(fileName, data)
            DirectoryType.PUBLIC -> writeToPublic(fileName, data)
            DirectoryType.CACHE -> writeToCache(fileName, data)
        }
    }

    // Чтение файла
    fun readFile(type: DirectoryType, fileName: String): ByteArray? {
        return try {
            when (type) {
                DirectoryType.PRIVATE -> readFromPrivate(fileName)
                DirectoryType.PUBLIC -> readFromPublic(fileName)
                DirectoryType.CACHE -> readFromCache(fileName)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading file: $fileName", e)
            null
        }
    }

    // Получение File объекта
    fun getFile(type: DirectoryType, fileName: String): File? = when (type) {
        DirectoryType.PRIVATE -> File(context.filesDir, fileName)
        DirectoryType.PUBLIC -> getPublicFile(fileName)
        DirectoryType.CACHE -> File(context.cacheDir, fileName)
    }

    private fun writeToPrivate(fileName: String, data: ByteArray): Boolean {
        return try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(data)
                true
            }
        } catch (e: IOException) {
            Log.e(TAG, "Private write error", e)
            false
        }
    }

    private fun writeToPublic(fileName: String, data: ByteArray): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(fileName, data)
        } else {
            if (!isExternalStorageWritable) return false
            val file = getPublicFile(fileName) ?: return false
            try {
                file.parentFile?.mkdirs()
                file.writeBytes(data)
                true
            } catch (e: IOException) {
                Log.e(TAG, "Public write error", e)
                false
            }
        }
    }

    private fun writeToCache(fileName: String, data: ByteArray): Boolean {
        return try {
            File(context.cacheDir, fileName).writeBytes(data)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Cache write error", e)
            false
        }
    }

    private fun readFromPrivate(fileName: String): ByteArray {
        return context.openFileInput(fileName).use { it.readBytes() }
    }

    private fun readFromPublic(fileName: String): ByteArray {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            readViaMediaStore(fileName)
        } else {
            getPublicFile(fileName)?.readBytes() ?: throw IOException("File not found")
        }
    }

    private fun readFromCache(fileName: String): ByteArray {
        return File(context.cacheDir, fileName).readBytes()
    }

    private fun getPublicFile(fileName: String): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            null // TODO: Для Android 10+ используем MediaStore
        } else {
            Environment.getExternalStoragePublicDirectory(PUBLIC_SUBDIR)?.let {
                File(it, fileName)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeViaMediaStore(fileName: String, data: ByteArray): Boolean {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, "$PUBLIC_SUBDIR/")
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(data)
                    true
                } ?: false
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore write error", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun readViaMediaStore(fileName: String): ByteArray {
        val resolver = context.contentResolver
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val args = arrayOf(fileName)

        return resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            args,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                val uri = ContentUris.withAppendedId(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    id
                )
                resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("File stream error")
            } else {
                throw IOException("File not found")
            }
        } ?: throw IOException("Query failed")
    }

    private val isExternalStorageWritable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}