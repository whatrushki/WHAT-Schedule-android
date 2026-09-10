package app.what.schedule.wasm.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.GroupDto
import app.what.schedule.core.models.TeacherDto
import app.what.schedule.wasm.model.WebInstitutionData
import app.what.schedule.wasm.model.WebSearchItem
import app.what.schedule.wasm.model.WebUniversity
import app.what.schedule.wasm.ui.components.WebUpdateBanner
import app.what.schedule.wasm.updater.WasmUpdateManager
import app.what.ui.components.SearchDrawerItem
import app.what.ui.components.UniversityOption
import app.what.ui.screens.AdaptiveScheduleScreen
import app.what.ui.theme.WHATTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.browser.window
import kotlinx.coroutines.launch

@Composable
fun WebApp(httpClient: HttpClient, updateManager: WasmUpdateManager) {
    var selectedUni by remember { mutableStateOf(WebUniversity.RKSI) }
    var searchItems by remember { mutableStateOf<List<WebSearchItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<WebSearchItem?>(null) }
    var fullScheduleData by remember { mutableStateOf<WebInstitutionData?>(null) }
    var scheduleDays by remember { mutableStateOf<List<DayScheduleDto>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }
    var isScheduleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val universities = remember {
        WebUniversity.entries.map { UniversityOption(it.name, it.title) }
    }

    // Base URL where CI static JSON files are located
    val dataBaseUrl = remember {
        val origin = window.location.origin
        val pathname = window.location.pathname.trimEnd('/')
        "$origin$pathname/schedule"
    }

    LaunchedEffect(selectedUni) {
        isSearchLoading = true
        errorMessage = null
        selectedItem = null
        scheduleDays = emptyList()
        try {
            // First attempt to load full data.json from GitHub Pages static data
            val rawDataUrl = "$dataBaseUrl/${selectedUni.code}/data.json"
            try {
                val fullData = httpClient.get(rawDataUrl).body<WebInstitutionData>()
                fullScheduleData = fullData
                val groups = fullData.groups.map { WebSearchItem(it.name, it.name, isTeacher = false) }
                val teachers = fullData.teachers.map { WebSearchItem(it.id, it.name, isTeacher = true) }
                searchItems = groups + teachers
            } catch (_: Exception) {
                // Fallback to separate files
                val groups = httpClient.get("$dataBaseUrl/${selectedUni.code}/groups.json").body<List<GroupDto>>()
                val teachers = try {
                    httpClient.get("$dataBaseUrl/${selectedUni.code}/teachers.json").body<List<TeacherDto>>()
                } catch (_: Exception) { emptyList() }
                searchItems = groups.map { WebSearchItem(it.name, it.name, isTeacher = false) } +
                        teachers.map { WebSearchItem(it.id, it.name, isTeacher = true) }
            }
        } catch (e: Exception) {
            errorMessage = "Не удалось загрузить данные расписания для ${selectedUni.title}."
        } finally {
            isSearchLoading = false
        }
    }

    fun loadSchedule(item: WebSearchItem) {
        selectedItem = item
        errorMessage = null
        val fullData = fullScheduleData
        if (fullData != null && fullData.schedules.containsKey(item.id)) {
            scheduleDays = fullData.schedules[item.id] ?: emptyList()
            return
        }

        isScheduleLoading = true
        scope.launch {
            try {
                val safeId = item.id.replace("/", "_").replace("\\", "_")
                val groupUrl = "$dataBaseUrl/${selectedUni.code}/groups/$safeId.json"
                val days = httpClient.get(groupUrl).body<List<DayScheduleDto>>()
                scheduleDays = days
            } catch (e: Exception) {
                errorMessage = "Расписание для '${item.title}' пока не сформировано."
            } finally {
                isScheduleLoading = false
            }
        }
    }

    WHATTheme {
        AdaptiveScheduleScreen(
            universities = universities,
            selectedUniversityId = selectedUni.name,
            onSelectUniversity = { uniName ->
                val uni = WebUniversity.entries.firstOrNull { it.name == uniName } ?: WebUniversity.RKSI
                selectedUni = uni
            },
            searchItems = searchItems.map { SearchDrawerItem(it.id, it.title, it.isTeacher) },
            selectedItem = selectedItem?.let { SearchDrawerItem(it.id, it.title, it.isTeacher) },
            onSelectItem = { drawerItem ->
                val item = searchItems.firstOrNull { it.id == drawerItem.id && it.isTeacher == drawerItem.isTeacher }
                    ?: WebSearchItem(drawerItem.id, drawerItem.title, drawerItem.isTeacher)
                loadSchedule(item)
            },
            isSearchLoading = isSearchLoading,
            scheduleDays = scheduleDays,
            isScheduleLoading = isScheduleLoading,
            errorMessage = errorMessage,
            onRefresh = {
                selectedItem?.let { loadSchedule(it) }
            },
            headerBanner = {
                WebUpdateBanner(
                    updateInfo = updateManager.updateInfo,
                    onUpdateClick = { updateManager.handleAction() }
                )
            }
        )
    }
}
