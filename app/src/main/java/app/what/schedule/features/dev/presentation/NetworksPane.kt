package app.what.schedule.features.dev.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.controllers.rememberDialogController
import app.what.schedule.features.dev.presentation.components.Filter
import app.what.schedule.features.dev.presentation.components.FilteredList
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow


@Composable
fun NetworksPane(
    modifier: Modifier = Modifier
) {
    val dialog = rememberDialogController()
    FilteredList(
        "Сетевые запросы",
        values = NetworkMonitor.requests,
        vKey = { it.id },
        vContent = {
            NetworkRequestItem(it) {
                dialog.open(true) {
                    NetworkRequestDialog(it)
                }
            }
        }, exportValues = NetworkMonitor::exportRequests,
        clearValues = NetworkMonitor::clearRequests,
        setIsMonitoringPaused = NetworkMonitor::setMonitoringPause,
        isMonitoringPaused = NetworkMonitor.isMonitoringPaused,
        modifier = modifier,
        filter = NetworkFilter(),
        filterHelpItems = listOf(
            "method:POST" to "Фильтр по методу (GET, POST, PUT, DELETE)",
            "status:500" to "Фильтр по коду статуса",
            "is:success" to "Только успешные запросы (200-299)",
            "is:error" to "Только ошибки (4xx, 5xx, обрыв сети)",
            "url:google" to "Явный поиск по вхождению в URL",
            "api/v1" to "Быстрый поиск по части URL"
        )
    )
}

//@Serializable
data class NetworkRequest(
    val id: UUID = UUID.randomUUID(),
    val url: String,
    val method: String,
    val statusCode: Int? = null,
    val requestTime: Long,
    var responseTime: Long? = null,
    var endTime: Long? = null,
    val requestHeaders: Map<String, String> = emptyMap(),
    var responseHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    var responseBody: String? = null,
    var error: String? = null,
    val requestSize: Long = 0,
    var responseSize: Long = 0
) {
    val isSuccessful: Boolean get() = statusCode in 200..299
    val isWebSocket: Boolean get() = requestHeaders["Upgrade"]?.equals("websocket", true) == true

    val host: String get() = runCatching { Url(url).host }.getOrDefault(url)
    val path: String get() = runCatching { Url(url).encodedPath }.getOrDefault("/")

    val queryParams: List<Pair<String, String>>
        get() = runCatching {
            Url(url).parameters.entries().flatMap { (key, values) -> values.map { key to it } }
        }.getOrDefault(emptyList())

    val requestCookies: Map<String, String> get() = parseCookies(requestHeaders["Cookie"])
    val responseCookies: Map<String, String> get() = parseSetCookies(responseHeaders["Set-Cookie"])

    val duration: Long get() = if (endTime != null && responseTime != null) endTime!! - requestTime else 0
    val latency: Long get() = if (responseTime != null) responseTime!! - requestTime else 0

    val contentType: String?
        get() = responseHeaders[HttpHeaders.ContentType] ?: responseHeaders["content-type"]

    val statusCategory: StatusCategory
        get() = when (statusCode) {
            in 200..299 -> StatusCategory.Success
            in 300..399 -> StatusCategory.Redirect
            in 400..599 -> StatusCategory.Error
            null -> StatusCategory.Pending
            else -> StatusCategory.Unknown
        }

    private fun parseCookies(header: String?): Map<String, String> {
        if (header.isNullOrEmpty()) return emptyMap()
        return header.split(";").associate {
            val parts = it.split("=", limit = 2)
            (parts.getOrNull(0)?.trim() ?: "") to (parts.getOrNull(1)?.trim() ?: "")
        }
    }

    private fun parseSetCookies(header: String?): Map<String, String> = parseCookies(header)
}

enum class StatusCategory {
    Success, Redirect, Error, Pending, Unknown
}

object NetworkMonitor {
    private val _requests = mutableStateListOf<NetworkRequest>()
    val requests: List<NetworkRequest> get() = _requests

    var isMonitoringPaused by mutableStateOf(false)
        private set

    fun setMonitoringPause(value: Boolean) {
        isMonitoringPaused = value
    }

    fun trackRequest(request: NetworkRequest) {
        if (isMonitoringPaused) return
        _requests.add(request)
        if (_requests.size > 1000) _requests.removeAt(_requests.lastIndex)
    }

    suspend fun updateRequest(id: UUID, update: suspend (NetworkRequest) -> NetworkRequest) {
        val index = _requests.indexOfFirst { it.id == id }
        if (index != -1) {
            _requests[index] = update(_requests[index])
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

private val json = Json { prettyPrint = true }
val NetworkMonitorPlugin = createClientPlugin("NetworkMonitor") {
    val callIdKey = AttributeKey<UUID>("CallId")

    on(SendingRequest) { request, content ->
        val callId = UUID.randomUUID()
        request.attributes.put(callIdKey, callId)

        val requestBodyString = content.decodeContent()

        val netRequest = NetworkRequest(
            id = callId,
            url = request.url.toString(),
            method = request.method.value,
            requestTime = System.currentTimeMillis(),
            requestHeaders = request.headers.entries()
                .associate { it.key to it.value.joinToString(", ") },
            requestBody = requestBodyString,
            requestSize = requestBodyString.length.toLong()
        )
        NetworkMonitor.trackRequest(netRequest)
    }

    client.sendPipeline.intercept(HttpSendPipeline.Engine) {
        val callId = context.attributes.getOrNull(callIdKey) ?: return@intercept
        try {
            proceed()
        } catch (e: Exception) {
            NetworkMonitor.updateRequest(callId) {
                it.copy(
                    error = "${e::class.simpleName}: ${e.message}",
                    endTime = System.currentTimeMillis()
                )
            }
            throw e
        }
    }

    onResponse { response ->
        val callId = response.call.attributes.getOrNull(callIdKey) ?: return@onResponse
        val responseTime = System.currentTimeMillis()

        NetworkMonitor.updateRequest(callId) {
            it.copy(
                statusCode = response.status.value,
                responseTime = responseTime,
                responseHeaders = response.headers.entries()
                    .associate { entry -> entry.key to entry.value.joinToString(", ") }
            )
        }

        try {

            NetworkMonitor.updateRequest(callId) {
                val isImage = it.responseHeaders["Content-Type"]?.contains("image") ?: false

                val (text, size) = if (!isImage) {
                    val body = response.bodyAsText()
                    try {
                        json.encodeToString(json.decodeFromString<JsonElement>(body))

                    } catch (_: Exception) {
                        body
                    }.let { it to it.length.toLong() }
                } else "image" to (it.responseHeaders["Content-Length"]?.toLong() ?: 0L)

                it.copy(
                    endTime = System.currentTimeMillis(),
                    responseBody = if (isImage) "image" else text,
                    responseSize = size
                )
            }
        } catch (e: Exception) {
            NetworkMonitor.updateRequest(callId) {
                it.copy(error = "Read Error: ${e.message}", endTime = System.currentTimeMillis())
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun OutgoingContent.decodeContent(): String {
    return when (this) {
        is OutgoingContent.ByteArrayContent -> bytes().decodeToString()
        is OutgoingContent.ReadChannelContent -> readFrom().readRemaining()
            .readString(Charsets.UTF_8)

        is OutgoingContent.WriteChannelContent -> {
            val channel = ByteChannel(true)
            GlobalScope.launch(currentCoroutineContext() + CoroutineName("decodeContent")) {
                writeTo(channel)
                channel.close()
            }
            channel.readRemaining().readString(Charsets.UTF_8)
        }

        is OutgoingContent.NoContent -> ""
        else -> ""
    }
}

class NetworkFilter : Filter<NetworkRequest> {
    private val methodFilters = mutableListOf<String>()
    private val statusFilters = mutableListOf<Int>()
    private val textFilters =
        mutableListOf<String>()

    private var isSuccessFilter: Boolean? = null

    override fun clearFilters() {
        methodFilters.clear()
        statusFilters.clear()
        textFilters.clear()
        isSuccessFilter = null
    }

    override fun parseQuery(query: String) {
        clearFilters()

        // Разбиваем по пробелам, игнорируя пустые части
        val tokens = query.trim().split("\\s+".toRegex())

        tokens.forEach { token ->
            when {
                token.startsWith("method:", ignoreCase = true) -> {
                    val value = token.substringAfter(":")
                    if (value.isNotBlank()) methodFilters.add(value.uppercase())
                }

                token.startsWith("status:", ignoreCase = true) -> {
                    token.substringAfter(":").toIntOrNull()?.let {
                        statusFilters.add(it)
                    }
                }

                token.startsWith("is:", ignoreCase = true) -> {
                    val value = token.substringAfter(":").lowercase()
                    when (value) {
                        "success" -> isSuccessFilter = true
                        "error" -> isSuccessFilter = false
                    }
                }

                // Обработка url: отдельно, если пользователь явно написал url:google
                token.startsWith("url:", ignoreCase = true) -> {
                    val value = token.substringAfter(":")
                    if (value.isNotBlank()) textFilters.add(value)
                }

                // Все остальное считаем простым поиском по URL/Host
                else -> {
                    if (token.isNotBlank()) textFilters.add(token)
                }
            }
        }
    }

    override fun matches(value: NetworkRequest): Boolean {
        // 1. Фильтр по методу (строгое совпадение)
        if (methodFilters.isNotEmpty()) {
            if (value.method.uppercase() !in methodFilters) return false
        }

        // 2. Фильтр по статусу (строгое совпадение)
        if (statusFilters.isNotEmpty()) {
            if (value.statusCode !in statusFilters) return false
        }

        // 3. Фильтр is:success / is:error
        if (isSuccessFilter != null) {
            val isSuccessCode = value.statusCode in 200..299

            if (isSuccessFilter == true) {
                // Если ищем успех, то должен быть 2xx
                if (!isSuccessCode) return false
            } else {
                // Если ищем ошибку (is:error):
                // Это либо код >= 400, либо нет кода (ошибка сети), либо поле error не пустое
                val hasErrorCode = (value.statusCode ?: 0) >= 400
                val hasNetworkError = value.error != null

                if (!hasErrorCode && !hasNetworkError) return false
            }
        }

        // 4. Текстовый поиск (URL или Хост содержит ХОТЯ БЫ ОДИН из фильтров)
        if (textFilters.isNotEmpty()) {
            val matchesAny = textFilters.any { filter ->
                value.url.contains(filter, ignoreCase = true)
            }
            if (!matchesAny) return false
        }

        return true
    }
}

@Composable
fun NetworkRequestItem(request: NetworkRequest, onClick: () -> Unit) {
    val isPending = request.statusCode == null && request.error == null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp), // Чуть больше воздуха
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Метод (Badge)
        MethodBadge(request.method)

        Gap(8)

        // 2. Основная инфа (URL) - Weight 1f, чтобы занимать все доступное место
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = request.path,
                color = colorScheme.onSurface,
                style = typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Gap(2)
            Text(
                text = request.host,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Gap(8)

        // 3. Правая часть (Статус и время) - Фиксируется по контенту с align End
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(min = 60.dp) // Минимальная ширина чтобы не скакало
        ) {
            if (isPending) {
                // Аккуратный лоадер
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = colorScheme.primary
                )
            } else {
                // Статус код
                if (request.error != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = request.statusCode.toString(),
                        style = typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = getStatusTextColor(request.statusCategory)
                    )
                }

                Gap(4)

                // Время
                Text(
                    text = formatTime(request.requestTime),
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MethodBadge(method: String) {
    // Пастельные цвета: Мягкий фон, контрастный текст
    val (bgColor, textColor) = when (method.uppercase()) {
        "GET" -> Color(0xFFD9F9FF) to Color(0xFF1565C0)     // Soft Blue
        "POST" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)    // Soft Purple
        "PUT" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)     // Soft Orange
        "DELETE" -> Color(0xFFFFEBEE) to Color(0xFFC62828)  // Soft Red
        "PATCH" -> Color(0xFFE0F2F1) to Color(0xFF00695C)   // Soft Teal
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)      // Grey
    }

    Box(
        modifier = Modifier
            .width(48.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = method.take(4), // Обрезаем длинные методы если что
            style = typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor
        )
    }
}

fun getStatusTextColor(category: StatusCategory): Color {
    return when (category) {
        StatusCategory.Success -> Color(0xFF2E7D32) // Green
        StatusCategory.Redirect -> Color(0xFFF57C00) // Orange/Dark Yellow
        StatusCategory.Error -> Color(0xFFC62828) // Red
        else -> Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkRequestDialog(
    request: NetworkRequest,
) = Surface(
    modifier = Modifier.fillMaxSize(),
    color = colorScheme.surface
) {

    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = request.host,
                        style = typography.titleSmall
                    )
                    // Компактный статус под заголовком
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusText = when (request.statusCode) {
                            null -> "Loading..."
                            else -> "${request.statusCode} ${getStatusText(request.statusCode)}"
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    getStatusTextColor(request.statusCategory),
                                    CircleShape
                                )
                        )
                        Gap(6)
                        Text(
                            text = statusText,
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            actions = {
                // Кнопка Share
                IconButton(onClick = { shareCurl(context, request) }) {
                    Icon(Icons.Default.Share, "Share")
                }
            }
        )

        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.3f))

        // --- Tabs ---
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Overview", "Request", "Response")

        SingleChoiceSegmentedButtonRow(
            space = (-4).dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                SegmentTab(
                    index,
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = title,
                    count = tabs.size,
                    icon = null
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> OverviewTabContent(request)
                1 -> RequestTabContent(request)
                2 -> ResponseTabContent(request)
            }
        }
    }
}


@Composable
fun OverviewTabContent(request: NetworkRequest) {
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Статус "плашкой" как в начале, но аккуратнее (если есть ошибка)
        if (request.error != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = colorScheme.onErrorContainer
                        )
                        Gap(8)
                        Text(
                            request.error!!,
                            color = colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            SectionHeader("General Info")
            // URL блок: Label сверху, Value снизу
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    "URL",
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                SelectionContainer {
                    Text(
                        text = request.url,
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray.copy(0.3f)
            )

            InfoRow("Method", request.method)
            InfoRow(
                "Status",
                "${request.statusCode ?: "Pending"} ${
                    if (request.statusCode != null) getStatusText(request.statusCode!!) else ""
                }"
            )
            InfoRow("Timestamp", formatFullDate(request.requestTime))
        }

        item {
            SectionHeader("Performance")
            // Вертикальный список атрибутов
            InfoRow("Total Duration", "${request.duration} ms")
            InfoRow("Latency (TTFB)", "${request.latency} ms")
            InfoRow("Request Size", formatBytes(request.requestSize))
            InfoRow("Response Size", formatBytes(request.responseSize))
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { openInBrowser(context, request.url) },
                    modifier = Modifier.weight(1f)
                ) { Text("Open Browser") }

                OutlinedButton(
                    onClick = { copyToClipboard(context, request.url) },
                    modifier = Modifier.weight(1f)
                ) { Text("Copy URL") }
            }
        }
    }
}

@Composable
fun RequestTabContent(request: NetworkRequest) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        // Query Params с декодированием
        if (request.queryParams.isNotEmpty()) {
            item {
                CleanExpandableSection(
                    title = "Query Params",
                    count = request.queryParams.size,
                    initExpanded = true,
                    onCopy = {
                        copyToClipboard(
                            context,
                            request.queryParams.joinToString("\n") { "${it.first}: ${it.second}" })
                    }
                ) {
                    KeyValueList(request.queryParams, decodeValues = true)
                }
            }
        }

        item {
            CleanExpandableSection(
                title = "Headers",
                count = request.requestHeaders.size,
                onCopy = {
                    copyToClipboard(
                        context,
                        request.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" })
                }
            ) {
                KeyValueList(request.requestHeaders.toList())
            }
        }

        if (request.requestCookies.isNotEmpty()) {
            item {
                CleanExpandableSection(
                    title = "Cookies",
                    count = request.requestCookies.size,
                    onCopy = {
                        copyToClipboard(
                            context,
                            request.requestCookies.entries.joinToString("\n") { "${it.key}: ${it.value}" })
                    }
                ) {
                    KeyValueList(request.requestCookies.toList())
                }
            }
        }

        item {
            CleanExpandableSection(
                title = "Body",
                initExpanded = true,
                onCopy = if (!request.requestBody.isNullOrEmpty()) {
                    { copyToClipboard(context, request.requestBody) }
                } else null
            ) {
                BodyContent(request.requestBody, request.requestHeaders["Content-Type"])
            }
        }
    }
}

@Composable
fun ResponseTabContent(request: NetworkRequest) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            CleanExpandableSection(
                title = "Headers",
                count = request.responseHeaders.size,
                onCopy = {
                    copyToClipboard(
                        context,
                        request.responseHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" })
                }
            ) {
                KeyValueList(request.responseHeaders.toList())
            }
        }

        if (request.requestCookies.isNotEmpty()) {
            item {
                CleanExpandableSection(
                    title = "Cookies",
                    count = request.responseCookies.size,
                    onCopy = {
                        copyToClipboard(
                            context,
                            request.responseCookies.entries.joinToString("\n") { "${it.key}: ${it.value}" })
                    }
                ) {
                    KeyValueList(request.responseCookies.toList())
                }
            }
        }

        item {
            CleanExpandableSection(
                title = "Body",
                initExpanded = true,
                onCopy = if (!request.responseBody.isNullOrEmpty()) {
                    {
                        copyToClipboard(
                            context,
                            request.responseBody!!
                        )
                    }
                } else null
            ) {
                BodyContent(request.responseBody, request.requestHeaders["Content-Type"])
            }
        }
    }
}

@Composable
fun CleanExpandableSection(
    title: String,
    count: Int? = null,
    initExpanded: Boolean = false,
    onCopy: (() -> Unit)? = null, // Кнопка копирования теперь тут
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initExpanded) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rot")

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая часть: Текст + Счетчик
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = typography.titleSmall,
                    color = colorScheme.onSurface
                )
                if (count != null) {
                    Gap(8)
                    Text(
                        text = "($count)",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (expanded && onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Copy",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Gap(12)
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                content()
                Gap(8)
            }
        }
    }
}

@Composable
fun BodyContent(body: String?, contentType: String?) {
    if (body.isNullOrEmpty()) {
        Text(
            "No content",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.Gray,
            fontSize = 12.sp
        )
        return
    }

    val isJson = contentType?.contains("json") == true
    val displayText = remember(body) { if (isJson) formatJson(body) else body }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = colorScheme.surfaceContainerLow, // Светло-серый/адаптивный фон
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        SelectionContainer {
            Text(
                text = displayText,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
fun KeyValueList(items: List<Pair<String, String>>, decodeValues: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        items.forEach { (key, value) ->
            val displayValue = if (decodeValues) {
                try {
                    java.net.URLDecoder.decode(value, "UTF-8")
                } catch (e: Exception) {
                    value
                }
            } else value

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = key,
                    style = typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(110.dp), // Фиксированная ширина для ключа
                    color = colorScheme.primary
                )
                SelectionContainer {
                    Text(
                        text = displayValue,
                        style = typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f),
                        color = colorScheme.onSurface
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color = colorScheme.primary) {
    Text(
        text = title,
        style = typography.labelLarge,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String, canCopy: Boolean = false) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 10
        )
        if (canCopy) {
            IconButton(onClick = { Pair(context, value) }, modifier = Modifier.size(20.dp)) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Copy",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

fun formatJson(json: String): String {
    return try {
        val jsonElement = Json.parseToJsonElement(json)
        val json = Json { prettyPrint = true }
        json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), jsonElement)
    } catch (e: Exception) {
        json
    }
}

fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

fun formatFullDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Network Data", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}

fun openInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
    }
}

fun shareCurl(context: Context, request: NetworkRequest) {
    val builder = StringBuilder("curl -X ${request.method} '${request.url}'")

    request.requestHeaders.forEach { (key, value) ->
        builder.append(" -H '$key: $value'")
    }

    if (!request.requestBody.isNullOrEmpty()) {
        builder.append(" -d '${request.requestBody.replace("'", "'\\''")}'")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, builder.toString())
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(sendIntent, "Share cURL"))
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    if (digitGroups >= units.size) return "${bytes / 1024 / 1024} MB"

    return String.format(
        Locale.US,
        "%.1f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

fun getStatusText(statusCode: Int): String {
    return when (statusCode) {
        // 1xx Information
        100 -> "Continue"
        101 -> "Switching Protocols"

        // 2xx Success
        200 -> "OK"
        201 -> "Created"
        202 -> "Accepted"
        204 -> "No Content"
        205 -> "Reset Content"
        206 -> "Partial Content"

        // 3xx Redirection
        301 -> "Moved Permanently"
        302 -> "Found"
        303 -> "See Other"
        304 -> "Not Modified"
        307 -> "Temporary Redirect"
        308 -> "Permanent Redirect"

        // 4xx Client Error
        400 -> "Bad Request"
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        405 -> "Method Not Allowed"
        406 -> "Not Acceptable"
        408 -> "Request Timeout"
        409 -> "Conflict"
        410 -> "Gone"
        415 -> "Unsupported Media Type"
        422 -> "Unprocessable Entity"
        429 -> "Too Many Requests"

        // 5xx Server Error
        500 -> "Internal Server Error"
        501 -> "Not Implemented"
        502 -> "Bad Gateway"
        503 -> "Service Unavailable"
        504 -> "Gateway Timeout"

        else -> when (statusCode) {
            in 200..299 -> "Success"
            in 300..399 -> "Redirect"
            in 400..499 -> "Client Error"
            in 500..599 -> "Server Error"
            else -> "Unknown"
        }
    }
}