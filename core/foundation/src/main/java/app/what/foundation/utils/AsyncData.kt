package app.what.foundation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface Resource<out T> {
    data object Idle : Resource<Nothing>   // был Pending
    data object Loading : Resource<Nothing>
    data class Success<out T>(val value: T) : Resource<T>
    data class Failure(val error: Throwable? = null, val message: String? = null) :
        Resource<Nothing>

    val isIdle: Boolean get() = this is Idle
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value
    fun exceptionOrNull(): Throwable? = (this as? Failure)?.error
}

fun <T : Any?> UIController<out Any, out Any, out Any>.asyncResource(
    isPaged: Boolean = false,
    block: suspend (params: Map<String, String>) -> T
) = AsyncResource(viewModelScope,  isPaged) { params, _, _ -> block(params) }

fun <T : Any?> UIController<Any, Any, Any>.asyncPagingResource(
    isPaged: Boolean = false,
    block: suspend (params: Map<String, String>, page: Int?, pageSize: Int?) -> T
) = AsyncResource(viewModelScope, isPaged, fetcher = block)

class AsyncResource<T>(
    private val scope: CoroutineScope,
    private val isPaged: Boolean = false,
    private val pageSize: Int = 50,
    private val fetcher: suspend (params: Map<String, String>, page: Int?, pageSize: Int?) -> T
) {
    private val _resource = MutableStateFlow<Resource<T>>(Resource.Idle)

    fun get() = _resource.value

    @Composable
    fun collect() = _resource.asStateFlow().collectAsState()

    private var currentPage = 1
    private var hasMorePages = true
    private var _isLoadingMore = false

    private var lastParams: Map<String, String>? = null

    fun refresh(params: Map<String, String> = emptyMap()) {
        scope.launchIO {
            _resource.value = Resource.Loading
            currentPage = 1
            hasMorePages = true

            loadInternal(params, refresh = true)
        }
    }

    fun loadNextPage() {
        if (!isPaged || _isLoadingMore || !hasMorePages || _resource.value.isLoading) return

        scope.launchIO {
            _isLoadingMore = true
            loadInternal(lastParams ?: emptyMap(), refresh = false)
            _isLoadingMore = false
        }
    }

    fun retry() {
        if (_resource.value.isFailure) {
            if (currentPage == 1) refresh(lastParams ?: emptyMap()) else loadNextPage()
        }
    }

    private suspend fun loadInternal(params: Map<String, String>, refresh: Boolean) {
        try {
            val pageParam = if (isPaged) currentPage else null
            val sizeParam = if (isPaged) pageSize else null

            @Suppress("UNCHECKED_CAST")
            val result = fetcher(params, pageParam, sizeParam) as Any

            val newResource = when {
                refresh -> Resource.Success(result)
                else -> {
                    // предполагаем, что T = List<Something>
                    val currentList = (_resource.value.getOrNull() as? List<Any?>)?.toMutableList()
                        ?: mutableListOf()
                    @Suppress("UNCHECKED_CAST")
                    currentList.addAll(result as List<Any?>)
                    Resource.Success(currentList as T)
                }
            }

            _resource.value = newResource as Resource<T>

            if (isPaged) {
                @Suppress("UNCHECKED_CAST")
                val added = (result as? List<*>)?.size ?: 0
                if (added < pageSize) hasMorePages = false
                else currentPage++
            }

        } catch (e: Throwable) {
            _resource.value = Resource.Failure(e, e.message ?: "Ошибка загрузки")
        }
    }

    val isLoadingMore get() = _isLoadingMore
    val hasMore get() = isPaged && hasMorePages && !_resource.value.isLoading
}