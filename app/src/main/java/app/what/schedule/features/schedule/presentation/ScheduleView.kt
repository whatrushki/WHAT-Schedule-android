package app.what.schedule.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.what.foundation.data.RemoteState
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.capplyIf
import app.what.foundation.ui.controllers.rememberSheetController
import app.what.foundation.ui.useChange
import app.what.foundation.ui.useState
import app.what.foundation.utils.freeze
import app.what.schedule.data.remote.api.LessonsScheduleType
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.features.schedule.presentation.components.LessonUI
import app.what.schedule.features.schedule.presentation.components.ScheduleShimmer
import app.what.schedule.features.schedule.presentation.components.SearchButton
import app.what.schedule.features.schedule.presentation.components.SearchSheet
import app.what.schedule.features.schedule.presentation.components.ViewType
import app.what.schedule.ui.components.Fallback
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleView(
    state: ScheduleState,
    listener: (ScheduleEvent) -> Unit
) = PullToRefreshBox(
    isRefreshing = state.scheduleState == RemoteState.Loading,
    onRefresh = { listener(ScheduleEvent.OnRefresh) },
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .capplyIf(state.scheduleState != RemoteState.Idle) {
                verticalScroll(rememberScrollState())
            }
    ) {
        val sheetController = rememberSheetController()
        val sheet = @Composable { SearchSheet(state, listener) }
        val pagerState = rememberPagerState { state.schedules.size }
        val scope = rememberCoroutineScope()
        val (scheduleType, setScheduleType) = useState<LessonsScheduleType?>(null)
        val currentDate = LocalDate.now().freeze()
        val currentTime = useChange(LocalTime.now(), 60) { LocalTime.now() }


        LaunchedEffect(pagerState.currentPage, state.scheduleState) {
            if (state.scheduleState != RemoteState.Success) return@LaunchedEffect
            val currentDaySchedule =
                state.schedules.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
            setScheduleType(currentDaySchedule.scheduleType)
        }

        Gap(16)

        SearchButton(state.search, scheduleType) {
            sheetController.open(content = sheet, full = true)
        }

        Gap(8)

        when (state.scheduleState) {
            RemoteState.Idle -> AnimatedEnter {
                Fallback(
                    text = "Для того чтобы появилось расписание нужно выбрать группу",
                    modifier = Modifier.fillMaxSize(),
                    action = "Выбрать" to { sheetController.open(content = sheet, full = true) }
                )
            }

            RemoteState.Loading -> ScheduleShimmer()

            RemoteState.Success -> {
                SingleChoiceSegmentedButtonRow(
                    space = (-4).dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    state.schedules.forEachIndexed { index, it ->
                        val selected = pagerState.currentPage == index

                        SegmentTab(
                            selected = selected,
                            index = index,
                            count = state.schedules.size,
                            icon = null,
                            label = "${it.date.dayOfMonth}" + if (state.schedules.size > 5) ""
                            else " " + it.date.dayOfWeek.getDisplayName(
                                if (state.schedules.size > 2) TextStyle.SHORT_STANDALONE
                                else TextStyle.FULL_STANDALONE,
                                Locale.getDefault()
                            )
                        ) { scope.launch { pagerState.animateScrollToPage(index) } }
                    }
                }

                Gap(8)

                AnimatedEnter {
                    HorizontalPager(
                        state = pagerState,
                        verticalAlignment = Alignment.Top,
                        key = { state.schedules[it].date.toString() },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            state.schedules[it].lessons.forEach { lesson ->
                                LessonUI(
                                    data = lesson,
                                    listener = listener,
                                    currentTime = if (state.schedules[it].date == currentDate)
                                        currentTime.value else null,
                                    viewType = when (state.search) {
                                        is ScheduleSearch.Teacher -> ViewType.TEACHER
                                        else -> ViewType.STUDENT
                                    }
                                )
                            }

                            Gap(80)
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}