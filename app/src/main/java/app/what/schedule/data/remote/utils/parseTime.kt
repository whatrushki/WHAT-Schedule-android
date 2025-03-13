package app.what.schedule.data.remote.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun parseTime(timeString: String): LocalTime = LocalTime.parse(
    timeString.trim(),
    DateTimeFormatter.ofPattern("H:mm")
)

fun formatTime(time: LocalTime): String =
    time.hour.toString().padStart(2, '0') + ":" + time.minute.toString().padStart(2, '0')