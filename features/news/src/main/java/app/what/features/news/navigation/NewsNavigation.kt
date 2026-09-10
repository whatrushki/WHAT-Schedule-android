package app.what.schedule.features.news.navigation

import app.what.navigation.core.NavProvider
import app.what.navigation.core.Registry
import app.what.navigation.core.register
import app.what.schedule.features.news.NewsFeature
import kotlinx.serialization.Serializable

@Serializable
object NewsProvider : NavProvider()

val newsRegistry: Registry = {
    register(NewsFeature::class)
}

