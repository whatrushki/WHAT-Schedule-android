package app.what.schedule.rksi.parser

import app.what.schedule.core.models.LessonDto
import app.what.schedule.core.models.LessonStateDto
import app.what.schedule.core.models.LessonTimeDto
import app.what.schedule.core.models.LessonTypeDto
import app.what.schedule.core.models.OneTimeUnitDto
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime

object RKSIReplacementsParser {

    fun parseFromStream(
        inputStream: InputStream,
        columns: Int,
        date: LocalDate,
        predicate: (teacher: String, group: String) -> Boolean
    ): List<LessonDto> {
        val workbook = WorkbookFactory.create(inputStream)
        return workbook.use { wb ->
            parseLessonsFromWorkbook(wb, columns, date, predicate)
        }
    }

    private fun parseLessonsFromWorkbook(
        workbook: Workbook,
        columns: Int,
        date: LocalDate,
        predicate: (teacher: String, group: String) -> Boolean
    ): List<LessonDto> {
        val lessons = mutableListOf<LessonDto>()

        workbook.forEach { sheet ->
            var emptyRows = 0
            var rowIndex = -1
            val otUnits = mutableListOf<OneTimeUnitDto>()

            val lessonNumber = if ("Пара" !in sheet.sheetName) 0
            else sheet.sheetName.split(" ").last().toIntOrNull() ?: 0

            for (row in sheet) {
                rowIndex++
                if (rowIndex == 0) continue
                if (emptyRows > 5) break

                if (row.lastCellNum < 0) {
                    emptyRows++
                } else {
                    (0 until columns).mapNotNull { i ->
                        val firstCellIndex = i * 3
                        val auditory = row.getCell(firstCellIndex)?.toString()?.ifEmpty { null } ?: return@mapNotNull null
                        val teacher = row.getCell(firstCellIndex + 2)?.toString()?.trim()?.ifEmpty { null } ?: return@mapNotNull null
                        val groups = row.getCell(firstCellIndex + 1)?.toString()
                            ?.split(if (columns == 1) "+" else ",")
                            ?.map { it.trim() }
                            ?.filter { predicate(teacher, it) }
                            ?.ifEmpty { null } ?: return@mapNotNull null

                        groups.map { groupName ->
                            OneTimeUnitDto(
                                teacher = teacher.replace("__", "_"),
                                group = groupName,
                                room = try { auditory.toFloat().toInt().toString() } catch (_: Exception) { auditory },
                                additional = if (columns == 1) "Корпус 2" else "Корпус 1"
                            )
                        }
                    }.let { otUnits.addAll(it.flatten()) }

                    if (otUnits.isNotEmpty()) {
                        lessons.add(
                            LessonDto(
                                date = date,
                                number = lessonNumber,
                                startTime = LocalTime.MIN,
                                endTime = LocalTime.MIN,
                                otUnits = otUnits.toList(),
                                subject = if (lessonNumber == 0) "Классный час" else "",
                                type = if (lessonNumber == 0) LessonTypeDto.OTHER else LessonTypeDto.OTHER
                            )
                        )
                        otUnits.clear()
                    }
                }
            }
        }

        return lessons
    }

    fun applyReplacements(
        baseLessons: List<LessonDto>,
        replacements: List<LessonDto>,
        timeSchedule: List<LessonTimeDto>
    ): List<LessonDto> {
        if (replacements.isEmpty()) return baseLessons

        val unionSchedule = mutableMapOf<Int, Pair<LessonDto?, LessonDto?>>()
        replacements.forEach { unionSchedule[it.number] = it to null }
        baseLessons.forEach { unionSchedule[it.number] = unionSchedule[it.number]?.first to it }

        return unionSchedule.mapNotNull { (_, pair) ->
            val replacement = pair.first
            val lesson = pair.second

            if (replacement == null && lesson != null) {
                lesson.copy(state = LessonStateDto.REMOVED)
            } else if (replacement != null && lesson == null) {
                val lessonTime = timeSchedule.firstOrNull { it.number == replacement.number }
                replacement.copy(
                    state = LessonStateDto.ADDED,
                    startTime = lessonTime?.start ?: LocalTime.MIN,
                    endTime = lessonTime?.end ?: LocalTime.MIN,
                    subject = if (replacement.number == 0) "Классный час" else replacement.subject
                )
            } else if (replacement != null && lesson != null && !lesson.equalsWithReplacement(replacement)) {
                val lessonTime = timeSchedule.firstOrNull { it.number == replacement.number }
                replacement.copy(
                    state = LessonStateDto.CHANGED,
                    startTime = lesson.startTime.takeIf { it != LocalTime.MIN } ?: (lessonTime?.start ?: LocalTime.MIN),
                    endTime = lesson.endTime.takeIf { it != LocalTime.MIN } ?: (lessonTime?.end ?: LocalTime.MIN),
                    subject = lesson.subject.ifEmpty { replacement.subject }
                )
            } else {
                lesson
            }
        }.sortedBy { it.startTime }
    }
}
