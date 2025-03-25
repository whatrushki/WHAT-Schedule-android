package app.what.schedule.features.schedule.presentation.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.schedule.data.remote.api.LessonsScheduleType
import app.what.schedule.data.remote.api.ScheduleSearch

@Composable
fun SearchButton(
    search: ScheduleSearch?,
    scheduleType: LessonsScheduleType?,
    onClick: () -> Unit
) = Box(
    Modifier
        .padding(horizontal = 12.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current,
            onClick = onClick
        )
) {
    Row(
        Modifier
            .padding(16.dp, 14.dp)
            .fillMaxWidth()
    ) {
        Icon(
            Icons.Default.Search,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            contentDescription = "search"
        )

        Gap(8)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when (search) {
                    is ScheduleSearch.Group -> "Группа: " + search.name
                    is ScheduleSearch.Teacher -> "Препод.: " + search.name
                    null -> "Поиск..."
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.secondary
            )


            if (scheduleType != null) Text(
                when (scheduleType) {
                    LessonsScheduleType.COMMON -> "обыч."
                    LessonsScheduleType.SHORTENED -> "сокр."
                    LessonsScheduleType.WITH_CLASS_HOUR -> "кл.ч."
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Preview
@Composable
fun SearchButtonPreview() = Column {
    SearchButton(
        ScheduleSearch.Group("ИКТ-20"),
        LessonsScheduleType.WITH_CLASS_HOUR,
        onClick = {}
    )

    Gap(8)

    SearchButton(
        ScheduleSearch.Teacher("Иванов И. И."),
        LessonsScheduleType.COMMON,
        onClick = {}
    )

    Gap(8)

    SearchButton(
        ScheduleSearch.Group("ИКТ-20"),
        LessonsScheduleType.SHORTENED,
        onClick = {}
    )
}