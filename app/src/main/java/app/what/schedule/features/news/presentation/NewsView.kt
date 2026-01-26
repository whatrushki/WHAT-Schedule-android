package app.what.schedule.features.news.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.data.RemoteState
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useState
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.features.news.domain.models.NewsEvent
import app.what.schedule.features.news.domain.models.NewsState
import app.what.schedule.ui.components.AsyncImageWithFallback
import app.what.schedule.ui.components.Fallback
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsView(
    state: NewsState,
    listener: (NewsEvent) -> Unit
) = PullToRefreshBox(
    isRefreshing = state.newsState == RemoteState.Loading,
    onRefresh = { listener(NewsEvent.OnRefresh) },
) {
    var selectedNewId by useState<String?>(null)
    val lazyListState = rememberLazyListState()


    LaunchedEffect(lazyListState.canScrollForward) {
        Auditor.debug("d", "on end ${!lazyListState.canScrollForward}")
        if (!lazyListState.canScrollForward && state.newsState != RemoteState.Loading) listener(
            NewsEvent.OnListEndingScrolled
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                Modifier
                    .animateContentSize()
                    .height(116.dp)
            )
        }

        item {
            Text(
                "Новости",
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
                color = colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        when (state.newsState) {
            is RemoteState.Error -> item {
                Fallback(
                    "Произошла непредвиденная ошибка",
                    Modifier.fillMaxSize(),
                    "Попробовать снова" to { listener(NewsEvent.OnRefresh) }
                )
            }

            RemoteState.Success -> items(state.news, key = { it.id }) {
                NewListItemView(Modifier.animateItem(), selectedNewId == it.id, it, {
                    selectedNewId = if (selectedNewId != it.id) it.id else null
                }) {
                    listener(NewsEvent.OnNewEnterClicked(it))
                }
            }


            else -> {}
        }
    }
}

@Composable
fun NewListItemView(
    modifier: Modifier,
    selected: Boolean,
    item: NewListItem,
    onClick: () -> Unit,
    onSelect: () -> Unit
) = Box(
    modifier
        .fillMaxWidth()
        .clip(shapes.large)
        .background(colorScheme.surfaceBright)
        .bclick(block = onClick)
) {
    Column(
        modifier = Modifier
            .padding(12.dp, 14.dp, 12.dp, 12.dp)
            .animateContentSize()
    ) {
        AsyncImageWithFallback(
            item.bannerUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(shapes.large)
        )

        Gap(8)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                item.timestamp.format(
                    DateTimeFormatter.ofPattern(
                        "d MMMM yyyy",
                        Locale.getDefault()
                    )
                ),
                color = colorScheme.secondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Row {
                item.tags.forEach {
                    FilterChip(true, {}, label = {
                        Text(it.name)
                    })
                }
            }
        }

        Gap(4)

        Text(
            item.title,
            color = colorScheme.onSurface,
            fontSize = if (item.description?.isNotBlank() == true) 22.sp else 18.sp,
            fontWeight = FontWeight.SemiBold,
        )

        if (item.description?.isNotBlank() == true) {
            Gap(4)

            Auditor.debug("d", item.description)
            Text(
                item.description.trim(),
                color = colorScheme.onSurfaceVariant,
                maxLines = if (selected) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                lineHeight = 18.sp
            )
        }

        if (selected) {
            Gap(12)

            Button(onSelect) {
                Text("Перейти")
            }
        }
    }
}