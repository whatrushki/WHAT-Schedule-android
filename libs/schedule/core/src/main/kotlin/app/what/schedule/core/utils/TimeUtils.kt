package app.what.schedule.core.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

fun parseTime(timeString: String): LocalTime =
    LocalTime.parse(timeString.trim(), DateTimeFormatter.ofPattern("H:mm"))

fun formatTime(time: LocalTime): String =
    time.hour.toString().padStart(2, '0') + ":" +
            time.minute.toString().padStart(2, '0')
