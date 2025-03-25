package app.what.schedule.data.remote.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun parseMonth(month: String) = when (month) {
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

fun parseTime(timeString: String): LocalTime =
    LocalTime.parse(timeString.trim(), DateTimeFormatter.ofPattern("H:mm"))

fun formatTime(time: LocalTime): String =
    time.hour.toString().padStart(2, '0') + ":" +
            time.minute.toString().padStart(2, '0')