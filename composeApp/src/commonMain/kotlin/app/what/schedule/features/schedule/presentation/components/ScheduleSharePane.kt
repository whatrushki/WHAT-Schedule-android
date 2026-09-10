package app.what.schedule.features.schedule.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useStateList
import app.what.foundation.utils.ShareChannel
import app.what.foundation.utils.ShareChannelsRow
import app.what.foundation.utils.ShareData
import app.what.foundation.utils.executeShare
import app.what.domain.models.DaySchedule
import app.what.domain.models.LessonState
import app.what.domain.models.LessonType
import app.what.domain.models.ScheduleSearch
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Telegram
import app.what.schedule.ui.theme.icons.filled.VK
import app.what.schedule.ui.theme.icons.filled.Whatsapp
import app.what.foundation.utils.DateTimeUtils


val ScheduleExportPane = @Composable { scheduleSearch: ScheduleSearch?,
                                       schedules: List<DaySchedule> ->
    val selectedDays = useStateList(schedules.first())
    
    Column(
        Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            "Поделиться расписанием",
            style = typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            color = colorScheme.primary,
            modifier = Modifier.padding(12.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Gap(4)
            
            schedules.forEachIndexed { index, it ->
                val selected = it in selectedDays
                val contentColor = if (selected) colorScheme.onPrimary
                else colorScheme.onSecondaryContainer
                
                Box(
                    Modifier
                        .clip(shapes.medium)
                        .background(
                            if (selected) colorScheme.primary
                            else colorScheme.secondaryContainer
                        )
                        .bclick {
                            if (it in selectedDays) {
                                if (selectedDays.size > 1) selectedDays.remove(it)
                            } else selectedDays.add(it)
                        }
                ) {
                    val dayOfWeekStr = if (schedules.size > 2) {
                        DateTimeUtils.RUSSIAN_DAYS_SHORT.getOrElse(it.date.dayOfWeek.ordinal) { "" }
                    } else {
                        DateTimeUtils.RUSSIAN_DAYS_FULL.getOrElse(it.date.dayOfWeek.ordinal) { "" }
                    }
                    Text(
                        "${it.date.dayOfMonth}\n$dayOfWeekStr",
                        modifier = Modifier.padding(16.dp, 8.dp),
                        color = contentColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Gap(4)
        }
        
        Gap(12)
        
        ShareChannelsRow(
            channels = ShareChannel.defaultChannels,
            onChannelClick = { channel ->
                val text = createShareTextFromDaySchedules(
                    scheduleSearch,
                    selectedDays.sortedBy { it.date }
                )
                executeShare(null, channel, ShareData.Text(text, "Расписание"))
            }
        )
        
        Gap(16)
    }
}

fun createShareTextFromDaySchedules(
    scheduleSearch: ScheduleSearch?,
    schedules: List<DaySchedule>
) = schedules.joinToString(
    "\n---------------------------\n\n"
) {
    val day = it.date.dayOfMonth
    val month = DateTimeUtils.RUSSIAN_MONTHS.getOrElse(it.date.monthNumber - 1) { "" }
    val dayOfWeek = DateTimeUtils.RUSSIAN_DAYS_SHORT.getOrElse(it.date.dayOfWeek.ordinal) { "" }
    
    
    "**$day $month ($dayOfWeek)**\n\n" +
            it.lessons.joinToString("\n") {
                when (it.type) { //🌱🍂🪻🌼🌻☘️🌳🌴🌾🍁🍃
                    LessonType.COMMON, LessonType.PRACTISE, LessonType.LECTURE -> "🌱"
                    LessonType.ADDITIONAL -> "🌾"
                    LessonType.CLASS_HOUR -> "🍁"
                    LessonType.LABORATORY -> "🪻"
                    LessonType.CREDIT -> "🍂"
                    LessonType.OBLIGATION -> "💸"
                } + " **${it.number}** пара __${it.startTime}–${it.endTime} " + when (it.state) {
                    LessonState.COMMON -> ""
                    LessonState.ADDED -> "(⚡)"
                    LessonState.REMOVED -> "(🛟)"
                    LessonState.CHANGED -> "(♻️)"
                } + "__\n" +
                        "**${it.subject.takeIf { it.isNotBlank() } ?: "Не заполнено"}**\n" +
                        if (it.otUnits.size == 1) {
                            "${if (scheduleSearch is ScheduleSearch.Teacher) it.otUnits.first().group.name else it.otUnits.first().teacher.name}\n" +
                                    "Ауд. ${it.otUnits.first().auditory} (корп.${it.otUnits.first().building})\n"
                        } else it.otUnits.joinToString("\n") {
                            "— ${if (scheduleSearch is ScheduleSearch.Teacher) it.group.name else it.teacher.name} — Ауд. ${it.auditory}/${it.building}"
                        } + "\n"
            }
}