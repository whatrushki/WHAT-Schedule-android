package app.what.schedule.wasm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.GroupDto
import app.what.schedule.core.models.TeacherDto
import app.what.schedule.wasm.model.WebInstitutionData
import app.what.schedule.wasm.model.WebSearchItem
import app.what.schedule.wasm.model.WebUniversity
import app.what.schedule.wasm.ui.components.WebHeaderBar
import app.what.schedule.wasm.ui.components.WebScheduleView
import app.what.schedule.wasm.ui.components.WebSearchSidebar
import app.what.schedule.wasm.ui.components.WebUpdateBanner
import app.what.schedule.wasm.updater.WasmUpdateManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.browser.window
import kotlinx.coroutines.launch

@Composable
fun WebApp(httpClient: HttpClient, updateManager: WasmUpdateManager) {
    var selectedUni by remember { mutableStateOf(WebUniversity.RKSI) }
    var searchQuery by remember { mutableStateOf("") }
    var searchItems by remember { mutableStateOf<List<WebSearchItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<WebSearchItem?>(null) }
    var fullScheduleData by remember { mutableStateOf<WebInstitutionData?>(null) }
    var scheduleDays by remember { mutableStateOf<List<DayScheduleDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Base URL where CI static JSON files are located
    val dataBaseUrl = remember {
        val origin = window.location.origin
        val pathname = window.location.pathname.trimEnd('/')
        "$origin$pathname/schedule"
    }

    LaunchedEffect(selectedUni) {
        isLoading = true
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
            errorMessage = "Не удалось загрузить данные расписания для ${selectedUni.title}. Возможно, данные ещё синхронизируются через GitHub Actions."
        } finally {
            isLoading = false
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

        isLoading = true
        scope.launch {
            try {
                val safeId = item.id.replace("/", "_").replace("\\", "_")
                val groupUrl = "$dataBaseUrl/${selectedUni.code}/groups/$safeId.json"
                val days = httpClient.get(groupUrl).body<List<DayScheduleDto>>()
                scheduleDays = days
            } catch (e: Exception) {
                errorMessage = "Расписание для '${item.title}' пока не сформировано."
            } finally {
                isLoading = false
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6C63FF),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121218),
            surface = Color(0xFF1E1E2A),
            surfaceVariant = Color(0xFF282838),
            onPrimary = Color.White,
            onSurface = Color(0xFFE2E2E8)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                WebUpdateBanner(
                    updateInfo = updateManager.updateInfo,
                    onUpdateClick = { updateManager.handleAction() }
                )

                WebHeaderBar(
                    selectedUni = selectedUni,
                    onSelectUni = { selectedUni = it }
                )

                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WebSearchSidebar(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        searchItems = searchItems,
                        selectedItem = selectedItem,
                        isLoading = isLoading,
                        onSelectItem = { loadSchedule(it) }
                    )

                    WebScheduleView(
                        scheduleDays = scheduleDays,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        selectedItem = selectedItem,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
