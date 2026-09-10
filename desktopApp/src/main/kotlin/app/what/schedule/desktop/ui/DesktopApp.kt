package app.what.schedule.desktop.ui

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
import app.what.schedule.desktop.model.SearchItem
import app.what.schedule.desktop.model.UniType
import app.what.schedule.desktop.ui.components.DesktopHeaderBar
import app.what.schedule.desktop.ui.components.DesktopScheduleView
import app.what.schedule.desktop.ui.components.DesktopSearchSidebar
import app.what.schedule.desktop.ui.components.UpdateBanner
import app.what.schedule.desktop.updater.DesktopAppUpdateManager
import app.what.schedule.dgtu.DGTUScheduleClient
import app.what.schedule.iubip.IUBIPScheduleClient
import app.what.schedule.rinh.RINHScheduleClient
import app.what.schedule.rksi.RKSIScheduleClient
import app.what.schedule.rksi.parser.JvmXlsxReader
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch

@Composable
fun DesktopApp(httpClient: HttpClient, updateManager: DesktopAppUpdateManager) {
    var selectedUni by remember { mutableStateOf(UniType.RKSI) }
    var searchQuery by remember { mutableStateOf("") }
    var searchItems by remember { mutableStateOf<List<SearchItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<SearchItem?>(null) }
    var scheduleDays by remember { mutableStateOf<List<DayScheduleDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val rksiClient = remember { RKSIScheduleClient(client = httpClient, xlsxReader = JvmXlsxReader()) }
    val dgtuClient = remember { DGTUScheduleClient(httpClient) }
    val iubipClient = remember { IUBIPScheduleClient(httpClient) }
    val rinhClient = remember { RINHScheduleClient(httpClient) }

    LaunchedEffect(selectedUni) {
        isLoading = true
        errorMessage = null
        selectedItem = null
        scheduleDays = emptyList()
        try {
            when (selectedUni) {
                UniType.RKSI -> {
                    val groups = rksiClient.getGroups().map { SearchItem(it.id, it.name, false) }
                    val teachers = rksiClient.getTeachers().map { SearchItem(it.id, it.name, true) }
                    searchItems = groups + teachers
                }
                UniType.DGTU -> {
                    val groups = dgtuClient.getGroups().map { SearchItem(it.id, it.name, false) }
                    searchItems = groups
                }
                UniType.IUBIP -> {
                    val groups = iubipClient.getGroups().map { SearchItem(it.id, it.name, false) }
                    searchItems = groups
                }
                UniType.RINH -> {
                    val groups = rinhClient.getGroups().map { SearchItem(it.id, it.name, false) }
                    searchItems = groups
                }
            }
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки списка: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun loadSchedule(item: SearchItem) {
        selectedItem = item
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                scheduleDays = when (selectedUni) {
                    UniType.RKSI -> {
                        if (item.isTeacher) rksiClient.getTeacherSchedule(item.title)
                        else rksiClient.getGroupSchedule(item.title)
                    }
                    UniType.DGTU -> {
                        if (item.isTeacher) dgtuClient.getTeacherSchedule(item.id)
                        else dgtuClient.getGroupSchedule(item.id)
                    }
                    UniType.IUBIP -> {
                        if (item.isTeacher) iubipClient.getTeacherSchedule(item.id)
                        else iubipClient.getGroupSchedule(item.title)
                    }
                    UniType.RINH -> {
                        if (item.isTeacher) rinhClient.getTeacherSchedule(item.id)
                        else rinhClient.getGroupSchedule(item.title)
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки расписания: ${e.message}"
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
                UpdateBanner(
                    updateInfo = updateManager.updateInfo,
                    onUpdateClick = { updateManager.handleAction() }
                )

                DesktopHeaderBar(
                    selectedUni = selectedUni,
                    onSelectUni = { selectedUni = it }
                )

                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DesktopSearchSidebar(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        searchItems = searchItems,
                        selectedItem = selectedItem,
                        isLoading = isLoading,
                        onSelectItem = { loadSchedule(it) }
                    )

                    DesktopScheduleView(
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
