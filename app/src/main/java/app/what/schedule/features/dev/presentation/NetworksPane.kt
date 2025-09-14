package app.what.schedule.features.dev.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.what.foundation.ui.Gap
import app.what.foundation.ui.VerticalGap
import app.what.schedule.features.dev.presentation.components.Filter
import app.what.schedule.features.dev.presentation.components.FilteredList
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Crown
import app.what.schedule.ui.theme.icons.filled.Export
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.contentLength
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale
import java.util.UUID


@Composable
fun NetworksPane(
    modifier: Modifier = Modifier
) {
    FilteredList(
        "Сетевые запросы",
        values = NetworkMonitor.requests,
        vKey = { it.id },
        vContent = { NetworkRequestItem(it) },
        exportValues = NetworkMonitor::exportRequests,
        clearValues = NetworkMonitor::clearRequests,
        setIsMonitoringPaused = NetworkMonitor::setMonitoringPause,
        isMonitoringPaused = NetworkMonitor.isMonitoringPaused,
        modifier = modifier,
        filter = NetworkFilter(),
        filterHelpItems = listOf(

        )
    )
}


data class NetworkRequest(
    val id: UUID,
    val url: String,
    val method: String,
    val statusCode: Int,
    val timestamp: Long,
    val duration: Duration,
    val requestSize: Long = 0,
    val responseSize: Long = 0,
    val requestHeaders: Map<String, String> = emptyMap(),
    val responseHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseBody: String? = null,
    val error: String? = null
) {
    val isSuccessful: Boolean get() = statusCode in 200..299
    val contentType: String?
        get() = requestHeaders["Content-Type"] ?: requestHeaders["content-type"]
}

// 2. Менеджер сетевых запросов
object NetworkMonitor {
    private val _requests = mutableStateListOf<NetworkRequest>()
    val requests: List<NetworkRequest> get() = _requests

    var isMonitoringPaused by mutableStateOf(false)
        private set

    fun setMonitoringPause(value: Boolean) {
        isMonitoringPaused = value
    }

    fun trackRequest(
        request: NetworkRequest
    ) {
        if (isMonitoringPaused) return

        _requests.add(request)

        // Ограничиваем размер списка
        if (_requests.size > 1000) {
            _requests.removeAt(0)
        }
    }

    fun toggleMonitoring(paused: Boolean) {
        isMonitoringPaused = paused
    }

    fun clearRequests() {
        _requests.clear()
    }

    fun exportRequests(): String {
        return Json.encodeToString(requests)
    }
}


val NetworkMonitorPlugin = createClientPlugin("NetworkMonitor") {
    val callIdKey = AttributeKey<UUID>("CallId")

    on(SendingRequest) { request, content ->
        val callId = UUID.randomUUID()
        request.attributes.put(callIdKey, callId)
    }

    onResponse { response ->
        val callId: UUID = response.call.attributes[callIdKey]

        NetworkMonitor.trackRequest(
            NetworkRequest(
                id = callId,
                url = response.request.url.toString(),
                method = response.request.method.value,
                statusCode = response.status.value,
                timestamp = response.responseTime.timestamp,
                duration = Duration.ofMillis(response.responseTime.timestamp - response.requestTime.timestamp),
                requestSize = response.contentLength() ?: 0,
                responseSize = response.contentLength() ?: 0,
                requestBody = response.request.content.toString(),
                responseBody = response.bodyAsText(),
                error = null
            )
        )
    }
}

class NetworkFilter : Filter<NetworkRequest> {
    private val methodFilters = mutableListOf<String>()
    private val statusFilters = mutableListOf<Int>()
    private val urlFilters = mutableListOf<String>()
    private var successFilter: Boolean? = null

    override fun clearFilters() {
        methodFilters.clear()
        statusFilters.clear()
        urlFilters.clear()
        successFilter = null
    }

    override fun parseQuery(query: String) {
        val patterns = listOf(
            Regex("""method:(\w+)"""),
            Regex("""status:(\d+)"""),
            Regex("""url:([\w\.\/:-]+)"""),
            Regex("""is:(\w+)"""),
            Regex(""""([^"]+)""""),
            Regex("""(\S+)""")
        )

        patterns.flatMap { it.findAll(query) }.forEach { match ->
            when {
                match.value.startsWith("method:") -> {
                    methodFilters.add(match.groupValues[1].uppercase())
                }

                match.value.startsWith("status:") -> {
                    match.groupValues[1].toIntOrNull()?.let {
                        statusFilters.add(it)
                    }
                }

                match.value.startsWith("url:") -> {
                    urlFilters.add(match.groupValues[1])
                }

                match.value.startsWith("is:") -> {
                    if (match.groupValues[1] == "success") {
                        successFilter = true
                    } else if (match.groupValues[1] == "error") {
                        successFilter = false
                    }
                }

                match.value.startsWith("\"") -> {
                    urlFilters.add(match.groupValues[1])
                }

                else -> {
                    if (!match.value.contains(":")) {
                        urlFilters.add(match.value)
                    }
                }
            }
        }
    }

    override fun matches(value: NetworkRequest): Boolean {
        if (methodFilters.isNotEmpty() && value.method !in methodFilters) return false
        if (statusFilters.isNotEmpty() && value.statusCode !in statusFilters) return false
        if (successFilter != null && value.isSuccessful != successFilter) return false
        if (urlFilters.isNotEmpty() && urlFilters.none {
                value.url.contains(
                    it,
                    true
                )
            }) return false

        return true
    }
}

@Composable
fun NetworkStats(requests: List<NetworkRequest>) {
    val stats = remember(requests) {
        val successful = requests.count { it.isSuccessful }
        val totalSize = requests.sumOf { it.requestSize + it.responseSize }
        val avgTime =
            if (requests.isNotEmpty()) requests.sumOf { it.duration.seconds } / requests.size else 0

        Triple(successful, totalSize, avgTime)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(colorScheme.surfaceContainer, shapes.medium)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem(
            value = requests.size.toString(),
            label = "Всего",
            icon = WHATIcons.Crown
        )
        StatItem(
            value = stats.first.toString(),
            label = "Успешно",
            icon = WHATIcons.Crown,
            color = colorScheme.primary
        )
        StatItem(
            value = "${stats.second / 1024} KB",
            label = "Объем",
            icon = WHATIcons.Crown
        )
        StatItem(
            value = "${stats.second}ms",
            label = "Среднее время",
            icon = WHATIcons.Crown
        )
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color = colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.12f), CircleShape)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = value,
            style = typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = label,
            style = typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NetworkRequestItem(request: NetworkRequest) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    val statusColor = when {
        request.statusCode >= 500 -> MaterialTheme.colorScheme.error
        request.statusCode >= 400 -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.primary
    }

    val statusBackground = when {
        request.statusCode >= 500 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        request.statusCode >= 400 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = statusBackground),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { showDetailsDialog = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Статус код
            Box(
                modifier = Modifier
                    .background(statusColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${request.method} ${request.statusCode}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // URL
            Text(
                text = request.url,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Время и размер
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${request.duration.seconds * 1000}ms",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                if (request.responseSize > 0) {
                    Text(
                        text = "${request.responseSize / 1024} KB",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    if (showDetailsDialog) {
        NetworkRequestDialog(
            request = request,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun NetworkRequestDialog(
    request: NetworkRequest,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = shapes.extraLarge,
            color = colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Заголовок диалога
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Детали запроса",
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, "Закрыть")
                    }
                }

                Divider()

                // Контент
                NetworkRequestDetailsContent(request = request)
            }
        }
    }
}

@Composable
fun NetworkRequestDetailsContent(request: NetworkRequest) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Обзор", "Headers", "Body", "Timing")

    Column(modifier = Modifier.fillMaxSize()) {
        // Табы
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // Контент табов
        when (selectedTab) {
            0 -> OverviewTab(request)
            1 -> HeadersTab(request)
            2 -> BodyTab(request)
            3 -> TimingTab(request)
        }
    }
}

@Composable
fun OverviewTab(request: NetworkRequest) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            RequestInfoItem("URL", request.url)
            RequestInfoItem("Method", request.method)
            RequestInfoItem("Status", "${request.statusCode} ${getStatusText(request.statusCode)}")
            RequestInfoItem("Duration", "${request.duration.seconds * 1000}ms")
            RequestInfoItem("Request Size", formatBytes(request.requestSize))
            RequestInfoItem("Response Size", formatBytes(request.responseSize))
            RequestInfoItem("Timestamp", formatTimestamp(request.timestamp))

            if (request.error != null) {
                VerticalGap(16)
                Text(
                    text = "Ошибка:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = request.error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RequestInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun HeadersTab(request: NetworkRequest) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Request Headers:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (request.requestHeaders.isEmpty()) {
            item {
                Text(
                    "No headers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(request.requestHeaders.entries.toList()) { (key, value) ->
                HeaderItem(key, value)
            }
        }

        item {
            Text(
                text = "Response Headers:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (request.responseHeaders.isEmpty()) {
            item {
                Text(
                    "No headers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(request.responseHeaders.entries.toList()) { (key, value) ->
                HeaderItem(key, value)
            }
        }
    }
}

@Composable
fun HeaderItem(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ключ
        Text(
            text = key,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(140.dp)
        )

        // Разделитель
        Text(
            text = ":",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )

        // Значение
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            softWrap = true
        )

        // Кнопка копирования
        IconButton(
            onClick = {
                val textToCopy = "$key: $value"
                // Копирование в буфер обмена
            },
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = WHATIcons.Export,
                contentDescription = "Копировать",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    Divider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        thickness = 0.5.dp
    )
}

@Composable
fun BodyTab(request: NetworkRequest) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Request Body:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        item {
            if (request.requestBody.isNullOrEmpty()) {
                Text("No body", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            } else {
                Text(
                    text = request.requestBody,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )
            }
        }

        item {
            Text(
                text = "Response Body:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            if (request.responseBody.isNullOrEmpty()) {
                Text("No body", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            } else {
                Text(
                    text = request.responseBody!!,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun TimingTab(request: NetworkRequest) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TimingItem("Total Time", "${request.duration.seconds * 1000}ms")
        TimingItem("Request Size", formatBytes(request.requestSize))
        TimingItem("Response Size", formatBytes(request.responseSize))
        TimingItem("Timestamp", formatTimestamp(request.timestamp))

        if (request.error != null) {
            Gap(16)
            Text(
                text = "Error:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = request.error,
                color = colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

fun getStatusText(statusCode: Int): String {
    return when (statusCode) {
        in 100..199 -> "Informational"
        in 200..299 -> "Success"
        in 300..399 -> "Redirection"
        in 400..499 -> "Client Error"
        in 500..599 -> "Server Error"
        else -> "Unknown"
    }
}

@Composable
fun TimingItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}