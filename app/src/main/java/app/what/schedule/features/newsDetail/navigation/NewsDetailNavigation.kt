package app.what.schedule.features.newsDetail.navigation

import app.what.navigation.core.NavProvider
import app.what.navigation.core.Registry
import app.what.navigation.core.register
import app.what.schedule.features.newsDetail.NewsDetailFeature
import kotlinx.serialization.Serializable

@Serializable
data class NewsDetailProvider(
    val id: String,
    val url: String,
    val bannerUrl: String,
    val title: String,
    val description: String?
) : NavProvider()

val newsDetailRegistry: Registry = {
    register(NewsDetailFeature::class)
}

