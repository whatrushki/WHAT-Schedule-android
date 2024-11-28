package app.what.schedule.data.remote.impl.turtle

import android.os.Build
import androidx.annotation.RequiresApi
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.ParseMode
import app.what.schedule.data.remote.api.ScheduleApi
import app.what.schedule.data.remote.impl.turtle.TurtleApi.Schedule.Responses.Get.Companion.toDaySchedules
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class TurtleScheduleApi(
    private val client: HttpClient
) : ScheduleApi(client) {

    companion object {
        const val BASE_URL = "http://45.155.207.232:8080/api/v2"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getGroupSchedule(
        group: String
    ): List<DaySchedule> = client
        .get("$BASE_URL/schedule/$group")
        .body<TurtleApi.Schedule.Responses.Get>()
        .toDaySchedules(ParseMode.GROUP)

    override suspend fun getTeacherSchedule(
        teacher: String
    ): List<DaySchedule> = client
        .get("$BASE_URL/schedule/$teacher")
        .body<TurtleApi.Schedule.Responses.Get>()
        .toDaySchedules(ParseMode.TEACHER)

    override suspend fun getGroups(): List<String> = client
        .get("$BASE_URL/schedule/list")
        .body<Map<String, List<String>>>()
        .get("group")!!

    override suspend fun getTeachers(): List<String> = client
        .get("$BASE_URL/schedule/list")
        .body<Map<String, List<String>>>()
        .get("teacher")!!
}