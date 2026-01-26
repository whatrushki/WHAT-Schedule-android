package app.what.foundation.services

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.what.foundation.core.UIComponent
import app.what.foundation.utils.delayLaunch
import kotlinx.coroutines.CoroutineScope

interface Event : UIComponent {
    val title: String
    val message: String
    val urgency: Urgency

    enum class Urgency { LOW, MEDIUM, HIGH }
}

class NotificationService<E : Event>(
    private val scope: CoroutineScope,
    private val config: Config = Mode.NORMAL,
    private val key: ((E) -> Any)? = null,
) : UIComponent {

    data class Config(
        val removeFor: Long = 700L,
        val deleteAfter: Long? = 3000L,
        val reverseLayout: Boolean = false,
        val focusOnNew: Boolean = true
    )

    companion object Mode {
        val NORMAL = Config()
        val STRONG = Config(deleteAfter = null)
    }

    private val _events = mutableStateListOf<E>()

    fun notify(event: E) {
        _events.add(0, event)

        config.deleteAfter?.let { time ->
            scope.delayLaunch(time) {
                _events.remove(event)
            }
        }
    }

    @Composable
    override fun content(modifier: Modifier) {
        val state = rememberLazyListState()

        LaunchedEffect(_events.size) {
            if (config.focusOnNew && _events.isNotEmpty()) {
                state.animateScrollToItem(0)
            }
        }

        LazyColumn(
            modifier = modifier,
            state = state,
            reverseLayout = config.reverseLayout,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items = _events, key = key ?: { it.hashCode() } ) { event ->
                Box(
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(500),
                        placementSpec = tween(500), // Анимация передвижения остальных элементов
                        fadeOutSpec = tween(500)
                    )
                ) {
                    event.content(Modifier.fillMaxWidth())
                }
            }
        }
    }
}