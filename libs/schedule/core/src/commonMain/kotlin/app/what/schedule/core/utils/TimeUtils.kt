package app.what.schedule.core.utils

import kotlinx.datetime.LocalTime

fun parseMonth(month: String): Int = when (month.lowercase()) {
    "января", "январь", "янв" -> 1
    "февраля", "февраль", "фев" -> 2
    "марта", "март", "мар" -> 3
    "апреля", "апрель", "апр" -> 4
    "мая", "май" -> 5
    "июня", "июнь", "июн" -> 6
    "июля", "июль", "июл" -> 7
    "августа", "август", "авг" -> 8
    "сентября", "сентябрь", "сен" -> 9
    "октября", "октябрь", "окт" -> 10
    "ноября", "ноябрь", "ноя" -> 11
    "декабря", "декабрь", "дек" -> 12
    else -> 1
}

fun parseTime(timeString: String): LocalTime {
    val trimmed = timeString.trim()
    val parts = trimmed.split(":")
    if (parts.size >= 2) {
        val hour = parts[0].trim().toIntOrNull() ?: 0
        val minute = parts[1].trim().toIntOrNull() ?: 0
        val second = if (parts.size >= 3) parts[2].trim().toIntOrNull() ?: 0 else 0
        return LocalTime(hour, minute, second)
    }
    return try {
        LocalTime.parse(trimmed)
    } catch (_: Exception) {
        LocalTime(0, 0)
    }
}

fun formatTime(time: LocalTime): String =
    time.hour.toString().padStart(2, '0') + ":" +
            time.minute.toString().padStart(2, '0')
