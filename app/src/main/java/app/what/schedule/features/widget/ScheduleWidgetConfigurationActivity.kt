package app.what.schedule.features.widget

import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import app.what.foundation.ui.Gap
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.useState
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.local.settings.ProvideGLobalAppValues
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.data.remote.api.models.toScheduleSearch
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.ui.components.ScheduleSearchData
import app.what.schedule.ui.components.ScheduleSearchPane
import app.what.schedule.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

class ScheduleWidgetConfigurationActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем ID виджета
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            // НЕ ПЕРЕМЕЩАТЬ!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setNavigationBarContrastEnforced(false)
            }

            val settings = koinInject<AppValues>()
            val scheduleRepository = koinInject<ScheduleRepository>()
            val scope = rememberCoroutineScope()
            var searchItems by useState<List<ScheduleSearch>>(emptyList())
            val search by useState(settings.lastSearch.get())
            val searchData = remember(searchItems, search) {
                mutableStateOf(
                    object : ScheduleSearchData {
                        override val scheduleSearches = searchItems
                        override val selectedSearch = search
                    }
                )
            }

            ProvideGLobalAppValues(settings) {
                AppTheme {
                    LaunchedEffect(Unit) {
                        scope.launch(IO) {
                            val ut =
                                async {
                                    scheduleRepository.getTeachers().map { it.toScheduleSearch() }
                                }
                            val ug =
                                async {
                                    scheduleRepository.getGroups().map { it.toScheduleSearch() }
                                }
                            searchItems = awaitAll(ut, ug).flatten()
                        }
                    }

                    WidgetConfigurationScreen(
                        appWidgetId = appWidgetId,
                        searchData = searchData
                    )
                }
            }
        }
    }


    @Composable
    private fun WidgetConfigurationScreen(
        appWidgetId: Int,
        searchData: State<ScheduleSearchData>
    ) = Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        AnimatedEnter(
            Modifier
                .zIndex(2f)
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 16.dp)
        ) {

            ExtendedFloatingActionButton(
                onClick = {
                    saveWidgetConfiguration(appWidgetId, searchData.value.selectedSearch!!)
                }
            ) {
                Text("Выбрать")
            }

        }

        Column {
            Box(
                Modifier
                    .animateContentSize()
                    .height(200.dp)
            ) {
                AnimatedEnter(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "Настройка виджета расписания",
                        style = typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 46.sp,
                        color = colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 18.dp, end = 16.dp)
                    )
                }
            }

            Gap(24)

            // Список для выбора
            ScheduleSearchPane(
                searchData,
                { search -> searchData.value.selectedSearch!! == search },
                { /* Обработка долгого нажатия */ }
            )
        }
    }

    private fun saveWidgetConfiguration(appWidgetId: Int, search: ScheduleSearch) =
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)

            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                prefs[stringPreferencesKey("search")] = Json.encodeToString(search)
            }

            ScheduleWidget().update(this@ScheduleWidgetConfigurationActivity, glanceId)

            setResult(RESULT_OK)
            finish()
        }
}