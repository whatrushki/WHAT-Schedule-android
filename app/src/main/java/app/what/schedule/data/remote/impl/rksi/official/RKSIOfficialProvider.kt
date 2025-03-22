package app.what.schedule.data.remote.impl.rksi.official

import android.util.Log
import app.what.foundation.utils.asyncLazy
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.InstitutionProvider
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.LessonState
import app.what.schedule.data.remote.api.LessonTime
import app.what.schedule.data.remote.api.LessonType
import app.what.schedule.data.remote.api.LessonsScheduleType
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.OneTimeUnit
import app.what.schedule.data.remote.api.ParseMode
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.Teacher
import app.what.schedule.data.remote.impl.rksi.RKSILessonsSchedule
import app.what.schedule.data.remote.impl.rksi.RKSILessonsSchedule.getByNumber
import app.what.schedule.data.remote.impl.rksi.RKSILessonsSchedule.numberOf
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.data.remote.utils.parseTime
import app.what.schedule.libs.FileManager
import app.what.schedule.libs.GoogleDriveParser
import app.what.schedule.libs.files
import app.what.schedule.libs.folders
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.time.LocalDate
import java.time.LocalTime


val RKSIProviderMetadata by lazy {
    MetaInfo(
        id = "rksi",
        name = "РКСИ",
        fullName = "Ростовский-на-Дону Колледж Связи и Информатики",
        description = "Официальный api-провайдер колледжа",
        sourceTypes = setOf(SourceType.PARSER, SourceType.EXCEL),
        sourceUrl = "https://rksi.ru/mobile_schedule",
        advantages = listOf(
            "Сокращенное расписание звонков",
            "Замены в расписании"
        ),
        disadvantages = listOf(
            "Долгая загрузка (~12с)"
        )
    )
}

class RKSIOfficialProvider(
    private val client: HttpClient,
    private val googleDriveApi: GoogleDriveParser,
    private val fileManager: FileManager
) : InstitutionProvider {
    companion object Factory : InstitutionProvider.Factory, KoinComponent {
        private const val BASE_URL = "https://www.rksi.ru"
        override fun create() = RKSIOfficialProvider(get(), get(), get())
        override val metadata: MetaInfo = RKSIProviderMetadata
    }

    override val metadata: MetaInfo = RKSIOfficialProvider.metadata
    override val lessonsSchedule = RKSILessonsSchedule
    private val scheduleTabletGoogleDriveId by asyncLazy {
        getScheduleTabletGoogleDriveId()
    }

    override suspend fun getTeachers(): List<Teacher> {
        val response = client.get("$BASE_URL/mobile_schedule").bodyAsText()
        val document = Ksoup.parse(response)
        return document.getElementById("teacher")!!.getElementsByTag("option")
            .map { Teacher(it.text(), it.attr("value")) }
    }

    override suspend fun getGroups(): List<Group> {
        val response = client.get("$BASE_URL/mobile_schedule").bodyAsText()
        val document = Ksoup.parse(response)
        return document.getElementById("group")!!.getElementsByTag("option")
            .map { Group(it.text(), it.attr("value")) }
    }

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = getAndParseSchedule(teacher, ParseMode.TEACHER, showReplacements)

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = getAndParseSchedule(group, ParseMode.GROUP, showReplacements)


    private suspend fun getAndParseSchedule(
        value: String,
        parseMode: ParseMode,
        showReplacements: Boolean
    ): List<DaySchedule> {
        val response = client.submitForm(
            url = "$BASE_URL/mobile_schedule",
            formParameters = parameters {
                if (parseMode == ParseMode.GROUP) {
                    append("group", value)
                    append("stt", "Показать!")
                } else {
                    append("teacher", value)
                    append("stp", "Показать!")
                }
            }
        ).bodyAsText()

        val document = Ksoup.parse(response)
        var dataRaw: List<String>

        val daySchedulesRaw = document.getElementsByClass("schedule_item")

        val daySchedules = daySchedulesRaw.mapIndexed { index, it ->
            dataRaw = it.getElementsByTag("b").text().split(" ")

            val dateDescription = it.getElementsByTag("b").first()!!.text()
            val date = LocalDate.now()
                .withDayOfMonth(dataRaw.first().toInt())
                .withMonth(parseMonth(dataRaw[1].substring(0, dataRaw[1].length - 1)))

            var lessons: List<Lesson>
            lessons = it.getElementsByTag("p").mapNotNull { lessonRaw ->
                if (lessonRaw.html().contains("href")) return@mapNotNull null
                val content = lessonRaw.html().split("<br>")

                dataRaw = content.first().split(" — ")
                val startDate = parseTime(dataRaw.first())
                val endDate = parseTime(dataRaw.last())
                val subject = content[1].substring(3, content[1].length - 4)

                dataRaw = content.last().split(", ")
                val teacherOrGroup = dataRaw.first().substring(1)

                dataRaw = dataRaw.last().split(" ").last().split("/")

                val auditory = if (dataRaw.size == 1) dataRaw.first()
                else dataRaw.dropLast(1).joinToString("/")
                val building = dataRaw.last()

                val lessonType = when {
                    "Доп." in subject -> LessonType.ADDITIONAL
                    "Классный" in subject -> LessonType.CLASS_HOUR
                    else -> LessonType.COMMON
                }

                val otUnits = listOf(
                    if (lessonType != LessonType.CLASS_HOUR) OneTimeUnit(
                        group = Group(if (parseMode == ParseMode.GROUP) value else teacherOrGroup),
                        teacher = Teacher(if (parseMode == ParseMode.TEACHER) value else teacherOrGroup),
                        building = building,
                        auditory = auditory
                    ) else OneTimeUnit.empty()
                )

                Lesson(
                    number = 0,
                    otUnits = otUnits,
                    startTime = startDate,
                    endTime = endDate,
                    subject = subject,
                    type = lessonType
                )
            }.toMutableList().apply {
                val groupedLessons = groupBy { it.startTime }
                clear()

                groupedLessons.forEach { (_, l) ->
                    var unionLesson = l.first()
                    l.forEachIndexed { index, it -> if (index != 0) unionLesson += it }
                    add(unionLesson)
                }
            }

            var schedule = lessonsSchedule.COMMON

            lessons = if (lessons.firstOrNull { it.type == LessonType.CLASS_HOUR } != null) {
                schedule = lessonsSchedule.WITH_CLASS_HOUR
                lessons.map { it.copy(number = schedule.numberOf(it.startTime) ?: 0) }
            } else {
                val getNumberOrChangeSchedule = { time: LocalTime, change: List<LessonTime> ->
                    schedule.numberOf(time) ?: let { schedule = change; null }
                }

                lessons.map { lesson ->
                    val number =
                        getNumberOrChangeSchedule(lesson.startTime, lessonsSchedule.WITH_CLASS_HOUR)
                            ?: getNumberOrChangeSchedule(
                                lesson.startTime,
                                lessonsSchedule.SHORTENED
                            )
                            ?: getNumberOrChangeSchedule(lesson.startTime, lessonsSchedule.COMMON)
                            ?: 0

                    lesson.copy(number = number)
                }
            }

            if (showReplacements && index < 2) {
                val replacements = getReplacements(
                    date, when (parseMode) {
                        ParseMode.GROUP -> ScheduleSearch.Group(value)
                        ParseMode.TEACHER -> ScheduleSearch.Teacher(value)
                    }
                )

                lessons = lessons.withReplacements(replacements, schedule).sortedBy { it.startTime }
            }

            DaySchedule(
                date = date,
                dateDescription = dateDescription,
                lessons = lessons,
                scheduleType = when (schedule) {
                    lessonsSchedule.COMMON -> LessonsScheduleType.COMMON
                    lessonsSchedule.SHORTENED -> LessonsScheduleType.SHORTENED
                    lessonsSchedule.WITH_CLASS_HOUR -> LessonsScheduleType.WITH_CLASS_HOUR
                    else -> LessonsScheduleType.COMMON
                }
            )
        }

        return daySchedules
    }

    private suspend fun getScheduleTabletGoogleDriveId(): String {
        val response = client.get("https://www.rksi.ru/schedule").bodyAsText()
        Log.d("d", "dd: 1")
        val document = Ksoup.parse(response)
        Log.d("d", "dd: 2")
        val tabletUrl = document.getElementsMatchingText("Планшетка").last()!!.attr("href")
        Log.d("d", "dd: 3")

        return tabletUrl.split("/").last()
    }

    private suspend fun getTablet(
        date: LocalDate,
        building: String,
        files: List<GoogleDriveParser.Item>,
        search: ScheduleSearch
    ): File? {
        val tabletName = generateFileName(
            mapOf(
                "building" to building,
                "date" to date.toString(),
                "query" to search.query
            ),
            fileExtension = "xlsx"
        )

        val get = {
            val file = fileManager.getFile(FileManager.DirectoryType.CACHE, tabletName)
            if (file?.exists() == true) file else null
        }

        return get() ?: let {
            val downloaded = findAndDownloadTabletFromGD(date, files, search.query)
            if (downloaded) get() else null
        }
    }

    private suspend fun findAndDownloadTabletFromGD(
        date: LocalDate,
        files: List<GoogleDriveParser.Item>,
        fileName: String
    ): Boolean {
        try {
            Log.d("d", "dd: 4")
            Log.d("d", "dd: 5")
            Log.d("d", "dd: 6 ${files.files().map { it.name }}")
            Log.d(
                "d",
                "dd: 6 $fileName"
            )

            val tablet = files
                .files()
                .firstOrNull {
                    "${
                        date.dayOfMonth.toString().padStart(2, '0')
                    }.${
                        date.month.value.toString().padStart(2, '0')
                    }.${date.year}.xlsx" in it.name
                }
                ?: return false

            Log.d("d", "dd: 7")

            val downloadedTablet = client.get(tablet.getDownloadLink()).readRawBytes()

            Log.d("d", "dd: 8")

            fileManager.writeFile(
                FileManager.DirectoryType.CACHE,
                fileName,
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

        workbook.forEach { sheet ->
            var emptyRows = 0
            var rowIndex = -1

            val otUnits = mutableListOf<OneTimeUnit>()

            val lessonNumber =
                if ("Пара" !in sheet.sheetName) 0
                else sheet.sheetName.split(" ").last().toInt()

            for (row in sheet) {
                rowIndex++
                if (rowIndex == 0) continue
                if (emptyRows > 5) break

                if (row.lastCellNum < 0) emptyRows++
                else (0..<columns).mapIndexedNotNull { colIndex, i ->
                    val firstCellIndex = i * 3

                    Log.d("d", "coord: ($colIndex;$rowIndex;$firstCellIndex)")

                    val auditory = row.getCell(firstCellIndex)
                        ?.toString()
                        ?.ifEmpty { return@mapIndexedNotNull null }
                        ?: return@mapIndexedNotNull null

                    val teacher = row.getCell(firstCellIndex + 2)
                        ?.toString()
                        ?.ifEmpty { return@mapIndexedNotNull null }
                        ?.trim()
                        ?: return@mapIndexedNotNull null

                    val groups = row.getCell(firstCellIndex + 1)
                        ?.toString()
                        ?.split(if (columns == 1) "+" else ",")
                        ?.filter { predicate(teacher, it) }
                        ?.ifEmpty { return@mapIndexedNotNull null }
                        ?: return@mapIndexedNotNull null

                    Log.d("d", "auditory: $auditory, group: $groups, teacher: $teacher")

                    Log.d(
                        "d",
                        "lessonNumber: $lessonNumber ${sheet.sheetName} ${"Пара" in sheet.sheetName}"
                    )

                    groups.map {
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
                    }
                }.let { otUnits.addAll(it.flatten()) }

                if (otUnits.isNotEmpty()) lessons.add(
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
                )
            }
        }

        return lessons
    }

    private suspend fun getReplacements(date: LocalDate, search: ScheduleSearch): List<Lesson> {
        val predicate = { teacher: String, group: String ->
            when (search) {
                is ScheduleSearch.Group -> group == search.query
                is ScheduleSearch.Teacher -> teacher == search.query
            }
        }

        Log.d("d", "d: 1")
        val files1 = googleDriveApi.getFolderContent(scheduleTabletGoogleDriveId.await())
        val files2 = googleDriveApi.getFolderContent(files1.folders().first().id)

        val parseTablet1Process = CoroutineScope(Default).async {
            Log.d("d", "d->: 11")
            val tablet1 = getTablet(date, "1", files1, search) ?: return@async null
            Log.d("d", "d->: 12 ${tablet1.canRead()} ${tablet1.exists()}")
            Log.d("d", "d->: 12")
            val workbook1 = WorkbookFactory.create(tablet1)
            Log.d("d", "d->: 13")
            return@async parseLessonsFromWorkbook(workbook1, 2, predicate)
        }

        val parseTablet2Process = CoroutineScope(Default).async {
            Log.d("d", "d->: 21")
            val tablet2 = getTablet(date, "2", files2, search) ?: return@async null
            Log.d("d", "d->: 22")
            val workbook2 = WorkbookFactory.create(tablet2)
            Log.d("d", "d->: 23")
            return@async parseLessonsFromWorkbook(workbook2, 1, predicate)
        }

        val tablet1Replacements = parseTablet1Process.await()
        val tablet2Replacements = parseTablet2Process.await()

        return (tablet1Replacements ?: emptyList()) + (tablet2Replacements ?: emptyList())
    }
}

fun List<Lesson>.withReplacements(
    replacements: List<Lesson>,
    schedule: List<LessonTime>
): List<Lesson> {
    val unionSchedule = mutableMapOf<Int, List<Lesson?>>()

    Log.d("d", replacements.joinToString("\n"))

    replacements.forEach {
        unionSchedule[it.number] = listOf(it, null)
    }

    this.forEach {
        unionSchedule[it.number] = listOf(unionSchedule[it.number]?.first(), it)
    }

    return unionSchedule.map {
        val replacement = it.value.first()
        val lesson = it.value.last()

        Log.d("d", it.toString())
        Log.d("d", replacement.toString())
        Log.d("d", lesson.toString())

        return@map if (replacements.isNotEmpty()) {
            if (replacement == null && lesson != null)
                lesson.copy(state = LessonState.REMOVED)
            else if (replacement != null && lesson == null) {
                val lessonTime = schedule.getByNumber(replacement.number)
                replacement.copy(
                    state = LessonState.ADDED,
                    startTime = lessonTime!!.startTime,
                    endTime = lessonTime.endTime,
                    subject = if (replacement.type == LessonType.CLASS_HOUR) "Классный час"
                    else replacement.subject
                )
            } else if (!lesson!!.equalsWithReplacement(replacement!!)) {
                val lessonTeachers = lesson.otUnits.map { it.teacher }

                replacement.copy(
                    number = lesson.number,
                    startTime = lesson.startTime,
                    endTime = lesson.endTime,
                    type = lesson.type,
                    state = if (lesson.type != LessonType.CLASS_HOUR) LessonState.CHANGED
                    else LessonState.COMMON,
                    subject = if (replacement.otUnits.all { it.teacher !in lessonTeachers }) ""
                    else lesson.subject
                )
            } else lesson
        } else lesson!!
    }
}
