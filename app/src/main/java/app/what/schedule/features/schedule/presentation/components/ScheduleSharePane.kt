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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useStateList
import app.what.foundation.utils.ShareUtils
import app.what.foundation.utils.ShareVariant
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Telegram
import app.what.schedule.ui.theme.icons.filled.VK
import app.what.schedule.ui.theme.icons.filled.Whatsapp
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


val ScheduleExportPane = @Composable { scheduleSearch: ScheduleSearch?,
                                       schedules: List<DaySchedule> ->
    val context = LocalContext.current
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
                            if (it in selectedDays && selectedDays.size > 1)
                                selectedDays.remove(it) else selectedDays.add(it)
                        }
                ) {
                    Text(
                        "${it.date.dayOfMonth}" + "\n"
                                + it.date.dayOfWeek.getDisplayName(
                            if (schedules.size > 2) TextStyle.SHORT_STANDALONE
                            else TextStyle.FULL_STANDALONE,
                            Locale.getDefault()
                        ),
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

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Gap(12)

            listOf(
                Triple(
                    Icons.Default.MoreVert,
                    colorScheme.secondaryContainer,
                    ShareVariant.SystemDefault
                ),
                Triple(WHATIcons.Telegram, Color(0xFF2AABEE), ShareVariant.Telegram),
                Triple(WHATIcons.VK, Color(0xFF2196F3), ShareVariant.VK),
                Triple(WHATIcons.Whatsapp, Color(0xFF4CAF50), ShareVariant.WhatsApp),
            ).forEach {
                ShareButton(
                    icon = it.first,
                    color = it.second,
                    if (it.third == ShareVariant.Telegram) 46 else 34
                ) {
                    ShareUtils.share(
                        context, it.third,
                        createShareTextFromDaySchedules(scheduleSearch, selectedDays)
                    )
                }

                Gap(8)
            }

            Gap(4)
        }

        Gap(16)
    }
}

@Composable
fun ShareButton(
    icon: ImageVector,
    color: Color,
    iconSize: Int = 34,
    onClick: () -> Unit
) = Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .size(68.dp)
        .clip(shapes.medium)
        .background(color)
        .bclick(block = onClick)
) {
    icon.Show(
        Modifier.size(iconSize.dp), Color.White
    )
}

fun createShareTextFromDaySchedules(
    scheduleSearch: ScheduleSearch?,
    schedules: List<DaySchedule>
) = schedules.joinToString(
    "\n---------------------------\n\n"
) {
    val day = it.date.dayOfMonth
    val month =
        it.date.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
    val dayOfWeek =
        it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())


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