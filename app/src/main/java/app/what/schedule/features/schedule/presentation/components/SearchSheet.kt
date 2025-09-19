package app.what.schedule.features.schedule.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.core.Listener
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.capplyIf
import app.what.foundation.ui.controllers.SheetController
import app.what.foundation.ui.controllers.rememberSheetController
import app.what.foundation.ui.useState
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.Teacher
import app.what.schedule.data.remote.api.toScheduleSearch
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.ui.components.AnimatedIconTitle
import app.what.schedule.ui.components.SearchBox
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Crown
import app.what.schedule.ui.theme.icons.filled.Group
import app.what.schedule.ui.theme.icons.filled.Person


val SearchSheet = @Composable { state: ScheduleState, listener: Listener<ScheduleEvent> ->
    val sheetController = rememberSheetController()
    val (query, setQuery) = useState("")

    val teachers = remember(
        query,
        state.teachers
    ) { state.teachers.filter { query.lowercase() in it.name.lowercase() } }

    val groups = remember(
        query,
        state.groups
    ) { state.groups.filter { query.lowercase() in it.name.lowercase() } }

    val (selectedTab, setSelectedTab) = useState(0)

    val favoriteList = remember(state.groups) {
        groups.filter { it.favorite } +
                teachers.filter { it.favorite }
    }

    val list = remember(
        selectedTab,
        groups.size,
        teachers.size
    ) { if (selectedTab == 0) groups else teachers }


    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                SearchBox(
                    query = query,
                    setQuery = setQuery,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )

                Gap(8)

                AnimatedIconTitle(
                    WHATIcons.Crown,
                    "Избранное",
                    Modifier.padding(12.dp, 8.dp)
                )
            }
        }

        if (favoriteList.isEmpty()) item(span = { GridItemSpan(2) }) {
            Text(
                "У вас пока нет избранных. Зажмите чтобы добавить",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )
        } else searchBlocks(
            favoriteList,
            true,
            state.search?.name,
            sheetController,
            listener
        )

        item(span = { GridItemSpan(2) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                AnimatedIconTitle(
                    if (selectedTab == 0) WHATIcons.Group else WHATIcons.Person,
                    if (selectedTab == 0) "Группы" else "Преподаватели"
                )

                SingleChoiceSegmentedButtonRow {
                    SegmentTab(
                        index = 0,
                        count = 2,
                        selected = selectedTab == 0,
                        icon = WHATIcons.Group,
                        label = null,
                        onClick = { setSelectedTab(0) }
                    )

                    Gap(8)

                    SegmentTab(
                        index = 1,
                        count = 2,
                        selected = selectedTab == 1,
                        icon = WHATIcons.Person,
                        label = null,
                        onClick = { setSelectedTab(1) }
                    )
                }
            }
        }


        searchBlocks(
            list,
            false,
            state.search?.name,
            sheetController,
            listener
        )
    }
}

fun LazyGridScope.searchBlocks(
    list: List<Any>,
    favorite: Boolean,
    state: String?,
    sheetController: SheetController,
    listener: Listener<ScheduleEvent>
) {
    items(
        list, key = {
            when (it) {
                is Group -> "group_${it.id}"
                is Teacher -> "teacher_${it.id}"
                else -> ""
            }.let { if (favorite) "fav_$it" else it }
        }
    ) {
        val name = when (it) {
            is Group -> it.name
            is Teacher -> it.name
            else -> ""
        }

        SearchItemChip(
            name = name,
            selected = state == name,
            favorite = favorite,
            modifier = Modifier
                .animateItem()
                .padding(horizontal = 8.dp),
            onLongClick = {
                listener(
                    when (it) {
                        is Group -> ScheduleEvent.OnGroupLongPressed(it)
                        is Teacher -> ScheduleEvent.OnTeacherLongPressed(it)
                        else -> error("unknown type")
                    }
                )
            },
            onClick = {
                listener(
                    ScheduleEvent.OnSearchCompleted(
                        when (it) {
                            is Group -> it.toScheduleSearch()
                            is Teacher -> it.toScheduleSearch()
                            else -> error("unknown type")
                        }
                    )
                )

                sheetController.animateClose()
            }
        )
    }
}

@Preview
@Composable
private fun LessonTeacherChipPreviev() {
    Column(
        Modifier
            .width(300.dp)
            .background(colorScheme.background)
    ) {
        SearchItemChip("ИС-33", selected = true) {}
        SearchItemChip("ИС-33", selected = false) {}
        SearchItemChip("ИС-33", selected = false, favorite = true) {}
    }
}

@Composable
private fun SearchItemChip(
    name: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    favorite: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor = if (selected) colorScheme.primary
    else if (favorite) colorScheme.secondaryContainer
    else Color.Transparent

    val contentColor = if (selected) colorScheme.onPrimary
    else if (favorite) colorScheme.onSecondaryContainer
    else colorScheme.primary


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(vertical = 3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .capplyIf(!favorite && !selected) {
                    border(1.dp, colorScheme.outlineVariant, shapes.small)
                }
                .clip(shapes.small)
                .combinedClickable(
                    interactionSource = interactionSource,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .background(containerColor)
        ) {
            Text(
                text = name,
                color = contentColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}