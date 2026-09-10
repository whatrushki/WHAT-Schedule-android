package app.what.foundation.services

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.what.foundation.core.UIComponent
import app.what.foundation.ui.Gap
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

    fun remove(event: E) {
        _events.remove(event)
    }

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
                        placementSpec = tween(500),
                        fadeOutSpec = tween(500)
                    )
                ) {
                    event.content(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

data class AppNotification(
    override val title: String,
    override val message: String,
    override val urgency: Event.Urgency = Event.Urgency.HIGH,
    val onDismiss: (() -> Unit)? = null
) : Event {
    @Composable
    override fun content(modifier: Modifier) {
        val (bgColor, textColor, icon) = when (urgency) {
            Event.Urgency.HIGH -> Triple(colorScheme.errorContainer, colorScheme.onErrorContainer, Icons.Default.Warning)
            Event.Urgency.MEDIUM -> Triple(colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer, Icons.Default.Info)
            Event.Urgency.LOW -> Triple(colorScheme.surfaceContainerHigh, colorScheme.onSurface, Icons.Default.Notifications)
        }

        Surface(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
                Gap(12)
                Column(modifier = Modifier.weight(1f)) {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    if (message.isNotBlank()) {
                        Text(
                            text = message,
                            style = typography.bodySmall,
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }
                }
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

val LocalNotificationService = staticCompositionLocalOf<NotificationService<AppNotification>?> { null }

@Composable
fun rememberAppNotificationService(scope: CoroutineScope = rememberCoroutineScope()): NotificationService<AppNotification> {
    return remember(scope) {
        NotificationService<AppNotification>(
            scope = scope,
            config = NotificationService.Config(deleteAfter = 4000L)
        )
    }
}