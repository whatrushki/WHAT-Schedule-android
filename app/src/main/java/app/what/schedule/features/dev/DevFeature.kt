package app.what.schedule.features.dev

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.foundation.ui.useState
import app.what.foundation.utils.freeze
import app.what.schedule.features.dev.presentation.FeaturePane
import app.what.schedule.features.dev.presentation.LogsPane
import app.what.schedule.features.dev.presentation.NetworksPane
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Features
import app.what.schedule.ui.theme.icons.filled.Logs
import app.what.schedule.ui.theme.icons.filled.Network
import kotlinx.coroutines.launch

enum class DevToolsTab(
    val title: String, val icon: ImageVector
) {
    LOGS("Логи", WHATIcons.Logs),
    NETWORK("Сеть", WHATIcons.Network),
    FEATURES("Фичи", WHATIcons.Features);

    companion object {
        fun all() = listOf(LOGS, NETWORK, FEATURES)
    }
}

@Composable
fun DevFeature(
    modifier: Modifier = Modifier
) = Column {
    var selectedTabIndex by useState(0)
    val devToolsTabs = DevToolsTab.all().freeze()
    val pagerState = rememberPagerState { devToolsTabs.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    SingleChoiceSegmentedButtonRow(
        space = (-4).dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        devToolsTabs.forEachIndexed { index, it ->
            val selected = selectedTabIndex == index

            SegmentTab(
                selected = selected,
                index = index,
                count = devToolsTabs.size,
                icon = it.icon,
                label = null
            ) { scope.launch { pagerState.animateScrollToPage(index) } }
        }
    }

    Gap(12)

    HorizontalPager(
        pagerState
    ) {
        when (devToolsTabs[it]) {
            DevToolsTab.LOGS -> LogsPane()
            DevToolsTab.NETWORK -> NetworksPane()
            DevToolsTab.FEATURES -> FeaturePane()
        }
    }
}