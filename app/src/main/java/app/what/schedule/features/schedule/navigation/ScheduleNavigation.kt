package app.what.schedule.features.schedule.navigation

import app.what.navigation.core.NavProvider
import app.what.navigation.core.Registry
import app.what.navigation.core.register
import app.what.schedule.features.schedule.ScheduleFeature
import kotlinx.serialization.Serializable

@Serializable
object ScheduleProvider : NavProvider()

val scheduleRegistry: Registry = {
    register(ScheduleFeature::class)
}

