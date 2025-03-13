package app.what.schedule.data.remote.impl.rksi

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.LessonType
import app.what.schedule.data.remote.api.OneTimeUnit
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.Teacher
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ReplacementsRepository(
    private val client: HttpClient,
    private val googleDriveApi: GoogleDriveApi,
    private val fileManager: FileManager
) {
    private fun getTabletName(date: LocalDate, building: String) =
        "tablet_${building}_${date.format(DateTimeFormatter.ISO_LOCAL_DATE).replace('-', '_')}.xlsx"

    suspend fun getTablet(date: LocalDate, building: String): File? {
        val tabletName = getTabletName(date, building)
        val get = {
            val file = fileManager.getFile(FileManager.DirectoryType.CACHE, tabletName)
            if (file?.exists() == true) file else null
        }

        Log.d("d", "get: ${get()}")

        return get() ?: let {
            val downloaded = downloadTablet(date, building)
            Log.d("d", "get: $downloaded ${get()}")
            if (downloaded) get() else null
        }
    }

    private suspend fun downloadTablet(date: LocalDate, building: String): Boolean {
        try {
            val response = client.get("https://www.rksi.ru/schedule").bodyAsText()
            Log.d("d", "dd: 1")
            val document = Ksoup.parse(response)
            Log.d("d", "dd: 2")
            val tabletUrl = document.getElementsMatchingText("Планшетка").last()!!.attr("href")
            Log.d("d", "dd: 3")

            var files = googleDriveApi.getFolderContent(tabletUrl.split("/").last())
            Log.d("d", "dd: 4")
            if (building == "2") files = googleDriveApi.getFolderContent(files.folders().first().id)
            Log.d("d", "dd: 5")
            val tabletFileName = getTabletName(date, building)
            Log.d("d", "dd: 6 ${files.files().map { it.name }}")
            Log.d(
                "d",
                "dd: 6 ${date.dayOfMonth.toString().padStart(2, '0')}.${
                    date.month.value.toString().padStart(2, '0')
                }.${date.year}.xlsx"
            )

            val tablet = files
                .files()
                .firstOrNull {
                    "${
                        date.dayOfMonth.toString().padStart(2, '0')
                    }.${date.month.value.toString().padStart(2, '0')}.${date.year}.xlsx" in it.name
                }
                ?: return false

            Log.d("d", "dd: 7")

            val downloadedTablet = client.get(tablet.getDownloadLink()).readRawBytes()

            Log.d("d", "dd: 8")

            fileManager.writeFile(
                FileManager.DirectoryType.CACHE,
                tabletFileName,
                downloadedTablet
            )

            Log.d("d", "dd: 9")
            return true
        } catch (e: Exception) {
            Log.d("d", "dd: 10")
            return false
        }
    }

    private fun parseLessonsFromWorkbook(
        workbook: Workbook,
        columns: Int,
        predicate: (teacher: String, group: String) -> Boolean
    ): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        workbook.forEachIndexed { index, sheet ->
            var emptyRows = 0
            sheet.iterator().asSequence().drop(1).forEachIndexed { rowIndex, row ->
                if (emptyRows > 5) return@forEachIndexed

                if (row.lastCellNum < 0) emptyRows++
                else (0..<columns).mapIndexedNotNull { index, i ->
                    val firstCellIndex = i * 3

                    Log.d("d", "coord: ($index;$rowIndex;$firstCellIndex)")

                    val auditory =
                        row.getCell(firstCellIndex)?.toString() ?: return@mapIndexedNotNull null
                    val teacher =
                        row.getCell(firstCellIndex + 2)?.toString()?.trim() ?: return@mapIndexedNotNull null
                    val groups = row.getCell(firstCellIndex + 1)
                        ?.toString()
                        ?.split(if (columns == 1) "+" else ",")
                        ?.map { it.trim() }
                        ?.filter { predicate(teacher, it) }
                        ?: return@mapIndexedNotNull null

                    Log.d("d", "auditory: $auditory, group: $groups, teacher: $teacher")

                    if (
                        auditory.isBlank() ||
                        teacher.isBlank() ||
                        groups.isEmpty()
                    ) return@mapIndexedNotNull null

                    val lessonNumber =
                        if ("Пара" !in sheet.sheetName) 0
                        else sheet.sheetName.split(" ").last().toInt()

                    Log.d(
                        "d",
                        "lessonNumber: $lessonNumber ${sheet.sheetName} ${"Пара" in sheet.sheetName}"
                    )

                    val otUnits = mutableListOf<OneTimeUnit>()

                    groups.forEach {
                        otUnits.add(
                            OneTimeUnit(
                                auditory = try {
                                    auditory.toFloat().toInt().toString()
                                } catch (e: Exception) {
                                    auditory
                                },
                                group = Group(it.trim()),
                                teacher = Teacher(teacher),
                                building = "1",
                            )
                        )
                    }

                    Lesson(
                        number = lessonNumber,
                        startTime = LocalTime.MIN,
                        endTime = LocalTime.MIN,
                        otUnits = otUnits,
                        subject = "",
                        type = when (lessonNumber == 0) {
                            true -> LessonType.CLASS_HOUR
                            false -> LessonType.COMMON
                        }
                    )
                }.let {
                    if (it.isNotEmpty())
                        lessons.add(it.first().copy(otUnits = it.map { it.otUnits }.flatten()))
                }
            }
        }

        return lessons
    }

    suspend fun getReplacements(date: LocalDate, search: ScheduleSearch): List<Lesson>? {
        val predicate = { teacher: String, group: String ->
            when (search) {
                is ScheduleSearch.Group -> group == search.query
                is ScheduleSearch.Teacher -> teacher == search.query
            }
        }

        Log.d("d", "d: 1")

        val tablet1 = getTablet(date, "1") ?: return null
        val tablet2 = getTablet(date, "2") ?: return null

        Log.d("d", "d: 2 ${tablet1.canRead()} ${tablet1.exists()}")

        val workbook1 = WorkbookFactory.create(tablet1)
        val workbook2 = WorkbookFactory.create(tablet2)

        Log.d("d", "d: 3")

        val lessons = parseLessonsFromWorkbook(workbook1, 2, predicate) +
                parseLessonsFromWorkbook(workbook2, 1, predicate)

        return lessons
    }
}

fun List<GoogleDriveApi.Item>.files() =
    filterIsInstance<GoogleDriveApi.Item.File>()

fun List<GoogleDriveApi.Item>.folders() =
    filterIsInstance<GoogleDriveApi.Item.Folder>()

class GoogleDriveApi(
    private val client: HttpClient
) {
    sealed class Item(val id: String, val name: String) {
        override fun toString(): String {
            return "${this::class.simpleName}(id=$id, name=$name)"
        }

        class Folder(id: String, name: String) : Item(id, name)
        class File(id: String, name: String) : Item(id, name) {
            fun getDownloadLink() =
                "https://drive.usercontent.google.com/uc?id=$id&authuser=0&export=download"
        }
    }

    suspend fun getFolderContent(folderId: String): List<Item> {
        val response = client.get("https://drive.google.com/drive/folders/$folderId").bodyAsText()
        val document = Ksoup.parse(response)
        return document.getElementsByAttributeValue("data-target", "doc").map {
            val id = it.attr("data-id")
            val name = it.getElementsByClass("KL4NAf").first()!!.text()

            return@map if ('.' in name) Item.File(id, name)
            else Item.Folder(id, name)
        }
    }
}

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