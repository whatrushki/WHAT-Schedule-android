package app.what.features.news.di

import app.what.schedule.features.news.domain.NewsController
import app.what.schedule.features.newsDetail.domain.NewsDetailController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val newsFeatureModule = module {
    singleOf(::NewsController)
    factory<NewsDetailController> { params -> NewsDetailController(params.get(), get()) }
}
