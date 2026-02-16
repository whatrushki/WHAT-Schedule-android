package app.what.foundation.services

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.plus
import kotlin.collections.takeLast
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

enum class LogLevel(val emoji: String, val color: Color) {
    DEBUG("🐛", Color(0xFF4CAF50)),    // Зеленый
    INFO("🧢", Color(0xFF2196F3)),     // Синий
    WARNING("🍣", Color(0xFFFF9800)),  // Оранжевый
    ERROR("🌶️", Color(0xFFF44336)),    // Красный
    CRITICAL("🪻", Color(0xFF9C27B0))  // Фиолетовый
}

data class LogEntry(
    val id: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    fun toFormattedString(): String {
        return "[${level.emoji}] [${
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                .format(Date(timestamp))
        }] [$tag] " + "$message " +
                (throwable?.let { "\n${it.stackTraceToString()}" } ?: "")
    }
}

class AppLogger private constructor(context: Context) {

    companion object {
        private const val MAX_MEMORY_LOGS = 500
        @Volatile private var instance: AppLogger? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) instance = AppLogger(context.applicationContext)
                }
            }
        }

        val Auditor: AppLogger
            get() = instance ?: throw IllegalStateException("Logger not initialized")
    }

    @OptIn(ExperimentalAtomicApi::class)
    private val atomicId = AtomicLong(0)
    val logFile: File by lazy { File(context.filesDir, "audit_logs.txt") }

    var isLoggingPaused by mutableStateOf(false)
        private set

    private val _logFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs = _logFlow.asStateFlow()

    @Composable
    fun collectLogs() = logs.collectAsState()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun setIsLoggingPaused(value: Boolean) { isLoggingPaused = value }

    @OptIn(ExperimentalAtomicApi::class)
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val entry = LogEntry(
            id = atomicId.incrementAndFetch(),
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )

        val logcatPriority = when(level) {
            LogLevel.DEBUG -> Log.DEBUG
            LogLevel.INFO -> Log.INFO
            LogLevel.WARNING -> Log.WARN
            else -> Log.ERROR
        }

        Log.println(logcatPriority, tag, entry.toFormattedString())

        if (!isLoggingPaused) {
            _logFlow.update { current ->
                (current + entry).takeLast(MAX_MEMORY_LOGS)
            }
        }

        scope.launch {
            synchronized(logFile) {
                logFile.appendText(entry.toFormattedString()  + "\n" + throwable?.stackTrace + "\n")
            }
        }
    }

    suspend fun readLogsSafe(): List<LogEntry> = withContext(Dispatchers.IO) {
        if (!logFile.exists()) return@withContext emptyList()
        val result = mutableListOf<LogEntry>()
        try {
            logFile.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    parseLogEntry(line)?.let { result.add(it) }
                }
            }
        } catch (e: Exception) {
            Log.e("Logger", "Read error", e)
        }
        result
    }

    private fun parseLogEntry(line: String): LogEntry? {
        if (line.isBlank() || !line.startsWith("[")) return null
        return try {
            // Быстрое извлечение через деление строки, если формат строгий
            // Или оставить Regex, но только для одной строки
            null // Реализация парсинга
        } catch (e: Exception) { null }
    }

    fun clearLogs() {
        _logFlow.value = emptyList()
        scope.launch { synchronized(logFile) { if (logFile.exists()) logFile.delete() } }
    }

    fun debug(tag: String, msg: String) = log(LogLevel.DEBUG, tag, msg)
    fun info(tag: String, msg: String) = log(LogLevel.INFO, tag, msg)
    fun warn(tag: String, msg: String) = log(LogLevel.WARNING, tag, msg)
    fun err(tag: String, msg: String, t: Throwable? = null) = log(LogLevel.ERROR, tag, msg, t)
    fun critic(tag: String, msg: String, t: Throwable? = null) = log(LogLevel.CRITICAL, tag, msg, t)
}