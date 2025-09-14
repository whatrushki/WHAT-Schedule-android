package app.what.foundation.services

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import app.what.foundation.core.Monitor.Companion.monitored
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        @Volatile
        private var instance: AppLogger? = null

        fun initialize(context: Context) {
            instance = AppLogger(context.applicationContext)
        }

        val Auditor
            get() = instance ?: throw IllegalStateException("FileLogger not initialized. Call initialize() first.")
    }

    @Volatile
    var autoIncrementedId = 0L
        get() = field++

    val logFile: File by lazy { File(context.filesDir, "audit_logs.txt") }

    var isLoggingPaused by monitored(false)
        private set

    fun setIsLoggingPaused(value: Boolean) {
        isLoggingPaused = value
    }

    private val logFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getLogsFlow(): StateFlow<List<LogEntry>> = logFlow.asStateFlow()

    @Composable
    fun collectLogs() = getLogsFlow().collectAsState()

    fun debug(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun info(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun warn(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.WARNING, tag, message, throwable)
    fun err(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.ERROR, tag, message, throwable)
    fun critic(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.CRITICAL, tag, message, throwable)

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val entry = LogEntry(id = autoIncrementedId, level = level, tag = tag, message = message, throwable = throwable)

        // Уведомляем если не остановлено
        if (!isLoggingPaused) {
            val currentList = logFlow.value.toMutableList()
            currentList.add(entry)
            logFlow.value = currentList
        }

        // Пишем в консоль
        Log.println(
            when (entry.level) {
                LogLevel.DEBUG -> Log.DEBUG
                LogLevel.INFO -> Log.INFO
                LogLevel.WARNING -> Log.WARN
                LogLevel.ERROR -> Log.ERROR
                LogLevel.CRITICAL -> Log.ERROR
            },
            entry.tag,
            entry.toFormattedString()
        )

        // Сохраняем в файл
        scope.launch { writeToFile(entry) }
    }

    private suspend fun writeToFile(entry: LogEntry) = withContext(Dispatchers.IO) {
        try {
            val formattedLog = entry.toFormattedString() + "\n"
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            logFile.appendText(formattedLog)
        } catch (e: Exception) {
            println("Failed to write log to file: ${e.message}")
        }
    }

    private fun parseLogEntry(block: String): LogEntry? {
        if (block.isBlank()) return null

        val lines = block.trim().split("\n")
        if (lines.isEmpty()) return null

        // Паттерн для первой строки: [🐛] [2024-01-15 14:30:25.123] [MainActivity] Сообщение
        val pattern = """\[(.)\] \[(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\] \[(.*)\] (.*)""".toRegex()

        return pattern.find(lines[0])?.let { matchResult ->
            val (emoji, timestampStr, tag, message) = matchResult.destructured
            val level = LogLevel.entries.find { it.emoji == emoji } ?: LogLevel.INFO

            // Обрабатываем stacktrace если есть
            val throwable = if (lines.size > 1) {
                val stackTrace = lines.subList(1, lines.size).joinToString("\n")
                Exception(stackTrace)
            } else {
                null
            }

            LogEntry(
                id = autoIncrementedId,
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                    .parse(timestampStr)?.time ?: System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = message,
                throwable = throwable
            )
        }
    }

    // Обновим функцию чтения логов
    suspend fun readAllLogsFromFile(): List<LogEntry> = withContext(Dispatchers.IO) {
        if (!logFile.exists()) return@withContext emptyList()

        try {
            logFile.readText()
                .split("\n")
                .mapNotNull { block -> parseLogEntry(block) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearLogs() {
        scope.launch {
            withContext(Dispatchers.IO) {
                if (logFile.exists()) {
                    logFile.delete()
                }
            }
            logFlow.value = emptyList()
        }
    }
}