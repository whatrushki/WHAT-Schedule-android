package app.what.schedule.features.schedule.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.data.RemoteState
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.Show
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.bclick
import app.what.foundation.ui.capplyIf
import app.what.foundation.ui.controllers.rememberDialogController
import app.what.foundation.ui.controllers.rememberSheetController
import app.what.foundation.ui.useChange
import app.what.foundation.ui.useSave
import app.what.foundation.ui.useState
import app.what.foundation.utils.ShareUtils
import app.what.foundation.utils.ShareVariant
import app.what.foundation.utils.freeze
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.features.schedule.presentation.components.BreakInfo
import app.what.schedule.features.schedule.presentation.components.LessonUI
import app.what.schedule.features.schedule.presentation.components.ScheduleExportPane
import app.what.schedule.features.schedule.presentation.components.ScheduleShimmer
import app.what.schedule.features.schedule.presentation.components.SearchButton
import app.what.schedule.features.schedule.presentation.components.ViewType
import app.what.schedule.ui.components.Fallback
import app.what.schedule.ui.components.ScheduleSearchPane
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.CloudSync
import app.what.schedule.ui.theme.icons.filled.Run
import app.what.schedule.ui.theme.icons.filled.Warn
import app.what.schedule.utils.Analytics
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
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
            .capplyIf(state.value.scheduleState != RemoteState.Idle && !state.value.schedules.isEmpty()) {
                verticalScroll(rememberScrollState())
            }
    ) {
        val context = LocalContext.current
        val sheet = rememberSheetController()
        val dialog = rememberDialogController()
        val scheduleExportSheet = remember(state.value.schedules) {
            @Composable { ScheduleExportPane(state.value.selectedSearch, state.value.schedules) }
        }
        
        val scheduleSearchSheet = remember {
            @Composable {
                ScheduleSearchPane(
                    state,
                    {
                        listener(ScheduleEvent.OnSearchClicked(it))
                        sheet.animateClose()
                    },
                    { listener(ScheduleEvent.OnSearchLongPressed(it)) }
                )
            }
        }
        
        val (scheduleType, setScheduleType) = useState<LessonsScheduleType?>(null)
        val currentDate = LocalDate.now().freeze()
        val currentTime = useChange(LocalTime.now(), 60) { LocalTime.now() }
        var showBreaks by useSave(false)
        val scope = rememberCoroutineScope()
        val weeks = state.value.schedules.groupBy { it.date.getWeekNumber() }
        val daysPagerState = rememberPagerState { state.value.schedules.size }
        val weeksPagerState = rememberPagerState { weeks.size }
        
        @Composable
        fun Lesson.Show(date: LocalDate) = LessonUI(
            data = this,
            listener = listener,
            currentTime = if (date == currentDate)
                currentTime.value else null,
            viewType = when (state.value.selectedSearch) {
                is ScheduleSearch.Teacher -> ViewType.TEACHER
                else -> ViewType.STUDENT
            }
        )
        
        LaunchedEffect(daysPagerState.currentPage, state.value.scheduleState) {
            if (state.value.schedules.isEmpty()) return@LaunchedEffect
            
            scope.launch {
                weeks.values.forEachIndexed { index, it ->
                    if (state.value.schedules.getOrNull(daysPagerState.currentPage) in it) {
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
        
        Column(
            Modifier.statusBarsPadding()
        ) {
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
                    sheet.open(content = scheduleSearchSheet, full = true)
                }
                
                AnimatedEnter(state.value.schedules.isNotEmpty()) {
                    StyledIconButton(
                        WHATIcons.Run,
                        active = if (showBreaks) ActiveState.ACTIVE else ActiveState.DISABLED
                    ) { showBreaks = !showBreaks }
                }
                
                AnimatedEnter(state.value.schedules.isNotEmpty()) {
                    StyledIconButton(
                        Icons.Default.Share,
                        state.value.schedules.isNotEmpty()
                    ) {
                        Analytics.logShare("schedule", "")
                        sheet.open(content = scheduleExportSheet)
                    }
                }
            }
            
            
            AnimatedEnter {
                Box(
                    Modifier
                        .height(54.dp)
                        .padding(12.dp, 8.dp)
                        .fillMaxWidth()
                ) {
                    when (state.value.scheduleState) {
                        RemoteState.Loading -> LinearProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            color = ProgressIndicatorDefaults.linearColor.copy(.7f),
                            trackColor = ProgressIndicatorDefaults.linearColor
                        )
                        
                        else -> Row {
                            AnimatedEnter(
                                modifier = Modifier
                                    .weight(1f)
                                    .animateContentSize()
                            ) {
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(
                                            when (state.value.scheduleState) {
                                                is RemoteState.Error -> colorScheme.errorContainer
                                                else -> colorScheme.tertiaryContainer
                                            }
                                        )
                                        .bclick {
                                            when (val s = state.value.scheduleState) {
                                                is RemoteState.Error -> dialog.open(true) {
                                                    ErrorContent(s.e) {
                                                        ShareUtils.share(
                                                            context,
                                                            ShareVariant.Clipboard,
                                                            s.e.message + "\n\n" + s.e.stackTraceToString()
                                                        )
                                                    }
                                                }
                                                
                                                else -> Unit
                                            }
                                        }
                                ) {
                                    val (text, color) = when (state.value.scheduleState) {
                                        RemoteState.Success -> "🌱 Успешно" to colorScheme.onTertiaryContainer
                                        is RemoteState.Error -> "🛟 Ошибка" to colorScheme.onErrorContainer
                                        else -> "🪹 Пусто" to colorScheme.onTertiaryContainer
                                    }
                                    
                                    Text(
                                        text,
                                        color = color,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                            
                            Gap(8)
                            
                            AnimatedEnter(delay = 150) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colorScheme.primaryContainer)
                                        .bclick {
                                            listener(ScheduleEvent.OnCloudSync)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(16.dp, 8.dp)
                                    ) {
                                        WHATIcons.CloudSync.Show(colorScheme.onPrimaryContainer)
                                        
                                        Text(
                                            "cloud sync",
                                            color = colorScheme.onPrimaryContainer,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            AnimatedEnter(state.value.schedules.isNotEmpty()) {
                ScheduleCalendar(weeks, weeksPagerState, daysPagerState) {
                    scope.launch { daysPagerState.animateScrollToPage(it) }
                }
            }
            
            when (state.value.scheduleState) {
                RemoteState.Loading if state.value.schedules.isEmpty() -> ScheduleShimmer()
                RemoteState.Success if state.value.schedules.isEmpty() -> Fallback(
                    text = "Тут ничего нет, попробуйте другую группу :3",
                    modifier = Modifier.fillMaxSize(),
                    action = "Выбрать" to {
                        sheet.open(content = scheduleSearchSheet, full = true)
                    }
                )
                
                RemoteState.Idle if state.value.schedules.isEmpty() -> Fallback(
                    text = "Для того чтобы появилось расписание нужно выбрать группу",
                    modifier = Modifier.fillMaxSize(),
                    action = "Выбрать" to {
                        sheet.open(content = scheduleSearchSheet, full = true)
                    }
                )
                
                else -> AnimatedEnter {
                    HorizontalPager(
                        state = daysPagerState,
                        verticalAlignment = Alignment.Top,
                        key = { state.value.schedules[it].date.toString() },
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 8.dp)
                    ) {
                        val date = state.value.schedules[it].date
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(if (showBreaks) 4.dp else 12.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            state.value.schedules[it].lessons.zipWithNext()
                                .forEach { (first, second) ->
                                    first.Show(date)
                                    
                                    AnimatedEnter(showBreaks) {
                                        BreakInfo(
                                            first.endTime.until(
                                                second.startTime,
                                                ChronoUnit.MINUTES
                                            ).toInt(),
                                            currentTime.value in first.startTime..second.startTime && currentDate == first.date
                                        )
                                    }
                                    
                                }
                            
                            state.value.schedules[it].lessons.last().Show(date)
                            
                            Gap(132)
                        }
                    }
                }
            }
        }
    }
}

enum class ActiveState {
    DISABLED, NEUTRAL, ACTIVE
}

@Composable
fun StyledIconButton(
    icon: ImageVector,
    enabled: Boolean = true,
    active: ActiveState = ActiveState.NEUTRAL,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f, true)
            .clip(CircleShape)
            .background(colorScheme.surfaceContainer)
            .bclick(enabled, onClick)
    ) {
        icon.Show(
            when (active) {
                ActiveState.ACTIVE -> colorScheme.primary
                ActiveState.NEUTRAL -> colorScheme.secondary
                ActiveState.DISABLED -> colorScheme.secondary
            }
        )
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

@Composable
fun ErrorContent(
    error: Throwable,
    onCopyClick: () -> Unit
) {
    val stackTrace = remember(error) { error.stackTraceToString() }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            WHATIcons.Warn.Show(colorScheme.error, 28)
            Gap(12)
            Text(
                text = "Ошибка загрузки",
                style = typography.headlineSmall,
                color = colorScheme.onSurface
            )
        }
        
        Gap(20)
        
        Text(
            text = error.localizedMessage ?: error.message ?: "Произошла неизвестная ошибка",
            style = typography.bodyLarge,
            color = colorScheme.onSurfaceVariant
        )
        
        Gap(24)
        
        Text(
            text = "Полный стек-трейс",
            style = typography.titleMedium,
            color = colorScheme.primary
        )
        
        Gap(8)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
                .background(
                    color = colorScheme.surfaceVariant,
                    shape = shapes.medium
                )
                .border(
                    width = 1.dp,
                    color = colorScheme.outlineVariant,
                    shape = shapes.medium
                )
        ) {
            Text(
                text = stackTrace,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                style = typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                ),
                color = colorScheme.onSurfaceVariant
            )
        }
        
        Gap(24)
        Button(
            onClick = onCopyClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icons.Default.Share.Show(colorScheme.onPrimary, 20)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Копировать стек-трейс",
                style = typography.labelLarge
            )
        }
    }
}
