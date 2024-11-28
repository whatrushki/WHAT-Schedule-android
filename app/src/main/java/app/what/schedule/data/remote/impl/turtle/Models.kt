package app.what.schedule.data.remote.impl.turtle

import android.annotation.SuppressLint
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.ParseMode
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.data.remote.utils.parseTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate


object TurtleApi {
    object Schedule {
        object Responses {
            @Serializable
            data class Get(
                val days: List<ForDay>,
                val name: String
            ) {
                companion object {
                    @SuppressLint("NewApi")
                    fun Get.toDaySchedules(parseMode: ParseMode): List<DaySchedule> =
                        this.days.map {
                            val dataRaw = it.day.split(" ")
                            val date = LocalDate.now()
                                .withDayOfMonth(dataRaw.first().toInt())
                                .withMonth(
                                    parseMonth(
                                        dataRaw[1].substring(
                                            0,
                                            dataRaw[1].length - 1
                                        )
                                    )
                                )

                            val lessons = mutableListOf<Lesson>()
                            it.apairs.forEach {
                                val mapped = it.apair.map { lesson ->
                                    Lesson(
                                        subject = lesson.doctrine,
                                        teacher = if (parseMode == ParseMode.TEACHER) name else lesson.teacher,
                                        auditory = lesson.auditory,
                                        startDate = parseTime(lesson.start),
                                        endDate = parseTime(lesson.end),
                                        group = if (parseMode == ParseMode.TEACHER) lesson.teacher else name
                                    )
                                }
                                lessons.addAll(mapped)
                            }

                            DaySchedule(
                                date = date,
                                dateDescription = it.day,
                                lessons = lessons
                            )
                        }
                }
            }

            @Serializable
            data class ForDay(
                val day: String,
                val isoDateDay: String,
                val apairs: List<Apair>
            )

            @Serializable
            data class Apair(
                val time: String,
                val apair: List<ApairApair>,
                val isoDateStart: String,
                val isoDateEnd: String
            )

            @Serializable
            data class ApairApair(
                val doctrine: String,
                val teacher: String,
                @SerialName("auditoria") val auditory: String,
                val corpus: String,
                val number: Int,
                val start: String,
                val end: String,
                val warn: String
            )
        }
    }
}