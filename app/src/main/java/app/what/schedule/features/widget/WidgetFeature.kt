package app.what.schedule.features.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.what.schedule.R
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.utils.formatTime
import app.what.schedule.domain.ScheduleRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

private const val DAY_INDEX_KEY = "day_index"
private val MAX_PAGE_INDEX_KEY = ActionParameters.Key<Int>("max_index")

class ScheduleWidget : GlanceAppWidget(), KoinComponent {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("d", "action updated")
        val scheduleRepository: ScheduleRepository by inject()
        val appValues: AppValues by inject()

        val schedule = withContext(IO) {
            val (groupName, groupId) = appValues.lastSearchedGroup.get()
                ?: return@withContext emptyList()
            scheduleRepository.getSchedule(ScheduleSearch.Group(groupName, groupId), true)
        }

        provideContent {
            val currentDayIndex = currentState(intPreferencesKey(DAY_INDEX_KEY)) ?: 0
            Log.d("d", "Current day index: $currentDayIndex")
            Log.d("d", "action  updated, index: $currentDayIndex")
            WidgetContent(schedule, currentDayIndex)
        }
    }

    private fun getCurrentDayIndex(context: Context, glanceId: GlanceId): Int {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("day_index_${glanceId.hashCode()}", 0)
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun WidgetContent(
    schedule: List<DaySchedule>,
    currentDayIndex: Int
) {
    val safeIndex = currentDayIndex.coerceIn(0, schedule.size - 1)
    val currentDay = schedule[safeIndex]

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = GlanceModifier.defaultWeight(),
                text = when (currentDayIndex) {
                    0 -> "Сегодня"
                    1 -> "Завтра"
                    2 -> "Послезавтра"
                    else -> "${currentDay.date.dayOfMonth} " + currentDay.date.dayOfWeek.getDisplayName(
                        java.time.format.TextStyle.FULL_STANDALONE,
                        Locale.getDefault()
                    )
                },
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Row {
                val maxIndexParameter = actionParametersOf(
                    MAX_PAGE_INDEX_KEY to schedule.size.minus(1)
                )

                IconButton(
                    "<",
                    actionRunCallback<PrevDayActionCallback>(maxIndexParameter),
                    safeIndex > 0
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                IconButton(
                    ">",
                    actionRunCallback<NextDayActionCallback>(maxIndexParameter),
                    safeIndex < schedule.size - 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        if (currentDay.lessons.isEmpty()) {
            Image(ImageProvider(R.drawable.il_totoro_friends), contentDescription = "No lessons")
            Text("Здесь пусто...")
        } else LazyColumn(
            GlanceModifier.cornerRadius(12.dp)
        ) {
            items(currentDay.lessons) {
                Column {
                    LessonCard(it)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun LessonCard(
    lesson: Lesson
) = Column(
    modifier = GlanceModifier
        .fillMaxWidth()
        .background(GlanceTheme.colors.secondaryContainer)
        .cornerRadius(12.dp)
        .padding(16.dp)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier.fillMaxWidth()
    ) {
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .cornerRadius(100.dp)
                .background(GlanceTheme.colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = lesson.number.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = formatTime(lesson.startTime),
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = "- ${formatTime(lesson.endTime)}",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 16.sp
            )
        )
    }

    Spacer(modifier = GlanceModifier.height(8.dp))

    // Информация
    Column {
        Text(
            text = lesson.subject,
            style = TextStyle(
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        OtUnitValueView("👔", lesson.otUnits.first().teacher.name)
        Spacer(modifier = GlanceModifier.height(4.dp))
        OtUnitValueView("🚪", lesson.otUnits.first().auditory)
        Spacer(modifier = GlanceModifier.height(4.dp))
        OtUnitValueView("🏢", lesson.otUnits.first().building)
    }
}

@Composable
fun OtUnitValueView(
    icon: String,
    text: String
) = Text(
    text = "$icon $text",
    style = TextStyle(
        color = GlanceTheme.colors.secondary,
        fontSize = 14.sp
    ),
    maxLines = 1
)

@SuppressLint("RestrictedApi")
@Composable
fun IconButton(
    icon: String,
    action: Action,
    enabled: Boolean = true
) {
    Box(
        modifier = GlanceModifier
            .size(32.dp)
            .cornerRadius(100.dp)
            .background(GlanceTheme.colors.secondaryContainer)
            .let {
                if (enabled) it.clickable(action)
                else it
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = TextStyle(
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 14.sp
            )
        )
    }
}

// Receiver для виджета
class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}

class PrevDayActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("d", "PrevDayActionCallback called")

        updateAppWidgetState(context, glanceId) { prefs ->
            val currentIndex = prefs[intPreferencesKey(DAY_INDEX_KEY)] ?: 0
            prefs[intPreferencesKey(DAY_INDEX_KEY)] = currentIndex.minus(1)
                .coerceAtLeast(0)
        }

        ScheduleWidget().update(context, glanceId)
    }
}

class NextDayActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("d", "NextDayActionCallback called")

        updateAppWidgetState(context, glanceId) { prefs ->
            val currentIndex = prefs[intPreferencesKey(DAY_INDEX_KEY)] ?: 0
            prefs[intPreferencesKey(DAY_INDEX_KEY)] = currentIndex.plus(1)
                .coerceAtMost(parameters[MAX_PAGE_INDEX_KEY]!!)
        }

        ScheduleWidget().update(context, glanceId)
    }
}