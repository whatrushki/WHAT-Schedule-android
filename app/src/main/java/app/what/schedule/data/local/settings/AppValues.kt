package app.what.schedule.data.local.settings

import android.content.Context
import app.what.foundation.data.PreferenceStorage
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.Teacher
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
    val lastSearchedGroup = storage.createValue("last_searched_group", null, Group.serializer())
    val lastSearchedTeacher =
        storage.createValue("last_searched_teacher", null, Teacher.serializer())
    val usedServer = storage.createValue("used_server", null, AppServers.serializer())
    val institution = storage.createValue("institution", null, ScheduleProvider.serializer())
}

