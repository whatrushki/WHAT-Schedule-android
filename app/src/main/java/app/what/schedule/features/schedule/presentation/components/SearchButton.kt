package app.what.schedule.features.schedule.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Group
import app.what.schedule.ui.theme.icons.filled.Person

@Composable
fun SearchButton(
    search: ScheduleSearch?,
    scheduleType: LessonsScheduleType?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = Box(
    modifier
        .clip(CircleShape)
        .background(colorScheme.surfaceContainer)
        .bclick(block = onClick)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp, 14.dp)
            .fillMaxWidth()
    ) {
        Icon(
            when (search) {
                is ScheduleSearch.Group -> WHATIcons.Group
                is ScheduleSearch.Teacher -> WHATIcons.Person
                null -> Icons.Default.Search
            },
            modifier = Modifier.size(20.dp),
            tint = colorScheme.onPrimaryContainer,
            contentDescription = "search"
        )

        Gap(8)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = search?.name ?: "Поиск...",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )


            if (scheduleType != null) Text(
                when (scheduleType) {
                    LessonsScheduleType.COMMON -> "обыч."
                    LessonsScheduleType.SHORTENED -> "сокр."
                    LessonsScheduleType.WITH_CLASS_HOUR -> "кл.ч."
                },
                color = colorScheme.primary,
                style = typography.bodyMedium.copy(
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
        null,
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