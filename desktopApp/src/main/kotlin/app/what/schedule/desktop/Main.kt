package app.what.schedule.desktop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import app.what.domain.services.*
import app.what.schedule.core.models.*
import app.what.schedule.dgtu.DGTUScheduleClient
import app.what.schedule.iubip.IUBIPScheduleClient
import app.what.schedule.rinh.RINHScheduleClient
import app.what.schedule.rksi.RKSIScheduleClient
import app.what.schedule.rksi.parser.JvmXlsxReader
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.URI

class DesktopAppUpdateManager(
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
                val asset = latest.assets.firstOrNull {
                    it.name.endsWith(".jar") || it.name.endsWith(".msi") ||
                    it.name.endsWith(".deb") || it.name.endsWith(".zip") || it.name.endsWith(".exe")
                }
                val info = UpdateInfo(
                    version = latest.tagName,
                    fileSize = asset?.size ?: 0L,
                    downloadUrl = asset?.browserDownloadUrl ?: "https://github.com/${config.githubOwner}/${config.githubRepo}/releases/tag/${latest.tagName}",
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
        val url = updateInfo?.downloadUrl ?: return
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        } catch (_: Exception) {}
    }

    override fun cancelDownload() {}
    override fun release() {}
}

enum class UniType(val title: String) {
    RKSI("РКСИ"),
    DGTU("ДГТУ"),
    IUBIP("ИУБиП"),
    RINH("РИНХ")
}

data class SearchItem(
    val id: String,
    val title: String,
    val isTeacher: Boolean
)

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
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
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
                // Update Banner if available
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
                                "Доступна новая версия: ${update.version}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { updateManager.handleAction() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Обновить")
                            }
                        }
                    }
                }

                // Top bar with university selector
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
                            "WHAT Schedule Desktop",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        UniType.entries.forEach { uni ->
                            FilterChip(
                                selected = selectedUni == uni,
                                onClick = { selectedUni = uni },
                                label = { Text(uni.title) }
                            )
                        }
                    }
                }

                // Main content: sidebar + schedule details
                Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Left sidebar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(320.dp).fillMaxHeight()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Поиск группы / преподавателя...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
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
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (item.isTeacher) Icons.Default.Person else Icons.Default.Group,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    item.title,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Area: Schedule view
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            when {
                                isLoading -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator()
                                        Spacer(Modifier.height(12.dp))
                                        Text("Загрузка расписания...")
                                    }
                                }
                                errorMessage != null -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                selectedItem == null -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                                        Spacer(Modifier.height(12.dp))
                                        Text("Выберите группу или преподавателя слева", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                scheduleDays.isEmpty() -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
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
                                                        "${day.date} (${day.scheduleType})",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    day.lessons.forEach { lesson ->
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
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        lesson.subject.ifBlank { "Пара" },
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        fontSize = 15.sp,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    val unit = lesson.otUnits.firstOrNull()
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

fun main() = application {
    val httpClient = remember {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    val updateManager = remember { DesktopAppUpdateManager(httpClient) }

    Window(
        onCloseRequest = {
            updateManager.release()
            exitApplication()
        },
        title = "WHAT Schedule",
        state = WindowState(size = DpSize(1100.dp, 750.dp))
    ) {
        DesktopApp(httpClient, updateManager)
    }
}
