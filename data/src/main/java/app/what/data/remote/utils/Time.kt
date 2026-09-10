package app.what.schedule.data.remote.utils

import kotlinx.datetime.LocalTime

fun parseMonth(month: String) = when (month.lowercase()) {
    "января" -> 1
    "февраля" -> 2
    "марта" -> 3
    "апреля" -> 4
    "мая" -> 5
    "июня" -> 6
    "июля" -> 7
    "августа" -> 8
    "сентября" -> 9
    "октября" -> 10
    "ноября" -> 11
    "декабря" -> 12
    else -> 1
}

fun parseTime(timeString: String): LocalTime {
    val parts = timeString.trim().split(":")
    return LocalTime(parts[0].toInt(), parts[1].toInt())
}

fun formatTime(time: LocalTime): String =
    time.hour.toString().padStart(2, '0') + ":" +
            time.minute.toString().padStart(2, '0')