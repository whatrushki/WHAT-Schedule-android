package app.what.navigation.core.bottom_navigation

import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.navigation.core.Navigator
import app.what.navigation.core.rememberNavigator
import kotlin.math.roundToInt

inline val Int.dp: Dp
    @Composable get() = with(LocalDensity.current) { this@dp.toDp() }

inline val Dp.px: Int
    @Composable get() = with(LocalDensity.current) { this@px.toPx().roundToInt() }

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    navigator: Navigator = rememberNavigator(),
    screens: Iterable<NavItem>,
    action: (NavDestination?) -> NavAction?
) {
    var currentDestination by remember { mutableStateOf(navigator.c.currentDestination) }
    val action = remember(currentDestination) { action(currentDestination) }

    val selectedIndex = screens.indexOfFirst {
        currentDestination != null && it.selected(currentDestination!!)
    }

    val containerHeight = 68.dp
    val containerPaddings = 8.dp
    val spacerBetween = 10.dp

    LaunchedEffect(Unit) {
        navigator.c.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .height(containerHeight)
                .clip(CircleShape)
                .background(colorScheme.surfaceContainer)
                .padding(containerPaddings),
        ) {
            // Анимированный индикатор выбранного элемента
            if (selectedIndex >= 0) {
                val buttonWidth = (containerHeight - containerPaddings * 2).px
                val indicatorOffset by animateIntAsState(
                    targetValue = selectedIndex * (buttonWidth + spacerBetween.px),
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = EaseOutExpo
                    ),
                    label = "indicatorOffset"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .offset { IntOffset(indicatorOffset, 0) }
                        .background(colorScheme.primary, CircleShape)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacerBetween)
            ) {
                screens.forEachIndexed { index, item ->
                    NavigationItem(
                        item = item,
                        selected = selectedIndex == index,
                        onClick = {
                            navigator.c.navigate(item.provider) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        action ?: return

        Gap(10)

        AnimatedEnter {
            ExtendedFloatingActionButton(
                containerColor = colorScheme.primaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                onClick = action.block
            ) {
                action.icon.Show(
                    color = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}