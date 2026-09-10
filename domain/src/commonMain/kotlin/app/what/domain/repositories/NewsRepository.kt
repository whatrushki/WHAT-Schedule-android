package app.what.domain.repositories

import app.what.domain.models.NewItem
import app.what.domain.models.NewListItem

interface NewsRepository {
    suspend fun getNews(page: Int): List<NewListItem>
    suspend fun getNewDetail(id: String): NewItem
}
