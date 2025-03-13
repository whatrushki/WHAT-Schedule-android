package app.what.navigation.core.bottom_navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import app.what.navigation.core.NavProvider

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
fun RowScope.NavigationItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.secondary

    NavigationBarItem(
        modifier = Modifier.size(26.dp),
        selected = selected,
        onClick = { if (!selected) onClick() },
        label = {
            Text(text = item.name, color = accentColor)
        },
        icon = {
            Icon(
                item.icon,
                contentDescription = null,
                tint = accentColor
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    )
}