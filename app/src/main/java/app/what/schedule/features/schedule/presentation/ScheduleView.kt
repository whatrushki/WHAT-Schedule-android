package app.what.schedule.features.schedule.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.what.foundation.data.RemoteState
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.Show
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.bclick
import app.what.foundation.ui.capplyIf
import app.what.foundation.ui.controllers.rememberSheetController
import app.what.foundation.ui.useChange
import app.what.foundation.ui.useState
import app.what.foundation.utils.freeze
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.features.schedule.presentation.components.LessonUI
import app.what.schedule.features.schedule.presentation.components.ScheduleExportPane
import app.what.schedule.features.schedule.presentation.components.ScheduleShimmer
import app.what.schedule.features.schedule.presentation.components.SearchButton
import app.what.schedule.features.schedule.presentation.components.ViewType
import app.what.schedule.ui.components.Fallback
import app.what.schedule.ui.components.ScheduleSearchPane
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleView(
    state: State<ScheduleState>,
    listener: (ScheduleEvent) -> Unit
) = PullToRefreshBox(
    isRefreshing = state.value.scheduleState == RemoteState.Loading,
    onRefresh = { listener(ScheduleEvent.OnRefresh) },
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .capplyIf(state.value.scheduleState != RemoteState.Idle && state.value.scheduleState !is RemoteState.Error) {
                verticalScroll(rememberScrollState())
            }
    ) {
        val sheetController = rememberSheetController()

        val scheduleExportSheet = remember(state.value.schedules) {
            @Composable { ScheduleExportPane(state.value.selectedSearch, state.value.schedules) }
        }

        val scheduleSearchSheet = remember {
            @Composable {
                ScheduleSearchPane(
                    state,
                    {
                        listener(ScheduleEvent.OnSearchClicked(it))
                        sheetController.animateClose()
                    },
                    { listener(ScheduleEvent.OnSearchLongPressed(it)) }
                )
            }
        }
        val (scheduleType, setScheduleType) = useState<LessonsScheduleType?>(null)
        val currentDate = LocalDate.now().freeze()
        val currentTime = useChange(LocalTime.now(), 60) { LocalTime.now() }
        val scope = rememberCoroutineScope()
        val weeks = state.value.schedules.groupBy { it.date.getWeekNumber() }
        val daysPagerState = rememberPagerState { state.value.schedules.size }
        val weeksPagerState = rememberPagerState { weeks.size }

        LaunchedEffect(daysPagerState.currentPage, state.value.scheduleState) {
            if (state.value.scheduleState != RemoteState.Success) return@LaunchedEffect

            scope.launch {
                weeks.values.forEachIndexed { index, it ->
                    if (state.value.schedules[daysPagerState.currentPage] in it) {
                        weeksPagerState.animateScrollToPage(index)
                        return@forEachIndexed
                    }
                }
            }

            val currentDaySchedule =
                state.value.schedules.getOrNull(daysPagerState.currentPage) ?: return@LaunchedEffect
            setScheduleType(currentDaySchedule.scheduleType)
        }

        Gap(16)


        Row(
            Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchButton(
                state.value.selectedSearch,
                scheduleType,
                Modifier
                    .animateContentSize()
                    .weight(1f)
            ) {
                sheetController.open(content = scheduleSearchSheet, full = true)
            }

            AnimatedEnter(state.value.schedules.isNotEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f, true)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceContainer)
                        .bclick(state.value.schedules.isNotEmpty()) {
                            sheetController.open(content = scheduleExportSheet)
                        }
                ) {
                    Icons.Default.Share.Show(Modifier.size(24.dp), colorScheme.onSecondaryContainer)
                }
            }
        }

        Gap(8)

        when (state.value.scheduleState) {
            RemoteState.Loading -> ScheduleShimmer()
            RemoteState.Idle -> AnimatedEnter {
                Fallback(
                    text = "Для того чтобы появилось расписание нужно выбрать группу",
                    modifier = Modifier.fillMaxSize(),
                    action = "Выбрать" to {
                        sheetController.open(content = scheduleSearchSheet, full = true)
                    }
                )
            }

            is RemoteState.Error -> Fallback(
                "Произошла непредвиденная ошибка",
                Modifier.fillMaxSize(),
                "Попробовать снова" to { listener(ScheduleEvent.OnRefresh) }
            )

            RemoteState.Success -> {
                ScheduleCalendar(weeks, weeksPagerState, daysPagerState) {
                    scope.launch { daysPagerState.animateScrollToPage(it) }
                }

                Gap(8)

                AnimatedEnter {
                    HorizontalPager(
                        state = daysPagerState,
                        verticalAlignment = Alignment.Top,
                        key = { state.value.schedules[it].date.toString() },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            state.value.schedules[it].lessons.forEach { lesson ->
                                LessonUI(
                                    data = lesson,
                                    listener = listener,
                                    currentTime = if (state.value.schedules[it].date == currentDate)
                                        currentTime.value else null,
                                    viewType = when (state.value.selectedSearch) {
                                        is ScheduleSearch.Teacher -> ViewType.TEACHER
                                        else -> ViewType.STUDENT
                                    }
                                )
                            }

                            Gap(120)
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}

@Composable
fun ScheduleCalendar(
    weeks: Map<Int, List<DaySchedule>>,
    weeksPagerState: PagerState,
    daysPagerState: PagerState,
    onClick: (day: Int) -> Unit
) {
    val schedules = weeks.values.flatten()

    HorizontalPager(
        weeksPagerState,
        key = { weeks.entries.elementAt(it).key },
        modifier = Modifier.fillMaxWidth()
    ) {
        val thisWeek = weeks.entries.elementAt(it)

        SingleChoiceSegmentedButtonRow(
            space = (-4).dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            thisWeek.value.forEach { day ->
                val realIndex = schedules.indexOfFirst { it.date == day.date }
                val selected = daysPagerState.currentPage == realIndex

                SegmentTab(
                    selected = selected,
                    index = realIndex,
                    count = schedules.size,
                    icon = null,
                    label = "${day.date.dayOfMonth}" + (if (thisWeek.value.size > 5) "\n"
                    else " ") + day.date.dayOfWeek.getDisplayName(
                        if (thisWeek.value.size > 2) TextStyle.SHORT_STANDALONE
                        else TextStyle.FULL_STANDALONE,
                        Locale.getDefault()
                    )
                ) {
                    onClick(realIndex)
                }
            }
        }
    }
}

fun LocalDate.getWeekNumber(): Int {
    val c = Calendar.getInstance()
    c.set(year, monthValue.minus(1), dayOfMonth)
    return c.get(Calendar.WEEK_OF_YEAR)
}
