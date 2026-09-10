package app.what.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.what.domain.models.LessonsScheduleType
import app.what.domain.models.ScheduleSearch
import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.LessonsScheduleTypeDto
import app.what.ui.components.BreakInfo
import app.what.ui.components.DayTabs
import app.what.ui.components.LessonUI
import app.what.ui.components.SearchCapsule
import app.what.ui.components.SearchDrawer
import app.what.ui.components.SearchDrawerItem
import app.what.ui.components.UniversityOption
import app.what.ui.components.ViewType
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveScheduleScreen(
    universities: List<UniversityOption> = emptyList(),
    selectedUniversityId: String = "",
    onSelectUniversity: (String) -> Unit = {},
    searchItems: List<SearchDrawerItem>,
    selectedItem: SearchDrawerItem?,
    onSelectItem: (SearchDrawerItem) -> Unit,
    isSearchLoading: Boolean = false,
    scheduleDays: List<DayScheduleDto>,
    isScheduleLoading: Boolean = false,
    errorMessage: String? = null,
    onRefresh: () -> Unit,
    headerBanner: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableStateOf(0) }
    var isSearchSheetOpen by remember { mutableStateOf(false) }

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val currentTime = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    }

    LaunchedEffect(scheduleDays) {
        if (scheduleDays.isNotEmpty()) {
            val todayIdx = scheduleDays.indexOfFirst { it.date == today }
            val nextIdx = scheduleDays.indexOfFirst { it.date >= today }
            selectedDayIndex = when {
                todayIdx != -1 -> todayIdx
                nextIdx != -1 -> nextIdx
                else -> 0
            }
        } else {
            selectedDayIndex = 0
        }
    }

    val currentDaySchedule = scheduleDays.getOrNull(selectedDayIndex)

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(colorScheme.background)) {
        val isWide = maxWidth >= 760.dp

        Column(modifier = Modifier.fillMaxSize()) {
            if (headerBanner != null) {
                headerBanner()
            }

            if (isWide) {
                // Wide Screen: 2-Column Split Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Column: Search & University Sidebar
                    Box(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                    ) {
                        SearchDrawer(
                            universities = universities,
                            selectedUniversityId = selectedUniversityId,
                            onSelectUniversity = onSelectUniversity,
                            items = searchItems,
                            selectedItem = selectedItem,
                            isLoading = isSearchLoading,
                            onSelectItem = onSelectItem
                        )
                    }

                    // Vertical Separator
                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colorScheme.outlineVariant)
                    )

                    // Right Column: Authentic WHAT Schedule View
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        ScheduleTopBar(
                            selectedItem = selectedItem,
                            scheduleType = currentDaySchedule?.scheduleType,
                            isRefreshing = isScheduleLoading,
                            onRefresh = onRefresh,
                            onClickSearch = { /* In wide mode sidebar is always open */ },
                            isWide = true
                        )

                        if (isScheduleLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                                    .height(3.dp)
                                    .clip(CircleShape),
                                color = colorScheme.primary,
                                trackColor = colorScheme.surfaceVariant
                            )
                        }

                        if (scheduleDays.isNotEmpty()) {
                            DayTabs(
                                days = scheduleDays.map { it.date },
                                selectedIndex = selectedDayIndex,
                                onSelectDay = { selectedDayIndex = it }
                            )
                        }

                        ScheduleBody(
                            daySchedule = currentDaySchedule,
                            selectedItem = selectedItem,
                            currentTime = currentTime,
                            isLoading = isScheduleLoading,
                            errorMessage = errorMessage,
                            onRefresh = onRefresh,
                            onOpenSearch = { isSearchSheetOpen = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Compact Screen (Mobile / Narrow Window Layout)
                Column(modifier = Modifier.fillMaxSize()) {
                    ScheduleTopBar(
                        selectedItem = selectedItem,
                        scheduleType = currentDaySchedule?.scheduleType,
                        isRefreshing = isScheduleLoading,
                        onRefresh = onRefresh,
                        onClickSearch = { isSearchSheetOpen = true },
                        isWide = false
                    )

                    if (isScheduleLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .height(3.dp)
                                .clip(CircleShape),
                            color = colorScheme.primary,
                            trackColor = colorScheme.surfaceVariant
                        )
                    }

                    if (scheduleDays.isNotEmpty()) {
                        DayTabs(
                            days = scheduleDays.map { it.date },
                            selectedIndex = selectedDayIndex,
                            onSelectDay = { selectedDayIndex = it }
                        )
                    }

                    ScheduleBody(
                        daySchedule = currentDaySchedule,
                        selectedItem = selectedItem,
                        currentTime = currentTime,
                        isLoading = isScheduleLoading,
                        errorMessage = errorMessage,
                        onRefresh = onRefresh,
                        onOpenSearch = { isSearchSheetOpen = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Compact Search Modal Dialog / Bottom Sheet
                if (isSearchSheetOpen) {
                    Dialog(
                        onDismissRequest = { isSearchSheetOpen = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            color = colorScheme.background
                        ) {
                            SearchDrawer(
                                universities = universities,
                                selectedUniversityId = selectedUniversityId,
                                onSelectUniversity = onSelectUniversity,
                                items = searchItems,
                                selectedItem = selectedItem,
                                isLoading = isSearchLoading,
                                onSelectItem = {
                                    onSelectItem(it)
                                    isSearchSheetOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTopBar(
    selectedItem: SearchDrawerItem?,
    scheduleType: LessonsScheduleTypeDto?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onClickSearch: () -> Unit,
    isWide: Boolean,
    modifier: Modifier = Modifier
) {
    val searchModel: ScheduleSearch? = selectedItem?.let {
        if (it.isTeacher) ScheduleSearch.Teacher(it.title, it.id)
        else ScheduleSearch.Group(it.title, it.id)
    }

    val domainScheduleType: LessonsScheduleType? = scheduleType?.let {
        when (it) {
            LessonsScheduleTypeDto.COMMON -> LessonsScheduleType.COMMON
            LessonsScheduleTypeDto.SHORTENED -> LessonsScheduleType.SHORTENED
            LessonsScheduleTypeDto.WITH_CLASS_HOUR -> LessonsScheduleType.WITH_CLASS_HOUR
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchCapsule(
            search = searchModel,
            scheduleType = domainScheduleType,
            placeholder = if (isWide) "Выберите расписание слева" else "Нажмите для поиска...",
            modifier = Modifier.weight(1f),
            onClick = onClickSearch
        )

        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = if (isRefreshing) colorScheme.outline else colorScheme.primary
            )
        }
    }
}

@Composable
private fun ScheduleBody(
    daySchedule: DayScheduleDto?,
    selectedItem: SearchDrawerItem?,
    currentTime: LocalTime,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ошибка загрузки расписания",
                        style = typography.titleMedium,
                        color = colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text("Повторить", fontWeight = FontWeight.Bold)
                    }
                }
            }

            selectedItem == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Для того чтобы появилось расписание нужно выбрать группу",
                        style = typography.bodyLarge,
                        color = colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenSearch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text("Выбрать группу", fontWeight = FontWeight.Bold)
                    }
                }
            }

            daySchedule == null || daySchedule.lessons.isEmpty() -> {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Тут ничего нет, попробуйте другую группу :3",
                            style = typography.bodyLarge,
                            color = colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                val lessons = daySchedule.lessons
                val viewType = if (selectedItem.isTeacher) ViewType.TEACHER else ViewType.STUDENT

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(lessons, key = { index, l -> "${l.number}_${l.startTime}_$index" }) { index, lesson ->
                        LessonUI(
                            data = lesson,
                            viewType = viewType,
                            currentTime = currentTime
                        )

                        // Calculate break to next lesson
                        if (index < lessons.size - 1) {
                            val nextLesson = lessons[index + 1]
                            val breakMinutes = (nextLesson.startTime.toSecondOfDay() - lesson.endTime.toSecondOfDay()) / 60
                            if (breakMinutes > 0) {
                                val isBreakActive = currentTime in lesson.endTime..nextLesson.startTime
                                BreakInfo(
                                    minutes = breakMinutes,
                                    active = isBreakActive
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}
