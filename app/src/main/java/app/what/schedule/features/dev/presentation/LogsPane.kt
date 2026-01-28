package app.what.schedule.features.dev.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.services.LogEntry
import app.what.foundation.services.LogLevel
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useState
import app.what.schedule.features.dev.presentation.components.Filter
import app.what.schedule.features.dev.presentation.components.FilteredList
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Warn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LogFilter : Filter<LogEntry> {
    private val levelFilters = mutableListOf<LogLevel>()
    private val tagFilters = mutableListOf<String>()
    private val textFilters = mutableListOf<String>()

    // true = показывать только логи с Throwable
    private var hasErrorsOnly = false

    // "today", "hour", "5min"
    private var timeFilter: String? = null

    override fun clearFilters() {
        levelFilters.clear()
        tagFilters.clear()
        textFilters.clear()
        hasErrorsOnly = false
        timeFilter = null
    }

    override fun parseQuery(query: String) {
        clearFilters()
        if (query.isBlank()) return

        // Разбиваем строку запроса по пробелам
        val tokens = query.trim().split("\\s+".toRegex())

        tokens.forEach { token ->
            // Убираем кавычки, если пользователь их ввел по привычке ("text" -> text)
            val cleanToken = token.removeSurrounding("\"").removeSurrounding("'")

            when {
                // Фильтр по уровню: level:info или level:error
                cleanToken.startsWith("level:", ignoreCase = true) -> {
                    val levelName = cleanToken.substringAfter(":").uppercase()
                    // Ищем такой уровень в enum LogLevel
                    LogLevel.entries.find { it.name == levelName }?.let {
                        levelFilters.add(it)
                    }
                }

                // Фильтр по тегу: tag:Network
                cleanToken.startsWith("tag:", ignoreCase = true) -> {
                    val tagValue = cleanToken.substringAfter(":")
                    if (tagValue.isNotBlank()) tagFilters.add(tagValue)
                }

                // Спец. фильтры: is:error (наличие throwable)
                cleanToken.startsWith("is:", ignoreCase = true) -> {
                    val value = cleanToken.substringAfter(":").lowercase()
                    if (value == "error" || value == "crash") {
                        hasErrorsOnly = true
                    }
                }

                // Фильтр по времени: time:today
                cleanToken.startsWith("time:", ignoreCase = true) -> {
                    timeFilter = cleanToken.substringAfter(":").lowercase()
                }

                // Всё остальное — текстовый поиск по сообщению и тегу
                else -> {
                    if (cleanToken.isNotBlank()) {
                        textFilters.add(cleanToken)
                    }
                }
            }
        }
    }

    override fun matches(value: LogEntry): Boolean {
        // 1. Уровень лога (точное совпадение хотя бы с одним выбранным)
        if (levelFilters.isNotEmpty()) {
            if (value.level !in levelFilters) return false
        }

        // 2. Тег (частичное совпадение)
        if (tagFilters.isNotEmpty()) {
            // Если ни один из фильтров тега не содержится в теге лога -> false
            val matchesTag = tagFilters.any { filterTag ->
                value.tag.contains(filterTag, ignoreCase = true)
            }
            if (!matchesTag) return false
        }

        // 3. Наличие ошибки (Throwable)
        if (hasErrorsOnly && value.throwable == null) {
            return false
        }

        // 4. Фильтр по времени
        if (timeFilter != null) {
            val now = System.currentTimeMillis()
            val logTime = value.timestamp

            val matchesTime = when (timeFilter) {
                "today" -> {
                    val startOfDay = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    logTime >= startOfDay
                }

                "hour" -> (now - logTime) <= 3600000 // 1 час
                "5min" -> (now - logTime) <= 300000  // 5 минут
                else -> true
            }
            if (!matchesTime) return false
        }

        // 5. Текстовый поиск (Ищет в message ИЛИ в tag)
        // Логика: Если введены слова, лог должен содержать ХОТЯ БЫ ОДНО из них
        if (textFilters.isNotEmpty()) {
            val matchesText = textFilters.any { filterText ->
                value.message.contains(filterText, ignoreCase = true) ||
                        value.tag.contains(filterText, ignoreCase = true)
            }
            if (!matchesText) return false
        }

        return true
    }
}

@Composable
fun LogsPane(
    modifier: Modifier = Modifier,
) {
    val logs by Auditor.collectLogs()

    FilteredList(
        title = "Логи приложения",
        values = logs,
        vKey = { it.id },
        vContent = { LogItem(it) },
        exportValues = {},
        clearValues = Auditor::clearLogs,
        setIsMonitoringPaused = Auditor::setIsLoggingPaused,
        isMonitoringPaused = Auditor.isLoggingPaused,
        modifier = modifier,
        filter = LogFilter(),
        filterHelpItems = listOf(
            "level:error" to "Фильтр по уровню (debug, info, warning, error)",
            "tag:Main" to "Поиск по тегу (содержит текст)",
            "is:error" to "Только логи с исключениями (Exception/Throwable)",
            "time:5min" to "Логи за последние 5 минут",
            "time:hour" to "Логи за последний час",
            "time:today" to "Логи с начала дня",
            "login failed" to "Простой поиск текста в сообщении"
        )
    )
}


@Composable
fun LogItem(logEntry: LogEntry) {
    val (expanded, setExpanded) = useState(false)
    val isDarkTheme = isSystemInDarkTheme()

    val (backgroundColor, textColor, borderColor) = remember(logEntry.level, isDarkTheme) {
        when (logEntry.level) {
            LogLevel.DEBUG -> if (isDarkTheme) Triple(
                Color(0xFF1B3B1B).copy(alpha = 0.6f),  // Темный зеленый
                Color(0xFF81C784),                     // Светло-зеленый
                Color(0xFF2E7D32).copy(alpha = 0.4f)   // Темно-зеленый
            ) else Triple(
                Color(0xFFE8F5E8).copy(alpha = 0.8f),  // Светлый зеленый
                Color(0xFF2E7D32),                     // Темно-зеленый
                Color(0xFFA5D6A7).copy(alpha = 0.5f)   // Пастельно-зеленый
            )

            LogLevel.INFO -> if (isDarkTheme) Triple(
                Color(0xFF1A237E).copy(alpha = 0.4f),  // Темный синий
                Color(0xFF90CAF9),                     // Светло-синий
                Color(0xFF1976D2).copy(alpha = 0.4f)   // Темно-синий
            ) else Triple(
                Color(0xFFE3F2FD).copy(alpha = 0.8f),  // Светлый голубой
                Color(0xFF1976D2),                     // Темно-синий
                Color(0xFF90CAF9).copy(alpha = 0.5f)   // Пастельно-голубой
            )

            LogLevel.WARNING -> if (isDarkTheme) Triple(
                Color(0xFF4E342E).copy(alpha = 0.4f),  // Темный оранжевый
                Color(0xFFFFB74D),                     // Светло-оранжевый
                Color(0xFFF57C00).copy(alpha = 0.4f)   // Темно-оранжевый
            ) else Triple(
                Color(0xFFFFF3E0).copy(alpha = 0.8f),  // Светлый оранжевый
                Color(0xFFF57C00),                     // Темно-оранжевый
                Color(0xFFFFCC80).copy(alpha = 0.5f)   // Пастельно-оранжевый
            )

            LogLevel.ERROR -> if (isDarkTheme) Triple(
                Color(0xFF4A1F1F).copy(alpha = 0.4f),  // Темный красный
                Color(0xFFEF9A9A),                     // Светло-красный
                Color(0xFFD32F2F).copy(alpha = 0.4f)   // Темно-красный
            ) else Triple(
                Color(0xFFFFEBEE).copy(alpha = 0.8f),  // Светлый розовый
                Color(0xFFD32F2F),                     // Темно-красный
                Color(0xFFFFCDD2).copy(alpha = 0.5f)   // Пастельно-розовый
            )

            LogLevel.CRITICAL -> if (isDarkTheme) Triple(
                Color(0xFF4A235A).copy(alpha = 0.4f),  // Темный фиолетовый
                Color(0xFFCE93D8),                     // Светло-фиолетовый
                Color(0xFF7B1FA2).copy(alpha = 0.4f)   // Темно-фиолетовый
            ) else Triple(
                Color(0xFFF3E5F5).copy(alpha = 0.8f),  // Светлый фиолетовый
                Color(0xFF7B1FA2),                     // Темно-фиолетовый
                Color(0xFFE1BEE7).copy(alpha = 0.5f)   // Пастельно-фиолетовый
            )
        }
    }


    Column(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(shapes.small)
            .bclick { setExpanded(!expanded) }
            .background(backgroundColor.copy(alpha = .8f), shapes.small)
            .border(1.dp, borderColor.copy(alpha = .6f), shapes.small)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Эмодзи уровня лога
            Text(
                text = "[ ${logEntry.level.emoji} ]",
                color = textColor,
                fontSize = 14.sp
            )

            Gap(8)

            // Время
            Text(
                text = "[${
                    SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                        .format(Date(logEntry.timestamp))
                }]",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
            )

            Gap(8)

            // Тег
            Text(
                text = logEntry.tag,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Сообщение
        Text(
            text = logEntry.message,
            color = textColor,
            fontSize = 12.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )

        if (logEntry.throwable != null) {
            Icon(
                imageVector = WHATIcons.Warn,
                contentDescription = "Contains error",
                tint = colorScheme.error,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}