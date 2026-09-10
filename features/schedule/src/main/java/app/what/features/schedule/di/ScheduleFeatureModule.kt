package app.what.features.schedule.di

import app.what.schedule.features.schedule.domain.ScheduleController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val scheduleFeatureModule = module {
    singleOf(::ScheduleController)
}
