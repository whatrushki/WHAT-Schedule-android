package app.what.schedule.features.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import androidx.glance.appwidget.state.getAppWidgetState
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
import androidx.glance.material3.ColorProviders
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.R
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.local.settings.ThemeStyle
import app.what.schedule.data.local.settings.ThemeType
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.utils.formatTime
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.utils.GlanceUtils.isSystemInDarkTheme
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.materialkolor.ktx.DynamicScheme
import com.materialkolor.toColorScheme
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}

private const val DAY_INDEX_KEY = "day_index"
internal const val SEARCH_KEY = "search"
private val MAX_PAGE_INDEX_KEY = ActionParameters.Key<Int>("max_index")

class ScheduleWidget : GlanceAppWidget(), KoinComponent {
    private val settings: AppValues by inject()
    private val scheduleRepository: ScheduleRepository by inject()
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val themeType = settings.themeType.get()
        val themeStyle = settings.themeStyle.get()
        val themeColor = settings.themeColor.get()

        val prefs = getAppWidgetState(context, stateDefinition, id) as Preferences
        val search = prefs[stringPreferencesKey(SEARCH_KEY)]
        val schedule = if (search == null) ScheduleResponse.Empty
        else withContext(IO) {
            scheduleRepository.getSchedule(
                Json.decodeFromString(search),
                useCache = true,
                requiresData = true
            )
        }

        provideContent {
            val isDarkTheme = when (themeType) {
                ThemeType.Dark -> true
                ThemeType.System -> LocalContext.current.isSystemInDarkTheme()
                else -> false
            }

            val theme = ColorProviders(
                when (themeStyle) {
                    ThemeStyle.CustomColor -> DynamicScheme(Color(themeColor!!), isDarkTheme)
                    else -> DynamicScheme(Color(0xFF94FF28), isDarkTheme)
                }.toColorScheme()
            )

            GlanceTheme(theme) {
                val currentDayIndex = currentState(intPreferencesKey(DAY_INDEX_KEY)) ?: 0
                when (schedule) {
                    is ScheduleResponse.Available -> WidgetContent(
                        schedule.schedules,
                        currentDayIndex
                    )

                    else -> Unit
                }

            }
        }
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
            items(currentDay.lessons.sortedBy { it.startTime }) {
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
        .background(
            GlanceTheme.colors.secondaryContainer.let {
                if (lesson.state != LessonState.REMOVED) it
                else ColorProvider(it.getColor(LocalContext.current).copy(alpha = .8f))

            }
        )
        .cornerRadius(12.dp)
        .padding(16.dp)
) {

    val primaryColor =
        if (lesson.state == LessonState.REMOVED) GlanceTheme.colors.secondaryContainer
        else if (lesson.type.isNonStandard) GlanceTheme.colors.tertiary
        else GlanceTheme.colors.primary

    val onPrimaryColor =
        if (lesson.state == LessonState.REMOVED) GlanceTheme.colors.onSecondaryContainer
        else if (lesson.type.isNonStandard) GlanceTheme.colors.onTertiary
        else GlanceTheme.colors.onPrimary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier.fillMaxWidth()
    ) {
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .cornerRadius(100.dp)
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (lesson.number != 0) lesson.number.toString() else "*",
                style = TextStyle(
                    color = onPrimaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = formatTime(lesson.startTime),
            style = TextStyle(
                color = primaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = "- ${formatTime(lesson.endTime)}",
            style = TextStyle(
                color = primaryColor,
                fontSize = 16.sp
            )
        )
    }

    Spacer(modifier = GlanceModifier.height(8.dp))

    // Информация
    Column {
        Text(
            text = lesson.subject.ifEmpty { "Замена" },
            style = TextStyle(
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Row(
            GlanceModifier.fillMaxWidth()
        ) {
            lesson.otUnits.forEach {
                Column {
                    OtUnitValueView("👔", it.teacher.name)
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    OtUnitValueView("🚪", it.auditory)
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    OtUnitValueView("🏢", it.building)
                }
                Spacer(modifier = GlanceModifier.width(4.dp))
            }
        }
    }
}

@Composable
fun OtUnitValueView(
    icon: String,
    text: String
) = Text(
    text = "$icon  $text",
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

class PrevDayActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val widgetTag = buildTag(LogScope.WIDGET, LogCat.UI)
        Auditor.debug(widgetTag, "Переключение на предыдущий день в виджете")

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
        val widgetTag = buildTag(LogScope.WIDGET, LogCat.UI)
        Auditor.debug(widgetTag, "Переключение на следующий день в виджете")

        updateAppWidgetState(context, glanceId) { prefs ->
            val currentIndex = prefs[intPreferencesKey(DAY_INDEX_KEY)] ?: 0
            prefs[intPreferencesKey(DAY_INDEX_KEY)] = currentIndex.plus(1)
                .coerceAtMost(parameters[MAX_PAGE_INDEX_KEY]!!)
        }

        ScheduleWidget().update(context, glanceId)
    }
}