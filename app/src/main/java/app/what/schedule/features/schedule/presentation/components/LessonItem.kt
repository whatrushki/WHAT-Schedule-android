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
import androidx.compose.runtime.remember
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
import app.what.foundation.core.UIComponent
import app.what.foundation.ui.Gap
import app.what.foundation.ui.applyIf
import app.what.foundation.ui.blcik
import app.what.foundation.ui.useState
import app.what.foundation.utils.clazy
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

class LessonUI(
    private val data: Lesson,
    private val viewType: ViewType,
    private val currentTime: LocalTime? = null,
    private val listener: Listener<ScheduleEvent>
) : UIComponent {
    private val nonStandard = data.type != LessonType.COMMON

    @Composable
    override fun content(modifier: Modifier) = when (data.type) {
        LessonType.CLASS_HOUR -> EventView(modifier)
        else -> CommonView(modifier)
    }


    @Composable
    private fun EventView(modifier: Modifier = Modifier, expandable: Boolean = true) {
        commonViewAccentColor.calculate()
        val (expanded, setExpanded) = useState(false)

        val expandedShape = shapes.medium

        val expandedTitleBoxBackground by animateColorAsState(
            if (expanded) commonViewAccentColor.get().copy(alpha = .2f)
            else colorScheme.surfaceContainer
        )

        val backgroundColor by animateColorAsState(
            if (data.state.isRemoved) colorScheme.surfaceVariant
            else if (expanded) colorScheme.surfaceContainer
            else commonViewAccentColor.get()
        )

        val titleColor by animateColorAsState(
            if (expanded) commonViewAccentColor.get()
            else if (data.state.isRemoved) commonViewAccentColor.get()
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
                .blcik(enabled = expandable) {
                    setExpanded(!expanded)
                }
        ) {
            AnimatedVisibility(expanded) { TimeLine() }

            Tag(
                Modifier
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
                        commonViewAccentColor.get(),
                        Arrangement.SpaceEvenly
                    )
                }
            }
        }
    }

    @Composable
    private fun CommonView(modifier: Modifier = Modifier) {
        commonViewAccentColor.calculate()
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
            .blcik(expanded || expandable) {
                setExpanded(!expanded)
            }
        ) {
            Tag(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp, end = 8.dp)
            )

            TimeLine()

            Row(
                Modifier.padding(8.dp, 12.dp)
            ) {
                Gap(4)

                CommonViewLeftSegment()

                Gap(12)

                Column {
                    CommonViewSubject(expanded, setExpandable)

                    Gap(8)

                    OtUnitsView()
                }
            }
        }
    }

    // -- CommonView Segments --
    private val commonViewAccentColor = clazy {
        when (data.state) {
            LessonState.REMOVED -> colorScheme.secondary
            else -> if (nonStandard) colorScheme.tertiary
            else colorScheme.primary
        }
    }

    @Composable
    private fun BoxScope.TimeLine() {
        val passingPercent by useState(0f).apply {
            if (currentTime != null) value = currentTime.percentOf(data.startTime, data.endTime)
        }

        if (currentTime != null) Box(
            modifier = Modifier
                .animateContentSize()
                .fillMaxHeight(passingPercent)
                .align(Alignment.TopStart)
                .width(4.dp)
                .background(commonViewAccentColor.get())
        )

    }

    @Composable
    private fun Tag(modifier: Modifier = Modifier) {
        if (data.state != LessonState.COMMON) Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(CircleShape)
                .height(24.dp)
                .background(commonViewAccentColor.get())
        ) {
            Text(
                modifier = Modifier.padding(8.dp, 4.dp),
                text = when (data.state) {
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
        data.otUnits.forEach {
            Column {
                AdditionalInfo(
                    color = color,
                    icon = if (viewType == ViewType.STUDENT) WHATIcons.Person
                    else WHATIcons.Group,
                    text = if (viewType == ViewType.TEACHER) it.group.name
                    else it.teacher.name.replace("__", "_"),
                    modifier = Modifier.blcik {
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
    private fun CommonViewSubject(expanded: Boolean, setExpandable: (Boolean) -> Unit) = Box(
        Modifier
            .clip(CircleShape)
            .fillMaxWidth()
            .background(commonViewAccentColor.get().copy(alpha = .2f))
    ) {
        val isLongTitle =
            remember(data.subject) { data.subject.split(" ").size > 3 }
        val (subjectFontSize, setSubjectFontSize) = useState(if (isLongTitle) 12 else 16)

        Text(modifier = Modifier.padding(16.dp, 8.dp),
            text = data.subject,
            color = when (data.state) {
                LessonState.REMOVED -> colorScheme.secondary
                else -> if (nonStandard) colorScheme.tertiary
                else colorScheme.onPrimaryContainer
            },
            fontSize = subjectFontSize.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            style = typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = (subjectFontSize + 4).sp,
                textDecoration = if (data.state == LessonState.REMOVED) TextDecoration.LineThrough
                else TextDecoration.None
            ),
            onTextLayout = {
                setExpandable(it.hasVisualOverflow)
                if (it.lineCount > 1) setSubjectFontSize(12)
            }
        )
    }

    @Composable
    private fun CommonViewLeftSegment() = Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(64.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Gap(4)

            Text(
                text = formatTime(data.startTime),
                fontSize = 24.sp,
                color = commonViewAccentColor.get(),
                style = typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Text(
                text = formatTime(data.endTime),
                fontSize = 20.sp,
                color = if (data.state == LessonState.REMOVED) colorScheme.secondary
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
                .background(commonViewAccentColor.get())
        ) {
            Text(
                text = data.number.toString(),
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
}

@Preview(showBackground = true)
@Composable
fun LessonPreview() = Column {
    MaterialTheme {
        val currentTime = LocalTime.of(13, 40).remember()
        Gap(12)

        val lesson = LessonUI(data = Lesson(
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
        ), viewType = ViewType.STUDENT, currentTime, {}).remember()

        lesson.content(Modifier)

        Gap(12)

        val classHour = LessonUI(data = Lesson(
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
        ), viewType = ViewType.TEACHER, currentTime, {}).remember()

        classHour.content(Modifier)

        Gap(12)

        val lesson2 = LessonUI(data = Lesson(
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
        ), viewType = ViewType.STUDENT, currentTime, {}).remember()

        lesson2.content(Modifier)

        Gap(12)

        val lesson3 = LessonUI(data = Lesson(
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
        ), viewType = ViewType.STUDENT, currentTime, {}).remember()

        lesson3.content(Modifier)

        Gap(12)

        val classHour2 = LessonUI(data = Lesson(
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
        ), viewType = ViewType.TEACHER, currentTime, {}).remember()

        classHour2.content(Modifier)

        Gap(12)

        val lesson4 = LessonUI(data = Lesson(
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
        ), viewType = ViewType.STUDENT, currentTime, {}).remember()

        lesson4.content(Modifier)

        Gap(12)
    }
}

private fun LocalTime.percentOf(start: LocalTime, end: LocalTime): Float {
    val timeLeft = (this.hour - start.hour) * 60 + this.minute - start.minute
    val timeAll = (end.hour - start.hour) * 60 + end.minute - start.minute
    return timeLeft.toFloat() / timeAll.toFloat()
}