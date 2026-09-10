package app.what.data.repositories

import app.what.domain.models.NewItem
import app.what.domain.models.NewListItem
import app.what.domain.repositories.NewsRepository
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.LogCat
import app.what.foundation.utils.LogScope
import app.what.foundation.utils.buildTag
import app.what.foundation.utils.orThrow
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.remote.api.InstitutionManager

class NewsRepositoryImpl(
    private val db: AppDatabase,
    private val institutionManager: InstitutionManager
) : NewsRepository {
    private val api
        get() = institutionManager.getSavedInstitution().orThrow { "No provider selected" }

    override suspend fun getNews(page: Int): List<NewListItem> {
        val newsTag = buildTag(LogScope.NEWS, LogCat.NET)
        Auditor.debug(newsTag, "Запрос новостей, страница: $page")

        val news = api.newsService.getNews(page)
        Auditor.debug(newsTag, "Получено новостей: ${news.size}")
        return news
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val newsTag = buildTag(LogScope.NEWS, LogCat.NET)
        Auditor.debug(newsTag, "Запрос деталей новости: $id")

        val newsDetail = api.newsService.getNewDetail(id)
        Auditor.debug(newsTag, "Детали новости загружены: ${newsDetail.title}")
        return newsDetail
    }
}