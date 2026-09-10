package app.what.schedule.wasm.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import app.what.schedule.wasm.model.WebSearchItem

@Composable
fun WebScheduleView(
    scheduleDays: List<DayScheduleDto>,
    isLoading: Boolean,
    errorMessage: String?,
    selectedItem: WebSearchItem?,
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
                selectedItem == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Выберите группу или преподавателя слева", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                scheduleDays.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Расписание отсутствует или выходные дни", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(scheduleDays) { day ->
                            WebDayScheduleCard(day)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebDayScheduleCard(day: DayScheduleDto) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "${day.date} (${day.date.dayOfWeek.name})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            for (lesson in day.lessons) {
                WebLessonItemCard(lesson)
            }
        }
    }
}

@Composable
fun WebLessonItemCard(lesson: LessonDto) {
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
            val unit = lesson.otUnits.firstOrNull()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lesson.subject.ifBlank { "Пара" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
