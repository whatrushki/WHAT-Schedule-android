package app.what.schedule.data.local.settings

import android.content.Context
import app.what.foundation.data.PreferenceStorage
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.Teacher
import app.what.schedule.features.settings.presentation.ThemeType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
enum class AppServers {
    TURTLE, RKSI
}

@Serializable
class ScheduleProvider(
    val inst: String,
    val filial: String,
    val provider: String
)

class AppValues(context: Context) {
    private val prefs = context.getSharedPreferences("MY_APP_PREFERENCES", Context.MODE_PRIVATE)
    private val preferencesFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val storage = PreferenceStorage(prefs, preferencesFlow)

    init {
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            key?.let { preferencesFlow.tryEmit(it) }
        }
    }

    val isFirstLaunch = storage.createValue("is_first_launch", true, Boolean.serializer())
    val lastSearch = storage.createValue("last_search", null, ScheduleSearch.serializer())

    val institution = storage.createValue("institution", null, ScheduleProvider.serializer())
    val themeType = storage.createValue("theme_type", ThemeType.System, ThemeType.serializer())
    val devFeaturesEnabled = storage.createValue("dev_features_enabled", false, Boolean.serializer())
}

