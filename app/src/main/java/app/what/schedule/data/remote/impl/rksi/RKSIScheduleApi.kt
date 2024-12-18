package app.what.schedule.data.remote.impl.rksi

import android.os.Build
import androidx.annotation.RequiresApi
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.ParseMode
import app.what.schedule.data.remote.api.ScheduleApi
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.data.remote.utils.parseTime
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import java.time.LocalDate

class RKSIScheduleApi(
    private val client: HttpClient
) : ScheduleApi(client) {

    companion object {
        const val BASE_URL = "https://www.rksi.ru"
    }

    override suspend fun getTeachers(): List<String> {
        val response = client.get("$BASE_URL/mobile_schedule").bodyAsText()
        val document = Ksoup.parse(response)
        return document.getElementById("teacher")!!.getElementsByTag("option").map { it.text() }
    }

    override suspend fun getGroups(): List<String> {
        val response = client.get("$BASE_URL/mobile_schedule").bodyAsText()
        val document = Ksoup.parse(response)
        return document.getElementById("group")!!.getElementsByTag("option").map { it.text() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getTeacherSchedule(
        teacher: String
    ): List<DaySchedule> = getAndParseSchedule(teacher, ParseMode.TEACHER)

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getGroupSchedule(
        group: String
    ): List<DaySchedule> = getAndParseSchedule(group, ParseMode.GROUP)

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getAndParseSchedule(
        value: String,
        parseMode: ParseMode
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

        val daySchedulesRaw = document
            .getElementsByTag("body")
            .first()!!
            .html()
            .split("</h3>")
            .last()
            .split("<hr>")


        val daySchedules = daySchedulesRaw.map {
            val lessonHtml = Ksoup.parse(it)

            dataRaw = lessonHtml.getElementsByTag("b").text().split(" ")

            val dateDescription = lessonHtml.getElementsByTag("b").first()!!.text()
            val date = LocalDate.now()
                .withDayOfMonth(dataRaw.first().toInt())
                .withMonth(parseMonth(dataRaw[1].substring(0, dataRaw[1].length - 1)))

            val lessons = lessonHtml.getElementsByTag("p").mapNotNull { lessonRaw ->
                if (lessonRaw.html().contains("href")) return@mapNotNull null

                val content = lessonRaw.html().split("<br>")

                dataRaw = content.first().split(" — ")
                val startDate = parseTime(dataRaw.first())
                val endDate = parseTime(dataRaw.last())
                val subject = content[1].substring(3, content[1].length - 4)

                dataRaw = content.last().split(", ")
                val teacherOrGroup = dataRaw.first().substring(2)
                val auditory = dataRaw.last().split(" ").last()

                Lesson(
                    subject = subject,
                    group = if (parseMode == ParseMode.GROUP) value else teacherOrGroup,
                    teacher = if (parseMode == ParseMode.TEACHER) value else teacherOrGroup,
                    auditory = auditory,
                    startDate = startDate,
                    endDate = endDate
                )
            }

            DaySchedule(
                date = date,
                dateDescription = dateDescription,
                lessons = lessons
            )
        }

        return daySchedules
    }
}
