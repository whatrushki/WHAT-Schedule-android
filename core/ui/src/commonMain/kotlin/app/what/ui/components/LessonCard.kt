package app.what.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.domain.models.Group
import app.what.domain.models.Lesson
import app.what.domain.models.LessonState
import app.what.domain.models.LessonType
import app.what.domain.models.OneTimeUnit
import app.what.domain.models.ScheduleSearch
import app.what.domain.models.Teacher
import app.what.schedule.core.models.LessonDto
import app.what.schedule.core.models.LessonStateDto
import app.what.schedule.core.models.LessonTypeDto
import app.what.schedule.core.models.OneTimeUnitDto
import app.what.ui.icons.WHATIcons
import kotlinx.datetime.LocalTime

enum class ViewType {
    STUDENT, TEACHER
}

fun formatTime(time: LocalTime): String {
    val h = time.hour.toString().padStart(2, '0')
    val m = time.minute.toString().padStart(2, '0')
    return "$h:$m"
}

private fun LocalTime.percentOf(start: LocalTime, end: LocalTime): Float {
    val timeLeft = (this.hour - start.hour) * 60 + this.minute - start.minute
    val timeAll = (end.hour - start.hour) * 60 + end.minute - start.minute
    if (timeAll <= 0) return 0f
    return (timeLeft.toFloat() / timeAll.toFloat()).coerceIn(0f, 1f)
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

@Composable
fun LessonUI(
    data: LessonDto,
    viewType: ViewType = ViewType.STUDENT,
    currentTime: LocalTime? = null,
    modifier: Modifier = Modifier,
    onSearchClicked: ((ScheduleSearch) -> Unit)? = null
) {
    LessonUI(
        data = data.toDomain(),
        viewType = viewType,
        currentTime = currentTime,
        modifier = modifier,
        onSearchClicked = onSearchClicked
    )
}

@Composable
fun LessonUI(
    data: Lesson,
    viewType: ViewType = ViewType.STUDENT,
    currentTime: LocalTime? = null,
    modifier: Modifier = Modifier,
    onSearchClicked: ((ScheduleSearch) -> Unit)? = null
) = when (data.type) {
    LessonType.CLASS_HOUR -> EventView(
        data = data,
        currentTime = currentTime,
        viewType = viewType,
        modifier = modifier,
        onSearchClicked = onSearchClicked
    )
    else -> CommonView(
        data = data,
        viewType = viewType,
        currentTime = currentTime,
        modifier = modifier,
        onSearchClicked = onSearchClicked
    )
}

@Composable
private fun getCommonViewAccentColor(state: LessonState, type: LessonType): Color = when (state) {
    LessonState.REMOVED -> colorScheme.secondary
    else -> if (type.isNonStandard) colorScheme.tertiary else colorScheme.primary
}

@Composable
private fun EventView(
    data: Lesson,
    currentTime: LocalTime?,
    viewType: ViewType,
    modifier: Modifier = Modifier,
    expandable: Boolean = true,
    onSearchClicked: ((ScheduleSearch) -> Unit)? = null
) {
    val commonViewAccentColor = getCommonViewAccentColor(data.state, data.type)
    var expanded by remember {
        mutableStateOf(currentTime != null && currentTime in data.startTime..data.endTime)
    }

    val expandedTitleBoxBackground by animateColorAsState(
        if (expanded) commonViewAccentColor.copy(alpha = 0.2f)
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
            .then(
                if (expanded) Modifier
                    .height(134.dp)
                    .padding(horizontal = 12.dp)
                    .clip(shapes.medium)
                else Modifier
            )
            .background(backgroundColor)
            .clickable(enabled = expandable) {
                expanded = !expanded
            }
    ) {
        AnimatedVisibility(expanded) {
            TimeLine(
                currentTime = currentTime,
                startTime = data.startTime,
                endTime = data.endTime,
                accentColor = commonViewAccentColor
            )
        }

        Tag(
            accentColor = commonViewAccentColor,
            state = data.state,
            modifier = Modifier
                .align(if (expanded) Alignment.BottomEnd else Alignment.CenterEnd)
                .padding(
                    end = if (expanded) 12.dp else 20.dp,
                    bottom = if (expanded) 12.dp else 0.dp
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .animateContentSize()
                    .then(
                        if (expanded) Modifier
                            .clip(CircleShape)
                            .background(expandedTitleBoxBackground)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                        else Modifier
                    ),
                horizontalArrangement = if (expanded) Arrangement.spacedBy(8.dp) else Arrangement.Center
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

            if (expanded) Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(expanded) {
                OtUnitsView(
                    viewType = viewType,
                    otUnits = data.otUnits,
                    color = commonViewAccentColor,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    expanded = false,
                    onSearchClicked = onSearchClicked
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
    modifier: Modifier = Modifier,
    onSearchClicked: ((ScheduleSearch) -> Unit)? = null
) {
    val commonViewAccentColor = getCommonViewAccentColor(data.state, data.type)
    var expanded by remember { mutableStateOf(false) }
    var expandable by remember { mutableStateOf(data.otUnits.size > 3) }

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .then(
                if (expanded) Modifier.height(146.dp)
                else Modifier.height(IntrinsicSize.Min)
            )
            .clip(shapes.medium)
            .background(
                if (data.state == LessonState.REMOVED) colorScheme.surfaceVariant
                else colorScheme.surfaceContainer
            )
            .clickable(enabled = expanded || expandable) {
                expanded = !expanded
            }
    ) {
        Tag(
            state = data.state,
            accentColor = commonViewAccentColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 8.dp)
        )

        TimeLine(
            currentTime = currentTime,
            startTime = data.startTime,
            endTime = data.endTime,
            accentColor = commonViewAccentColor
        )

        Row(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Spacer(modifier = Modifier.width(4.dp))

            CommonViewLeftSegment(
                startTime = data.startTime,
                endTime = data.endTime,
                state = data.state,
                accentColor = commonViewAccentColor,
                number = data.number
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                CommonViewSubject(
                    subject = data.subject,
                    type = data.type,
                    state = data.state,
                    accentColor = commonViewAccentColor,
                    expanded = expanded,
                    setExpandable = { expandable = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OtUnitsView(
                    viewType = viewType,
                    otUnits = data.otUnits,
                    expanded = expanded,
                    setExpandable = { expandable = it },
                    onSearchClicked = onSearchClicked
                )
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
    if (currentTime == null) return
    val percent = currentTime.percentOf(startTime, endTime)
    if (percent > 0f) {
        Box(
            modifier = Modifier
                .animateContentSize()
                .fillMaxHeight(percent)
                .align(Alignment.TopStart)
                .width(4.dp)
                .background(accentColor)
        )
    }
}

@Composable
private fun Tag(
    modifier: Modifier = Modifier,
    state: LessonState,
    accentColor: Color
) {
    if (state != LessonState.COMMON) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(CircleShape)
                .height(24.dp)
                .background(accentColor)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
        Spacer(modifier = Modifier.height(4.dp))

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
private fun CommonViewSubject(
    subject: String,
    type: LessonType,
    state: LessonState,
    accentColor: Color,
    expanded: Boolean,
    setExpandable: (Boolean) -> Unit
) = Box(
    Modifier
        .clip(if (expanded) shapes.medium else CircleShape)
        .fillMaxWidth()
        .background(accentColor.copy(alpha = 0.2f))
) {
    val isLongTitle = subject.split(" ").size > 3
    var subjectFontSize by remember { mutableStateOf(if (isLongTitle) 12 else 16) }

    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = subject,
        color = when (state) {
            LessonState.REMOVED -> colorScheme.secondary
            else -> if (type.isNonStandard) colorScheme.tertiary
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
            if (it.hasVisualOverflow) setExpandable(true)
            if (it.lineCount > 1) subjectFontSize = 12
        }
    )
}

@Composable
private fun OtUnitsView(
    viewType: ViewType,
    otUnits: List<OneTimeUnit>,
    expanded: Boolean = false,
    setExpandable: (Boolean) -> Unit = {},
    onSearchClicked: ((ScheduleSearch) -> Unit)? = null,
    color: Color = colorScheme.secondary,
    horizontalArrangement: Arrangement.Horizontal =
        Arrangement.spacedBy(if (viewType == ViewType.TEACHER) 16.dp else 8.dp)
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(),
    horizontalArrangement = horizontalArrangement
) {
    val unionUnits = otUnits.size > 3
    val displayUnits = if (unionUnits && !expanded) otUnits.take(1) else otUnits
    displayUnits.forEach { unit ->
        Column {
            AdditionalInfo(
                color = color,
                icon = if (viewType == ViewType.STUDENT) WHATIcons.Person else WHATIcons.Group,
                texts = if (unionUnits) otUnits.map {
                    if (viewType == ViewType.TEACHER) it.group.name else it.teacher.name
                } else listOf(
                    if (viewType == ViewType.TEACHER) unit.group.name else unit.teacher.name
                ),
                maxLines = if (viewType == ViewType.TEACHER && !expanded) 1 else Int.MAX_VALUE,
                setExpandable = setExpandable,
                onClick = onSearchClicked?.let { callback ->
                    { str ->
                        callback(
                            if (viewType == ViewType.TEACHER) ScheduleSearch.Group(str)
                            else ScheduleSearch.Teacher(str)
                        )
                    }
                }
            )

            AdditionalInfo(
                color = color,
                icon = WHATIcons.Room,
                texts = listOf(unit.auditory)
            )

            AdditionalInfo(
                color = color,
                icon = WHATIcons.Building,
                texts = listOf(unit.building.ifEmpty { "_" })
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdditionalInfo(
    icon: ImageVector,
    texts: List<String>,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = colorScheme.secondary,
    setExpandable: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: ((String) -> Unit)? = null
) = Row(modifier = modifier, verticalAlignment = Alignment.Top) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = color
    )

    Spacer(modifier = Modifier.width(8.dp))

    FlowRow(
        maxLines = maxLines
    ) {
        texts.forEachIndexed { i, it ->
            Text(
                text = it + if (i != texts.lastIndex) ", " else "",
                color = color,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                style = typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = if (onClick != null) Modifier.clickable { onClick(it) } else Modifier
            )
        }
    }
}
