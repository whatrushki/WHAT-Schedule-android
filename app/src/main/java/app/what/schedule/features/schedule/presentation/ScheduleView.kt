package app.what.schedule.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.what.foundation.data.RemoteState
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.useChange
import app.what.foundation.ui.useState
import app.what.foundation.utils.remember
import app.what.navigation.core.rememberSheetController
import app.what.schedule.data.remote.api.LessonsScheduleType
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.features.schedule.presentation.components.LessonPreview
import app.what.schedule.features.schedule.presentation.components.LessonUI
import app.what.schedule.features.schedule.presentation.components.ScheduleShimmer
import app.what.schedule.features.schedule.presentation.components.SearchButton
import app.what.schedule.features.schedule.presentation.components.SearchSheet
import app.what.schedule.features.schedule.presentation.components.ViewType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleView(
    state: ScheduleState,
    listener: (ScheduleEvent) -> Unit
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.verticalScroll(rememberScrollState())
) {
    val sheetController = rememberSheetController()
    val sheet = @Composable { SearchSheet(state, listener) }
    val pagerState = rememberPagerState { state.schedules.size }
    val scope = rememberCoroutineScope()
    val (scheduleType, setScheduleType) = useState<LessonsScheduleType?>(null)
    val currentDate = useChange(LocalDate.now(), 60) { LocalDate.now() }
    val currentTime = useChange(LocalTime.now(), 60) { LocalTime.now() }

    LaunchedEffect(pagerState.currentPage, state.scheduleState) {
        if (state.scheduleState != RemoteState.Success) return@LaunchedEffect
        val currentDaySchedule =
            state.schedules.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        setScheduleType(currentDaySchedule.scheduleType)
    }

    Gap(16)

    SearchButton(state.search, scheduleType) {
        sheetController.apply {
            content = sheet
            cancellable = true
            open()
        }
    }

    Gap(8)

    when (state.scheduleState) {
        RemoteState.Loading, RemoteState.Idle -> ScheduleShimmer()
        RemoteState.Success -> {
            SingleChoiceSegmentedButtonRow(
                Modifier.padding(horizontal = 12.dp)
            ) {
                state.schedules.forEachIndexed { index, it ->
                    val selected = pagerState.currentPage == index

                    SegmentTab(
                        selected = selected,
                        index = index,
                        count = state.schedules.size,
                        icon = null,
                        label = "${it.date.dayOfMonth}" + if (state.schedules.size > 6) ""
                        else " " + it.date.dayOfWeek.getDisplayName(
                            if (state.schedules.size > 2) TextStyle.SHORT_STANDALONE
                            else TextStyle.FULL_STANDALONE,
                            Locale.getDefault()
                        )
                    ) { scope.launch { pagerState.animateScrollToPage(index) } }
                }
            }

            Gap(8)

            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxHeight()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    state.schedules[it].lessons.forEach { lesson ->
                        val lessonUI = LessonUI(
                            data = lesson,
                            listener = listener,
                            currentTime = if (state.schedules[it].date == currentDate.value)
                                currentTime.value else null,
                            viewType = when (state.search) {
                                is ScheduleSearch.Teacher -> ViewType.TEACHER
                                else -> ViewType.STUDENT
                            }
                        ).remember()

                        lessonUI.content(Modifier)
                    }
                }
            }

            Gap(12)
        }

        else -> Unit
    }
}