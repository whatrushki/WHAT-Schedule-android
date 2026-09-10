package app.what.schedule.wasm.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.what.compose.App
import app.what.compose.ScheduleDataLoader
import app.what.compose.University
import app.what.compose.toDomain
import app.what.domain.models.DaySchedule
import app.what.domain.models.ScheduleSearch
import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.GroupDto
import app.what.schedule.core.models.TeacherDto
import app.what.schedule.wasm.model.WebInstitutionData
import app.what.schedule.wasm.ui.components.WebUpdateBanner
import app.what.schedule.wasm.updater.WasmUpdateManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.browser.window

class WasmScheduleDataLoader(
    private val httpClient: HttpClient,
    private val dataBaseUrl: String
) : ScheduleDataLoader {
    private var fullData: WebInstitutionData? = null

    override suspend fun getSearches(university: University): List<ScheduleSearch> {
        val code = when (university) {
            University.RKSI -> "rksi"
            University.DGTU -> "dgtu"
            University.IUBIP -> "iubip"
            University.RINH -> "rinh"
        }
        val rawDataUrl = "$dataBaseUrl/$code/data.json"
        return try {
            val data = httpClient.get(rawDataUrl).body<WebInstitutionData>()
            fullData = data
            val groups = data.groups.map { ScheduleSearch.Group(it.name, it.name) }
            val teachers = data.teachers.map { ScheduleSearch.Teacher(it.name, it.id) }
            groups + teachers
        } catch (_: Exception) {
            val groups = httpClient.get("$dataBaseUrl/$code/groups.json").body<List<GroupDto>>()
            val teachers = try {
                httpClient.get("$dataBaseUrl/$code/teachers.json").body<List<TeacherDto>>()
            } catch (_: Exception) { emptyList() }
            groups.map { ScheduleSearch.Group(it.name, it.name) } +
                teachers.map { ScheduleSearch.Teacher(it.name, it.id) }
        }
    }

    override suspend fun getSchedule(university: University, search: ScheduleSearch): List<DaySchedule> {
        val code = when (university) {
            University.RKSI -> "rksi"
            University.DGTU -> "dgtu"
            University.IUBIP -> "iubip"
            University.RINH -> "rinh"
        }
        val currentFullData = fullData
        val dtos = if (currentFullData != null && currentFullData.schedules.containsKey(search.name)) {
            currentFullData.schedules[search.name] ?: emptyList()
        } else {
            val safeId = search.name.replace("/", "_").replace("\\", "_")
            httpClient.get("$dataBaseUrl/$code/groups/$safeId.json").body<List<DayScheduleDto>>()
        }
        return dtos.map { it.toDomain() }
    }
}

@Composable
fun WebApp(httpClient: HttpClient, updateManager: WasmUpdateManager) {
    val dataBaseUrl = remember {
        val origin = window.location.origin
        val pathname = window.location.pathname.trimEnd('/')
        "$origin$pathname/schedule"
    }

    val dataLoader = remember(httpClient, dataBaseUrl) {
        WasmScheduleDataLoader(httpClient, dataBaseUrl)
    }

    App(
        httpClient = httpClient,
        dataLoader = dataLoader,
        headerBanner = {
            WebUpdateBanner(
                updateInfo = updateManager.updateInfo,
                onUpdateClick = { updateManager.handleAction() }
            )
        }
    )
}
