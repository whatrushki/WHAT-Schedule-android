package app.what.schedule.wasm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.CanvasBasedWindow
import app.what.domain.services.*
import app.what.schedule.core.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class WasmUpdateManager(
    private val httpClient: HttpClient,
    private val config: UpdateConfig = UpdateConfig(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AppUpdateManager {
    override var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    private val _downloadState = mutableStateOf<DownloadState>(DownloadState.Idle)
    override val downloadState: DownloadState get() = _downloadState.value

    init {
        scope.launch {
            try {
                checkForUpdates()
            } catch (_: Exception) {}
        }
    }

    override suspend fun checkForUpdates(): UpdateResult {
        return try {
            val releases = httpClient.get("https://api.github.com/repos/${config.githubOwner}/${config.githubRepo}/releases") {
                parameter("per_page", 5)
            }.body<List<GitHubRelease>>()

            val latest = releases.firstOrNull { !it.draft && !it.prerelease } ?: return UpdateResult.NotAvailable
            val cleanLatest = latest.tagName.trimStart('v', 'V')
            val cleanCurrent = config.currentVersion.trimStart('v', 'V')

            if (cleanLatest > cleanCurrent) {
                val info = UpdateInfo(
                    version = latest.tagName,
                    fileSize = 0L,
                    downloadUrl = window.location.href,
                    releaseNotes = latest.body
                )
                updateInfo = info
                UpdateResult.Available(info)
            } else {
                UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Failed to check updates")
        }
    }

    override fun handleAction() {
        window.location.reload()
    }

    override fun cancelDownload() {}
    override fun release() {}
}

data class WebSearchItem(
    val id: String,
    val title: String,
    val isTeacher: Boolean = false
)

@Serializable
data class WebInstitutionData(
    val lastSync: String = "",
    val institution: String = "",
    val groups: List<GroupDto> = emptyList(),
    val teachers: List<TeacherDto> = emptyList(),
    val schedules: Map<String, List<DayScheduleDto>> = emptyMap()
)

enum class WebUniversity(val code: String, val title: String) {
    RKSI("rksi", "РКСИ"),
    DGTU("dgtu", "ДГТУ"),
    IUBIP("iubip", "ИУБиП"),
    RINH("rinh", "РИНХ")
}

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
                // Update banner
                updateManager.updateInfo?.let { update ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Доступна новая версия сайта (${update.version})",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { updateManager.handleAction() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Обновить страницу")
                            }
                        }
                    }
                }

                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "WHAT Schedule Web",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        WebUniversity.entries.forEach { uni ->
                            FilterChip(
                                selected = selectedUni == uni,
                                onClick = { selectedUni = uni },
                                label = { Text(uni.title) }
                            )
                        }
                    }
                }

                // Body
                Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Left list
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(300.dp).fillMaxHeight()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Поиск...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))

                            val filtered = remember(searchQuery, searchItems) {
                                if (searchQuery.isBlank()) searchItems
                                else searchItems.filter { it.title.contains(searchQuery, ignoreCase = true) }
                            }

                            if (isLoading && searchItems.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(filtered) { item ->
                                        val isSelected = selectedItem?.id == item.id
                                        Surface(
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().clickable { loadSchedule(item) }
                                        ) {
                                            Text(
                                                item.title,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Schedule view
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            when {
                                isLoading -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                errorMessage != null -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                selectedItem == null -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Выберите группу или преподавателя слева", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                scheduleDays.isEmpty() -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Расписание отсутствует или выходные дни", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                else -> {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(scheduleDays) { day ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Text(
                                                        "${day.date} (${day.date.dayOfWeek.name})",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    for (lesson in day.lessons) {
                                                        Surface(
                                                            color = MaterialTheme.colorScheme.surface,
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(12.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    "${lesson.number}",
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    fontSize = 18.sp,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.width(32.dp)
                                                                )
                                                                val unit = lesson.otUnits.firstOrNull()
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        lesson.subject.ifBlank { "Пара" },
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        fontSize = 15.sp,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    val details = listOfNotNull(
                                                                        "${lesson.startTime} – ${lesson.endTime}",
                                                                        unit?.teacher?.takeIf { it.isNotBlank() },
                                                                        unit?.room?.takeIf { it.isNotBlank() }
                                                                    ).joinToString(" • ")
                                                                    Text(
                                                                        details,
                                                                        fontSize = 13.sp,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                                if (lesson.state == LessonStateDto.CHANGED || lesson.state == LessonStateDto.ADDED) {
                                                                    Surface(
                                                                        color = Color(0xFFE53935),
                                                                        shape = RoundedCornerShape(4.dp)
                                                                    ) {
                                                                        Text(
                                                                            "Замена",
                                                                            color = Color.White,
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    val updateManager = WasmUpdateManager(httpClient)

    CanvasBasedWindow(title = "WHAT Schedule Web", canvasElementId = "ComposeTarget") {
        WebApp(httpClient, updateManager)
    }
}
