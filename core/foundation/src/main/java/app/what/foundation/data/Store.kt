package app.what.foundation.data

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

abstract class Store<T>(
    private val context: Context,
    serializer: Serializer<T>,
    fileName: String
) {
    private val Context.store by dataStore(fileName = fileName, serializer = serializer)

    fun getDataFlow(): Flow<T?> = context.store.data

    suspend fun getData(): T? = getDataFlow().firstOrNull()
    suspend fun saveData(data: T) = context.store.updateData { data }
}