package app.what.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.what.domain.models.LessonsScheduleType
import app.what.domain.models.ScheduleSearch
import app.what.ui.icons.WHATIcons

@Composable
fun SearchCapsule(
    search: ScheduleSearch?,
    scheduleType: LessonsScheduleType?,
    modifier: Modifier = Modifier,
    placeholder: String = "Поиск группы или преподавателя...",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = when (search) {
                    is ScheduleSearch.Group -> WHATIcons.Group
                    is ScheduleSearch.Teacher -> WHATIcons.Person
                    null -> Icons.Default.Search
                },
                modifier = Modifier.size(20.dp),
                tint = colorScheme.onPrimaryContainer,
                contentDescription = "Search"
            )

            Spacer(modifier = Modifier.width(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = search?.name ?: placeholder,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (search != null) colorScheme.onSurface else colorScheme.outline,
                    style = typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                if (scheduleType != null) {
                    Text(
                        text = when (scheduleType) {
                            LessonsScheduleType.COMMON -> "обыч."
                            LessonsScheduleType.SHORTENED -> "сокр."
                            LessonsScheduleType.WITH_CLASS_HOUR -> "кл.ч."
                        },
                        color = colorScheme.primary,
                        style = typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
