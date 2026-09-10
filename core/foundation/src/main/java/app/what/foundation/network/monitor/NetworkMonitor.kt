package app.what.foundation.network.monitor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.util.UUID

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
        return Json.encodeToString(requests.map {
            mapOf(
                "url" to it.url,
                "method" to it.method,
                "status" to it.statusCode.toString(),
                "duration" to "${it.duration}ms",
                "requestTime" to it.requestTime.toString()
            )
        })
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
                val isImage = it.responseHeaders["Content-Type"]?.contains("image")
                    ?: it.responseHeaders["content-type"]?.contains("image")
                    ?: false

                val (text, size) = if (!isImage) {
                    val body = response.bodyAsText()
                    try {
                        json.encodeToString(json.decodeFromString<JsonElement>(body))
                    } catch (_: Exception) {
                        body
                    }.let { b -> b to b.length.toLong() }
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
