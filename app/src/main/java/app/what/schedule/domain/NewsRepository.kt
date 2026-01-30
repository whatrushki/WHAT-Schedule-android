package app.what.schedule.domain

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.orThrow
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag

class NewsRepository(
    private val db: AppDatabase,
    private val institutionManager: InstitutionManager
) {
    private val api
        get() = institutionManager.getSavedInstitution().orThrow { "No provider selected" }

    private fun getFilialId() = api.metadata.id

    suspend fun getNews(page: Int): List<NewListItem> {
        val newsTag = buildTag(LogScope.NEWS, LogCat.NET)
        Auditor.debug(newsTag, "Запрос новостей, страница: $page")
        
        val news = api.getNews(page)
        Auditor.debug(newsTag, "Получено новостей: ${news.size}")
        return news
    }

    suspend fun getNewDetail(id: String): NewItem {
        val newsTag = buildTag(LogScope.NEWS, LogCat.NET)
        Auditor.debug(newsTag, "Запрос деталей новости: $id")
        
        val newsDetail = api.getNewDetail(id)
        Auditor.debug(newsTag, "Детали новости загружены: ${newsDetail.title}")
        return newsDetail
    }
}