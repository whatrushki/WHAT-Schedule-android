package app.what.navigation.core.bottom_navigation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import app.what.navigation.core.Navigator
import app.what.navigation.core.rememberNavigator


@Composable
fun BottomNavBar(
    navigator: Navigator = rememberNavigator(),
    screens: Iterable<NavItem>
) = NavigationBar(
    modifier = Modifier
        .shadow(
            10.dp,
            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        )
) {
    var currentDestination by remember { mutableStateOf(navigator.c.currentDestination) }

    LaunchedEffect(Unit) {
        navigator.c.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination
        }
    }

    screens.forEach { item ->
        NavigationItem(
            item = item,
            selected = currentDestination != null && item.selected(currentDestination!!),
            onClick = {
                navigator.c.navigate(item.provider) {
                    launchSingleTop = true
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
