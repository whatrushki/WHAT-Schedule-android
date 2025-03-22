package app.what.schedule.features.schedule.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.what.foundation.core.Listener
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.useState
import app.what.navigation.core.rememberSheetController
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.Teacher
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.presentation.theme.icons.WHATIcons
import app.what.schedule.presentation.theme.icons.filled.Group
import app.what.schedule.presentation.theme.icons.filled.Person
import app.what.schedule.ui.components.SearchBox


val SearchSheet = @Composable { state: ScheduleState, listener: Listener<ScheduleEvent> ->
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        val sheetController = rememberSheetController()
        val (query, setQuery) = useState("")

        val teachers = remember(
            query,
            state.teachers.size
        ) { state.teachers.filter { query.lowercase() in it.name.lowercase() } }

        val groups = remember(
            query,
            state.groups.size
        ) { state.groups.filter { query.lowercase() in it.id.lowercase() } }

        SearchBox(query, setQuery, Modifier.padding(horizontal = 12.dp))

        Gap(12)

        val (selectedTab, setSelectedTab) = useState(0)
        val list = remember(
            selectedTab,
            groups.size,
            teachers.size
        ) { if (selectedTab == 0) groups else teachers }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            SegmentTab(
                0,
                2,
                selectedTab == 0,
                WHATIcons.Group,
                "Группы",
                onClick = { setSelectedTab(0) }
            )

            SegmentTab(
                1,
                2,
                selectedTab == 1,
                WHATIcons.Person,
                "Преподаватели",
                onClick = { setSelectedTab(1) }
            )
        }


        Gap(12)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            items(
                list.size,
                key = {
                    when (val item = list[it]) {
                        is Group -> item.id
                        is Teacher -> item.id
                        else -> ""
                    }
                }
            ) { index ->
                val item = list[index]

                LessonTeacherChip(
                    name = when (item) {
                        is Group -> item.name
                        is Teacher -> item.name
                        else -> ""
                    },
                    selected = state.search?.query == when (item) {
                        is Group -> item.id
                        is Teacher -> item.id
                        else -> ""
                    },
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 8.dp)
                ) {
                    when (item) {
                        is Group -> item.id
                        is Teacher -> item.id
                        else -> null
                    }?.let {
                        listener(
                            ScheduleEvent.OnSearchCompleted(
                                if (selectedTab == 0) ScheduleSearch.Group(it)
                                else ScheduleSearch.Teacher(it)
                            )
                        )
                    }

                    sheetController.animateClose()
                }
            }
        }
    }
}

@Composable
private fun LessonTeacherChip(
    name: String,
    selected: Boolean,
    modifier: Modifier,
    onCLick: () -> Unit
) = FilterChip(
    selected = selected,
    onClick = onCLick,
    modifier = modifier,
    label = {
        Text(
            text = name,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
)