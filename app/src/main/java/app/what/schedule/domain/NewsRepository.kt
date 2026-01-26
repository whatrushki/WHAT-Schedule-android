package app.what.schedule.domain

import app.what.foundation.utils.orThrow
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem

class NewsRepository(
    private val db: AppDatabase,
    private val institutionManager: InstitutionManager
) {
    private val api get() = institutionManager.getSavedProvider().orThrow { "No provider selected" }
    private fun getFilialId() = institutionManager.getSavedFilial()!!.metadata.id

    suspend fun getNews(page: Int): List<NewListItem> {
        return api.getNews(page)
    }

    suspend fun getNewDetail(id: String): NewItem {
        return api.getNewDetail(id)
    }
}