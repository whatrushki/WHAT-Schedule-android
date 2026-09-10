package app.what.schedule.features.main.navigation

import app.what.navigation.core.NavProvider
import app.what.navigation.core.Registry
import app.what.navigation.core.register
import app.what.schedule.features.main.MainFeature


val mainRegistry: Registry = {
    register(MainFeature::class)
}

