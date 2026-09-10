package app.what.foundation.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

object DateTimeUtils {
    val RUSSIAN_MONTHS = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )

    val RUSSIAN_MONTHS_NOMINATIVE = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    val RUSSIAN_DAYS_SHORT = listOf(
        "пн", "вт", "ср", "чт", "пт", "сб", "вс"
    )

    val RUSSIAN_DAYS_FULL = listOf(
        "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"
    )

    fun formatDate(date: LocalDate?): String {
        if (date == null) return ""
        val monthStr = RUSSIAN_MONTHS.getOrElse(date.monthNumber - 1) { "" }
        return "${date.dayOfMonth} $monthStr ${date.year}"
    }

    fun formatShortDate(date: LocalDate?): String {
        if (date == null) return ""
        val monthStr = RUSSIAN_MONTHS.getOrElse(date.monthNumber - 1) { "" }
        return "${date.dayOfMonth} $monthStr"
    }

    fun formatTime(time: LocalTime?): String {
        if (time == null) return ""
        val h = time.hour.toString().padStart(2, '0')
        val m = time.minute.toString().padStart(2, '0')
        return "$h:$m"
    }

    fun formatDotDate(date: LocalDate?): String {
        if (date == null) return ""
        val d = date.dayOfMonth.toString().padStart(2, '0')
        val m = date.monthNumber.toString().padStart(2, '0')
        return "$d.$m.${date.year}"
    }

    fun formatDateTime(dateTime: LocalDateTime?): String {
        if (dateTime == null) return ""
        return "${formatDotDate(dateTime.date)} ${formatTime(dateTime.time)}"
    }
}