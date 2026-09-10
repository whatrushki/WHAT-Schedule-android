package app.what.schedule.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.LessonDto
import app.what.schedule.core.models.LessonStateDto
import app.what.schedule.desktop.model.SearchItem

@Composable
fun DesktopScheduleView(
    scheduleDays: List<DayScheduleDto>,
    isLoading: Boolean,
    errorMessage: String?,
    selectedItem: SearchItem?,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxHeight()
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Загрузка расписания...")
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
                selectedItem == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Выберите группу или преподавателя слева", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                scheduleDays.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Расписание отсутствует или выходные дни", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(scheduleDays) { day ->
                            DayScheduleCard(day)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayScheduleCard(day: DayScheduleDto) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "${day.date} (${day.scheduleType})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            day.lessons.forEach { lesson ->
                LessonItemCard(lesson)
            }
        }
    }
}

@Composable
fun LessonItemCard(lesson: LessonDto) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${lesson.number}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lesson.subject.ifBlank { "Пара" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val unit = lesson.otUnits.firstOrNull()
                val details = listOfNotNull(
                    "${lesson.startTime} – ${lesson.endTime}",
                    unit?.teacher?.takeIf { it.isNotBlank() },
                    unit?.room?.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                Text(
                    details,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (lesson.state == LessonStateDto.CHANGED || lesson.state == LessonStateDto.ADDED) {
                Surface(
                    color = Color(0xFFE53935),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "Замена",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
