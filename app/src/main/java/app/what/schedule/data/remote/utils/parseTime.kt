package app.what.schedule.data.remote.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun parseTime(timeString: String): LocalTime = LocalTime.parse(
    timeString.trim(),
    DateTimeFormatter.ofPattern("H:mm")
)
