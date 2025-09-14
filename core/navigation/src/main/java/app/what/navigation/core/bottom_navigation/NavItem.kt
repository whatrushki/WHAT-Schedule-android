package app.what.navigation.core.bottom_navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import app.what.navigation.core.NavProvider

class NavAction(
    val name: String,
    val icon: ImageVector,
    val block: () -> Unit
)

abstract class NavItem(
    val name: String,
    val icon: ImageVector,
    val provider: NavProvider
) {
    abstract fun selected(destination: NavDestination): Boolean
}

inline fun <reified P : NavProvider> navItem(
    name: String,
    icon: ImageVector,
    provider: P
) = object : NavItem(name, icon, provider) {
    override fun selected(destination: NavDestination) = destination.hasRoute<P>()
}

@Composable
fun NavigationItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimary
        else colorScheme.onSurfaceVariant,
        label = "contentColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimary
        else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        label = "iconTint"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(
                indication = null, // Убираем ripple эффект
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.name,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        // Анимация для выбранного элемента
        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(0.5f, 0.5f),
                            radius = 0.8f
                        )
                    )
            )
        }
    }
}
