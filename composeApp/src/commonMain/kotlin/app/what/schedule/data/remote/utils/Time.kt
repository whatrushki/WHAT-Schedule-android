package app.what.schedule.data.remote.utils

import app.what.foundation.utils.DateTimeUtils
import kotlinx.datetime.LocalTime

fun formatTime(time: LocalTime): String = DateTimeUtils.formatTime(time)
