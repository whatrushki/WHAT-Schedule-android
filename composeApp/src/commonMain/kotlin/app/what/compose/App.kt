package app.what.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.domain.models.DaySchedule
import app.what.domain.models.Group
import app.what.domain.models.Lesson
import app.what.domain.models.LessonState
import app.what.domain.models.LessonType
import app.what.domain.models.LessonsScheduleType
import app.what.domain.models.OneTimeUnit
import app.what.domain.models.ScheduleSearch
import app.what.domain.models.Teacher
import app.what.foundation.data.RemoteState
import app.what.foundation.ui.theme.WHATTheme
import app.what.navigation.core.ProvideGlobalDialog
import app.what.navigation.core.ProvideGlobalSheet
import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.LessonDto
import app.what.schedule.core.models.LessonStateDto
import app.what.schedule.core.models.LessonTypeDto
import app.what.schedule.core.models.LessonsScheduleTypeDto
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.features.schedule.presentation.ScheduleView
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch

enum class University(val title: String) {
    RKSI("РКСИ"),
    DGTU("ДГТУ"),
    IUBIP("ЮУИУиБ"),
    RINH("РИНХ")
}

fun LessonDto.toDomain(): Lesson = Lesson(
    date = date,
    number = number,
    startTime = startTime,
    endTime = endTime,
    subject = subject,
    otUnits = otUnits.map {
        OneTimeUnit(
            group = Group(it.group),
            teacher = Teacher(it.teacher),
            auditory = it.room,
            building = it.additional
        )
    },
    type = when {
        subject.contains("классный час", ignoreCase = true) -> LessonType.CLASS_HOUR
        type == LessonTypeDto.PRACTICE -> LessonType.PRACTISE
        type == LessonTypeDto.LECTURE -> LessonType.LECTURE
        type == LessonTypeDto.LABORATORY -> LessonType.LABORATORY
        type == LessonTypeDto.EXAM || type == LessonTypeDto.CREDIT -> LessonType.CREDIT
        else -> LessonType.COMMON
    },
    state = when (state) {
        LessonStateDto.ADDED -> LessonState.ADDED
        LessonStateDto.REMOVED -> LessonState.REMOVED
        LessonStateDto.CHANGED -> LessonState.CHANGED
        else -> LessonState.COMMON
    }
)

fun DayScheduleDto.toDomain(): DaySchedule = DaySchedule(
    date = date,
    scheduleType = when (scheduleType) {
        LessonsScheduleTypeDto.SHORTENED -> LessonsScheduleType.SHORTENED
        LessonsScheduleTypeDto.WITH_CLASS_HOUR -> LessonsScheduleType.WITH_CLASS_HOUR
        else -> LessonsScheduleType.COMMON
    },
    lessons = lessons.map { it.toDomain() }
)

@Composable
fun App(
    httpClient: HttpClient,
    dataLoader: ScheduleDataLoader = remember(httpClient) { DefaultScheduleDataLoader(httpClient) },
    headerBanner: (@Composable () -> Unit)? = null
) {
    var selectedUni by remember { mutableStateOf(University.RKSI) }
    var scheduleState by remember { mutableStateOf(ScheduleState()) }
    val scope = rememberCoroutineScope()

    fun loadSearches() {
        scheduleState = scheduleState.copy(
            scheduleSearchesState = RemoteState.Loading,
            schedules = emptyList(),
            selectedSearch = null,
            scheduleState = RemoteState.Empty
        )
        scope.launch {
            try {
                val searches = dataLoader.getSearches(selectedUni)
                scheduleState = scheduleState.copy(
                    scheduleSearches = searches,
                    scheduleSearchesState = RemoteState.Success
                )
            } catch (e: Exception) {
                scheduleState = scheduleState.copy(
                    scheduleSearchesState = RemoteState.Error(e)
                )
            }
        }
    }

    fun loadSchedule(search: ScheduleSearch) {
        scheduleState = scheduleState.copy(
            selectedSearch = search,
            scheduleState = RemoteState.Loading
        )
        scope.launch {
            try {
                val domainSchedules = dataLoader.getSchedule(selectedUni, search)
                scheduleState = scheduleState.copy(
                    schedules = domainSchedules,
                    scheduleState = RemoteState.Success
                )
            } catch (e: Exception) {
                scheduleState = scheduleState.copy(
                    scheduleState = RemoteState.Error(e)
                )
            }
        }
    }

    LaunchedEffect(selectedUni) {
        loadSearches()
    }

    WHATTheme {
        ProvideGlobalDialog {
            ProvideGlobalSheet {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (headerBanner != null) {
                            headerBanner()
                        }

                        // University Switcher Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorScheme.surface)
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            University.entries.forEach { uni ->
                                val isSelected = uni == selectedUni
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) colorScheme.primary
                                            else colorScheme.surfaceContainer
                                        )
                                        .clickable {
                                            if (selectedUni != uni) {
                                                selectedUni = uni
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = uni.title,
                                        color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        ScheduleView(
                            state = remember(scheduleState) { mutableStateOf(scheduleState) },
                            listener = { event ->
                                when (event) {
                                    is ScheduleEvent.OnSearchClicked -> loadSchedule(event.value)
                                    is ScheduleEvent.OnRefresh -> {
                                        scheduleState.selectedSearch?.let { loadSchedule(it) }
                                    }
                                    is ScheduleEvent.OnRefreshSearches -> loadSearches()
                                    else -> {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
