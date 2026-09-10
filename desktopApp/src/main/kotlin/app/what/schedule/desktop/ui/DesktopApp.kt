package app.what.schedule.desktop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.desktop.model.SearchItem
import app.what.schedule.desktop.model.UniType
import app.what.schedule.desktop.ui.components.UpdateBanner
import app.what.schedule.desktop.updater.DesktopAppUpdateManager
import app.what.schedule.dgtu.DGTUScheduleClient
import app.what.schedule.iubip.IUBIPScheduleClient
import app.what.schedule.rinh.RINHScheduleClient
import app.what.schedule.rksi.RKSIScheduleClient
import app.what.schedule.rksi.parser.JvmXlsxReader
import app.what.ui.components.SearchDrawerItem
import app.what.ui.components.UniversityOption
import app.what.ui.screens.AdaptiveScheduleScreen
import app.what.ui.theme.WHATTheme
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch

@Composable
fun DesktopApp(httpClient: HttpClient, updateManager: DesktopAppUpdateManager) {
    var selectedUni by remember { mutableStateOf(UniType.RKSI) }
    var searchItems by remember { mutableStateOf<List<SearchItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<SearchItem?>(null) }
    var scheduleDays by remember { mutableStateOf<List<DayScheduleDto>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }
    var isScheduleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val universities = remember {
        UniType.entries.map { UniversityOption(it.name, it.title) }
    }

    val rksiClient = remember { RKSIScheduleClient(client = httpClient, xlsxReader = JvmXlsxReader()) }
    val dgtuClient = remember { DGTUScheduleClient(httpClient) }
    val iubipClient = remember { IUBIPScheduleClient(httpClient) }
    val rinhClient = remember { RINHScheduleClient(httpClient) }

    LaunchedEffect(selectedUni) {
        isSearchLoading = true
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
            isSearchLoading = false
        }
    }

    fun loadSchedule(item: SearchItem) {
        selectedItem = item
        isScheduleLoading = true
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
                isScheduleLoading = false
            }
        }
    }

    WHATTheme {
        AdaptiveScheduleScreen(
            universities = universities,
            selectedUniversityId = selectedUni.name,
            onSelectUniversity = { uniName ->
                val uni = UniType.entries.firstOrNull { it.name == uniName } ?: UniType.RKSI
                selectedUni = uni
            },
            searchItems = searchItems.map { SearchDrawerItem(it.id, it.title, it.isTeacher) },
            selectedItem = selectedItem?.let { SearchDrawerItem(it.id, it.title, it.isTeacher) },
            onSelectItem = { drawerItem ->
                val item = searchItems.firstOrNull { it.id == drawerItem.id && it.isTeacher == drawerItem.isTeacher }
                    ?: SearchItem(drawerItem.id, drawerItem.title, drawerItem.isTeacher)
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
                UpdateBanner(
                    updateInfo = updateManager.updateInfo,
                    onUpdateClick = { updateManager.handleAction() }
                )
            }
        )
    }
}
