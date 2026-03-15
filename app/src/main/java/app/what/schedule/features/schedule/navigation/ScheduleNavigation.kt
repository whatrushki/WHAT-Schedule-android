package app.what.schedule.features.schedule.navigation

import app.what.navigation.core.NavProvider
import app.what.navigation.core.Registry
import app.what.navigation.core.register
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.features.schedule.ScheduleFeature
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleProvider(
    val searchName: String? = null,
    val searchId: String? = null,
    val isGroup: Boolean = true
) : NavProvider() {
    val search
        get() = if (searchId != null && searchName != null) {
            if (isGroup) ScheduleSearch.Group(searchName, searchId)
            else ScheduleSearch.Teacher(searchName, searchId)
        } else null
}

val scheduleRegistry: Registry = {
    register(ScheduleFeature::class)
}

