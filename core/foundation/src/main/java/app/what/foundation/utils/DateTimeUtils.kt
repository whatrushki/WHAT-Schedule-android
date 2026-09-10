package app.what.foundation.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    val FULL_DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

    val SHORT_DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())

    val TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    val DOT_DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy")

    val DATE_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun formatDate(date: LocalDate?): String =
        date?.format(FULL_DATE_FORMATTER) ?: ""

    fun formatShortDate(date: LocalDate?): String =
        date?.format(SHORT_DATE_FORMATTER) ?: ""

    fun formatTime(time: LocalTime?): String =
        time?.format(TIME_FORMATTER) ?: ""

    fun formatDateTime(dateTime: LocalDateTime?): String =
        dateTime?.format(DATE_TIME_FORMATTER) ?: ""
}