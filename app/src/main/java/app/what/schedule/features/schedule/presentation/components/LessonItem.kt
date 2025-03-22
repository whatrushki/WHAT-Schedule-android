package app.what.schedule.features.schedule.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.core.Listener
import app.what.foundation.ui.Gap
import app.what.foundation.ui.applyIf
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useState
import app.what.foundation.utils.remember
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.LessonState
import app.what.schedule.data.remote.api.LessonType
import app.what.schedule.data.remote.api.OneTimeUnit
import app.what.schedule.data.remote.api.Teacher
import app.what.schedule.data.remote.utils.formatTime
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.presentation.theme.icons.WHATIcons
import app.what.schedule.presentation.theme.icons.filled.Domain
import app.what.schedule.presentation.theme.icons.filled.Group
import app.what.schedule.presentation.theme.icons.filled.MeetingRoom
import app.what.schedule.presentation.theme.icons.filled.Person
import java.time.LocalTime

enum class ViewType {
    TEACHER, STUDENT
}

@Composable
fun LessonUI(
    modifier: Modifier = Modifier,
    data: Lesson,
    viewType: ViewType,
    currentTime: LocalTime? = null,
    listener: Listener<ScheduleEvent>
) = when (data.type) {
    LessonType.CLASS_HOUR -> EventView(
        data,
        currentTime,
        viewType,
        listener,
        modifier
    )

    else -> CommonView(
        data,
        viewType,
        currentTime,
        listener,
        modifier
    )
}

@Composable
private fun getCommonViewAccentColor(state: LessonState, type: LessonType) = when (state) {
    LessonState.REMOVED -> colorScheme.secondary
    else -> if (type != LessonType.COMMON) colorScheme.tertiary
    else colorScheme.primary
}

@Composable
private fun EventView(
    data: Lesson,
    currentTime: LocalTime?,
    viewType: ViewType,
    listener: Listener<ScheduleEvent>,
    modifier: Modifier = Modifier,
    expandable: Boolean = true
) {
    val commonViewAccentColor = getCommonViewAccentColor(data.state, data.type)

    val (expanded, setExpanded) = useState(currentTime != null && currentTime in data.startTime..data.endTime)

    val expandedShape = shapes.medium

    val expandedTitleBoxBackground by animateColorAsState(
        if (expanded) commonViewAccentColor.copy(alpha = .2f)
        else colorScheme.surfaceContainer
    )

    val backgroundColor by animateColorAsState(
        if (data.state.isRemoved) colorScheme.surfaceVariant
        else if (expanded) colorScheme.surfaceContainer
        else commonViewAccentColor
    )

    val titleColor by animateColorAsState(
        if (expanded) commonViewAccentColor
        else if (data.state.isRemoved) commonViewAccentColor
        else colorScheme.onTertiary
    )

    Box(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .applyIf(expanded) {
                height(134.dp)
                    .padding(12.dp, 0.dp)
                    .clip(expandedShape)
            }
            .background(backgroundColor)
            .bclick(enabled = expandable) {
                setExpanded(!expanded)
            }
    ) {
        AnimatedVisibility(expanded) {
            TimeLine(
                currentTime,
                data.startTime,
                data.endTime,
                commonViewAccentColor
            )
        }

        Tag(
            accentColor = commonViewAccentColor,
            state = data.state,
            modifier = Modifier
                .align(
                    if (expanded) Alignment.BottomEnd
                    else Alignment.CenterEnd
                )
                .padding(
                    end = if (expanded) 12.dp
                    else 20.dp,
                    bottom = if (expanded) 12.dp
                    else 0.dp
                )
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .animateContentSize()
                    .applyIf(expanded) {
                        clip(CircleShape)
                            .background(expandedTitleBoxBackground)
                            .padding(8.dp, 4.dp)
                    },
                horizontalArrangement = if (expanded)
                    Arrangement.spacedBy(8.dp) else Arrangement.Center
            ) {
                AnimatedVisibility(expanded) {
                    Text(
                        text = "${formatTime(data.startTime)} - ${formatTime(data.endTime)}",
                        color = titleColor,
                        style = typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Text(
                    text = data.subject,
                    color = titleColor,
                    textDecoration = if (data.state == LessonState.REMOVED) TextDecoration.LineThrough
                    else TextDecoration.None,
                    style = typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            if (expanded) Gap(8)

            AnimatedVisibility(expanded) {
                OtUnitsView(
                    viewType = viewType,
                    otUnits = data.otUnits,
                    listener = listener,
                    commonViewAccentColor,
                    Arrangement.SpaceEvenly
                )
            }
        }
    }
}

@Composable
private fun CommonView(
    data: Lesson,
    viewType: ViewType,
    currentTime: LocalTime? = null,
    listener: Listener<ScheduleEvent>,
    modifier: Modifier = Modifier
) {
    val commonViewAccentColor = getCommonViewAccentColor(data.state, data.type)
    val (expanded, setExpanded) = useState(false)
    val (expandable, setExpandable) = useState(true)

    Box(modifier
        .padding(horizontal = 12.dp)
        .fillMaxWidth()
        .applyIf(!expanded, elseBlock = {
            height(IntrinsicSize.Min)
        }) { height(146.dp) }
        .clip(shapes.medium)
        .background(
            if (data.state == LessonState.REMOVED) colorScheme.surfaceVariant
            else colorScheme.surfaceContainer
        )
        .bclick(expanded || expandable) {
            setExpanded(!expanded)
        }
    ) {
        Tag(
            state = data.state,
            accentColor = commonViewAccentColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 8.dp)
        )

        TimeLine(currentTime, data.startTime, data.endTime, commonViewAccentColor)

        Row(
            Modifier.padding(8.dp, 12.dp)
        ) {
            Gap(4)

            CommonViewLeftSegment(
                data.startTime,
                data.endTime,
                data.state,
                commonViewAccentColor,
                data.number
            )

            Gap(12)

            Column {
                CommonViewSubject(
                    data.subject,
                    data.type,
                    data.state,
                    commonViewAccentColor,
                    expanded,
                    setExpandable
                )

                Gap(8)

                OtUnitsView(viewType, data.otUnits, listener)
            }
        }
    }
}

@Composable
private fun BoxScope.TimeLine(
    currentTime: LocalTime?,
    startTime: LocalTime,
    endTime: LocalTime,
    accentColor: Color
) {
    val passingPercent by useState(0f).apply {
        if (currentTime != null) value = currentTime.percentOf(startTime, endTime)
    }

    if (currentTime != null) Box(
        modifier = Modifier
            .animateContentSize()
            .fillMaxHeight(passingPercent)
            .align(Alignment.TopStart)
            .width(4.dp)
            .background(accentColor)
    )

}

@Composable
private fun Tag(
    modifier: Modifier = Modifier,
    state: LessonState,
    accentColor: Color
) {
    if (state != LessonState.COMMON) Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .height(24.dp)
            .background(accentColor)
    ) {
        Text(
            modifier = Modifier.padding(8.dp, 4.dp),
            text = when (state) {
                LessonState.ADDED -> "доб."
                LessonState.REMOVED -> "отм."
                LessonState.CHANGED -> "изм."
                else -> ""
            },
            color = colorScheme.onPrimary,
            style = typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun OtUnitsView(
    viewType: ViewType,
    otUnits: List<OneTimeUnit>,
    listener: Listener<ScheduleEvent>,
    color: Color = colorScheme.secondary,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        if (viewType == ViewType.TEACHER) 16.dp else 8.dp
    )
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(),
    horizontalArrangement = horizontalArrangement
) {
    otUnits.forEach {
        Column {
            AdditionalInfo(
                color = color,
                icon = if (viewType == ViewType.STUDENT) WHATIcons.Person
                else WHATIcons.Group,
                text = if (viewType == ViewType.TEACHER) it.group.name
                else it.teacher.name.replace("__", "_"),
                modifier = Modifier.bclick {
                    if (viewType == ViewType.TEACHER)
                        listener(ScheduleEvent.OnLessonItemGroupClicked(it.group.name))
                    else listener(
                        ScheduleEvent.OnLessonItemTeacherClicked(
                            it.teacher.name
                        )
                    )
                }
            )

            AdditionalInfo(
                color = color,
                icon = WHATIcons.MeetingRoom,
                text = it.auditory
            )

            AdditionalInfo(
                color = color,
                icon = WHATIcons.Domain,
                text = it.building.ifEmpty { "_" }
            )
        }
    }
}

@Composable
private fun CommonViewSubject(
    subject: String,
    type: LessonType,
    state: LessonState,
    accentColor: Color,
    expanded: Boolean,
    setExpandable: (Boolean) -> Unit
) = Box(
    Modifier
        .clip(CircleShape)
        .fillMaxWidth()
        .background(accentColor.copy(alpha = .2f))
) {
    val isLongTitle = subject.split(" ").size > 3
    val (subjectFontSize, setSubjectFontSize) = useState(if (isLongTitle) 12 else 16)

    Text(modifier = Modifier.padding(16.dp, 8.dp),
        text = subject,
        color = when (state) {
            LessonState.REMOVED -> colorScheme.secondary
            else -> if (type != LessonType.COMMON) colorScheme.tertiary
            else colorScheme.onPrimaryContainer
        },
        fontSize = subjectFontSize.sp,
        overflow = TextOverflow.Ellipsis,
        maxLines = if (expanded) Int.MAX_VALUE else 2,
        style = typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            lineHeight = (subjectFontSize + 4).sp,
            textDecoration = if (state == LessonState.REMOVED) TextDecoration.LineThrough
            else TextDecoration.None
        ),
        onTextLayout = {
            setExpandable(it.hasVisualOverflow)
            if (it.lineCount > 1) setSubjectFontSize(12)
        }
    )
}

@Composable
private fun CommonViewLeftSegment(
    startTime: LocalTime,
    endTime: LocalTime,
    state: LessonState,
    accentColor: Color,
    number: Int
) = Column(
    modifier = Modifier
        .fillMaxHeight()
        .width(64.dp),
    verticalArrangement = Arrangement.SpaceBetween
) {
    Column {
        Gap(4)

        Text(
            text = formatTime(startTime),
            fontSize = 24.sp,
            color = accentColor,
            style = typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold
            )
        )

        Text(
            text = formatTime(endTime),
            fontSize = 20.sp,
            color = if (state == LessonState.REMOVED) colorScheme.secondary
            else colorScheme.onPrimaryContainer,
            style = typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .size(24.dp)
            .background(accentColor)
    ) {
        Text(
            text = number.toString(),
            color = colorScheme.onPrimary,
            style = typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun AdditionalInfo(
    icon: ImageVector,
    text: String,
    color: Color = colorScheme.secondary,
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier, verticalAlignment = Alignment.CenterVertically
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = color
    )

    Gap(8)

    Text(
        text = text,
        color = color,
        style = typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Preview(showBackground = true)
@Composable
fun LessonPreview() = Column {
    MaterialTheme {
        val currentTime = LocalTime.of(13, 40).remember()
        Gap(12)

        val lesson = LessonUI(
            data = Lesson(
                number = 1,
                subject = "Основы дискретной математики и философии науки",
                type = LessonType.COMMON,
                startTime = LocalTime.of(13, 10),
                endTime = LocalTime.of(14, 40),
                state = LessonState.REMOVED,
                otUnits = listOf(
                    OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Савельев А.В."),
                        building = "1",
                        auditory = "101"
                    ), OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Кузнецов А.В."),
                        building = "2",
                        auditory = "131"
                    )
                )
            ), viewType = ViewType.STUDENT, currentTime = currentTime
        ) {}
        Gap(12)

        val classHour = LessonUI(
            data = Lesson(
                number = 1,
                subject = "Классный час",
                type = LessonType.CLASS_HOUR,
                state = LessonState.REMOVED,
                startTime = LocalTime.of(13, 10),
                endTime = LocalTime.of(14, 40),
                otUnits = listOf(
                    OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Савельев А.В."),
                        building = "1",
                        auditory = "101"
                    ),
                )
            ), viewType = ViewType.TEACHER, currentTime = currentTime
        ) {}
        Gap(12)

        val lesson2 = LessonUI(
            data = Lesson(
                number = 1,
                subject = "Основы дискретной математики и философии науки",
                type = LessonType.COMMON,
                startTime = LocalTime.of(13, 10),
                endTime = LocalTime.of(14, 40),
                state = LessonState.CHANGED,
                otUnits = listOf(
                    OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Савельев А.В."),
                        building = "1",
                        auditory = "101"
                    ), OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Кузнецов А.В."),
                        building = "2",
                        auditory = "131"
                    )
                )
            ), viewType = ViewType.STUDENT, currentTime = currentTime
        ) {}
        Gap(12)

        val lesson3 = LessonUI(
            data = Lesson(
                number = 1,
                subject = "Доп.занятие",
                type = LessonType.COMMON,
                startTime = LocalTime.of(13, 10),
                endTime = LocalTime.of(14, 40),
                state = LessonState.ADDED,
                otUnits = listOf(
                    OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Савельев А.В."),
                        building = "1",
                        auditory = "101"
                    )
                )
            ), viewType = ViewType.STUDENT, currentTime = currentTime
        ) {}
        Gap(12)

        val classHour2 = LessonUI(
            data = Lesson(
                number = 1,
                subject = "Классный час",
                type = LessonType.CLASS_HOUR,
                state = LessonState.CHANGED,
                startTime = LocalTime.of(13, 10),
                endTime = LocalTime.of(14, 40),
                otUnits = listOf(
                    OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Савельев А.В."),
                        building = "1",
                        auditory = "101"
                    ),
                    OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Савельев А.В."),
                        building = "1",
                        auditory = "101"
                    ),
                )
            ), viewType = ViewType.TEACHER, currentTime = currentTime
        ) {}
        Gap(12)

        val lesson4 = LessonUI(
            data = Lesson(
                number = 1,
                subject = "Доп.занятие",
                type = LessonType.ADDITIONAL,
                startTime = LocalTime.of(13, 10),
                endTime = LocalTime.of(14, 40),
                state = LessonState.COMMON,
                otUnits = listOf(
                    OneTimeUnit(
                        group = Group("ИС-23"),
                        teacher = Teacher("Савельев А.В."),
                        building = "1",
                        auditory = "101"
                    )
                )
            ), viewType = ViewType.STUDENT, currentTime = currentTime
        ) {}

        Gap(12)
    }
}

private fun LocalTime.percentOf(start: LocalTime, end: LocalTime): Float {
    val timeLeft = (this.hour - start.hour) * 60 + this.minute - start.minute
    val timeAll = (end.hour - start.hour) * 60 + end.minute - start.minute
    return timeLeft.toFloat() / timeAll.toFloat()
}