package app.what.schedule.features.newsDetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.data.RemoteState
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.animations.rememberShimmer
import app.what.foundation.ui.animations.wiggle
import app.what.foundation.ui.bclick
import app.what.foundation.ui.capplyIf
import app.what.foundation.ui.controllers.rememberSheetController
import app.what.foundation.ui.useState
import app.what.schedule.data.remote.api.models.NewContent
import app.what.schedule.features.newsDetail.domain.models.NewsDetailEvent
import app.what.schedule.features.newsDetail.domain.models.NewsDetailState
import app.what.schedule.features.newsDetail.presentation.components.NewDetailContentShimmer
import app.what.schedule.features.newsDetail.presentation.components.NewDetailTitleShimmer
import app.what.schedule.features.newsDetail.presentation.components.NewsSharePane
import app.what.schedule.ui.components.AsyncImageWithFallback
import app.what.schedule.ui.components.Fallback
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Features
import app.what.schedule.ui.theme.icons.filled.Question
import app.what.schedule.ui.theme.icons.filled.Quote
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailView(
    state: NewsDetailState,
    listener: (NewsDetailEvent) -> Unit
) = Column(
    Modifier
        .fillMaxSize()
        .background(colorScheme.background)
        .capplyIf(state.newState != RemoteState.Loading && state.newState !is RemoteState.Error) {
            verticalScroll(rememberScrollState())
        }
) {
    val sheet = rememberSheetController()
    val shimmer = rememberShimmer(state.newState == RemoteState.Loading)
    val description = state.newDetailInfo?.description?.takeIf { !it.isEmpty() }
        ?: state.newListInfo.description?.let { buildAnnotatedString { append(it) } }

    Gap(120)
    Box {
        AsyncImageWithFallback(
            url = state.newListInfo.bannerUrl,
            enableDetailView = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(shapes.extraLarge)
        )

        Box(
            modifier = Modifier
                .padding(top = 200.dp)
                .padding(horizontal = 12.dp)
                .clip(shapes.large)
                .background(colorScheme.surfaceContainer)
        ) {
            if (state.newState == RemoteState.Loading) {
                NewDetailTitleShimmer(
                    shimmer, Modifier.padding(horizontal = 12.dp)
                )
            } else Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.newDetailInfo?.tags?.takeIf { it.isNotEmpty() }?.let {
                        it.forEach {
                            FilterChip(true, {}, { Text(it.name) })
                        }

                        Gap(8)
                    }

                    state.newDetailInfo?.timestamp?.let {
                        Text(
                            it.format(
                                DateTimeFormatter.ofPattern(
                                    "d MMMM yyyy",
                                    Locale.getDefault()
                                )
                            ),
                            color = colorScheme.secondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    state.newDetailInfo?.title ?: state.newListInfo.title,
                    color = colorScheme.onSurface,
                    fontSize = if (state.newDetailInfo?.description?.takeIf { !it.isEmpty() } != null
                        || state.newListInfo.description != null) 24.sp else 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Gap(4)

                if (description != null && state.newDetailInfo?.content?.isNotEmpty() == true) {
                    var descriptionIsExpanded by useState(false)
                    val measurer = rememberTextMeasurer()
                    val style = TextStyle(
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    )

                    Text(
                        description,
                        style = style,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = if (descriptionIsExpanded) Int.MAX_VALUE else 5,
                        modifier = Modifier.bclick(
                            measurer.measure(
                                description,
                                style = style
                            ).lineCount > 4
                        ) {
                            descriptionIsExpanded = !descriptionIsExpanded
                        }
                    )
                }

                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .clip(shapes.small)
                        .background(colorScheme.primary)
                        .bclick {
                            sheet.open { NewsSharePane(state.newListInfo.url) }
                        }
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icons.Default.Share.Show(Modifier.height(18.dp), colorScheme.onPrimary)
                        Gap(8)
                        Text(
                            "Поделиться",
                            color = colorScheme.onPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    when (state.newState) {
        is RemoteState.Error -> Fallback(
            "Произошла непредвиденная ошибка",
            Modifier.fillMaxSize(),
            "Попробовать снова" to { listener(NewsDetailEvent.OnRefresh) }
        )

        RemoteState.Loading -> NewDetailContentShimmer(shimmer, Modifier.padding(12.dp))
        RemoteState.Success -> {
            Gap(12)

            NewContentPainter(
                if (state.newDetailInfo?.content?.isNotEmpty() == true) listOf(state.newDetailInfo.content)
                else if (description != null) listOf(NewContent.Item.Text(description))
                else emptyList()
            )
        }

        else -> Unit
    }

    Gap(50)
}

@Composable
fun NewContentPainter(content: List<NewContent>) {
    val scope = rememberCoroutineScope()
    content.forEachIndexed { i, it ->
        when (it) {
            is NewContent.Container -> when (it) {
                is NewContent.Container.Card -> Box { Column { NewContentPainter(it.content) } }
                is NewContent.Container.Column -> Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) { NewContentPainter(it.content) }

                is NewContent.Container.Row -> FlowRow(
                    maxItemsInEachRow = 2,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) { NewContentPainter(it.content) }
            }

            is NewContent.Item -> AnimatedEnter(delay = 100L * i) {
                when (it) {
                    is NewContent.Item.ImageCarousel -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        var selectedImageIndex by useState(0)
                        val lazyListState = rememberLazyListState()

                        Box(
                            Modifier.padding(horizontal = 12.dp)
                        ) {
                            AsyncImageWithFallback(
                                enableDetailView = true,
                                url = it.data[selectedImageIndex],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4 / 3f, true)
                                    .clip(shapes.large)
                            )

                            val nextButtonEnabled = selectedImageIndex + 1 < it.data.size
                            val prevButtonEnabled = selectedImageIndex - 1 >= 0

                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 8.dp)
                                    .size(48.dp)
                                    .clip(shapes.medium)
                                    .border(
                                        2.dp,
                                        colorScheme.onSurface.copy(alpha = if (prevButtonEnabled) .8f else .5f),
                                        shapes.medium
                                    )
                                    .background(colorScheme.surface.copy(alpha = .2f))
                                    .bclick(prevButtonEnabled) {
                                        val index = max(selectedImageIndex - 1, 0)
                                        selectedImageIndex = index
                                        scope.launch { lazyListState.animateScrollToItem(index) }
                                    }
                            ) {
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft.Show(
                                    Modifier.padding(12.dp),
                                    color = colorScheme.onSurface.copy(alpha = if (prevButtonEnabled) 1f else .3f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 8.dp)
                                    .size(48.dp)
                                    .clip(shapes.medium)
                                    .border(
                                        2.dp,
                                        colorScheme.onSurface.copy(alpha = if (nextButtonEnabled) .8f else .5f),
                                        shapes.medium
                                    )
                                    .background(colorScheme.surface.copy(alpha = .2f))
                                    .bclick(nextButtonEnabled) {
                                        val index = min(selectedImageIndex + 1, it.data.lastIndex)
                                        selectedImageIndex = index
                                        scope.launch { lazyListState.animateScrollToItem(index) }
                                    }
                            ) {
                                Icons.AutoMirrored.Filled.KeyboardArrowRight.Show(
                                    Modifier.padding(12.dp),
                                    color = colorScheme.onSurface.copy(alpha = if (nextButtonEnabled) 1f else .3f)
                                )
                            }
                        }

                        Gap(12)

                        LazyRow(
                            state = lazyListState,
                            contentPadding = PaddingValues(12.dp, 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(it.data) { i, it ->
                                AsyncImageWithFallback(
                                    it, Modifier
                                        .height(80.dp)
                                        .aspectRatio(4 / 3f, true)
                                        .clip(shapes.small)
                                        .capplyIf(i == selectedImageIndex) {
                                            border(3.dp, colorScheme.primary, shapes.small)
                                        }
                                        .bclick {
                                            selectedImageIndex = i
                                        }
                                )
                            }
                        }
                    }

                    is NewContent.Item.Table -> TODO()
                    is NewContent.Item.Subtitle -> Text(
                        it.data,
                        color = colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    is NewContent.Item.Text -> Text(
                        it.data,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    is NewContent.Item.SimpleText -> Text(
                        it.data,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    is NewContent.Item.Image -> AsyncImageWithFallback(
                        url = it.data,
                        enableDetailView = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(horizontal = 12.dp)
                            .clip(shapes.large)
                    )

                    is NewContent.Item.SortedList -> Column(
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Gap(4)
                        it.data.forEachIndexed { i, it ->
                            Row {
                                Text(
                                    "${i + 1}.",
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.tertiary
                                )
                                Gap(8)
                                Text(
                                    it,
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    color = colorScheme.secondary
                                )
                            }
                            Gap(4)
                        }
                    }

                    is NewContent.Item.UnsortedList -> Column(
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Gap(4)

                        it.data.forEach {
                            Row {
                                WHATIcons.Features.Show(
                                    Modifier.size(20.dp),
                                    color = colorScheme.tertiary
                                )

                                Gap(4)
                                Text(
                                    it,
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    color = colorScheme.secondary
                                )
                            }

                            Gap(4)
                        }
                    }

                    is NewContent.Item.Info -> Box(
                        Modifier
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 12.dp)
                            .clip(shapes.medium)
                            .background(colorScheme.tertiary.copy(alpha = .3f))
                    ) {
                        Spacer(
                            Modifier
                                .align(Alignment.CenterStart)
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(colorScheme.tertiary)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            WHATIcons.Question.Show(
                                Modifier
                                    .wiggle(15f)
                                    .size(28.dp),
                                colorScheme.tertiary
                            )
                            Gap(16)
                            Text(
                                it.data,
                                color = colorScheme.tertiary,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                            )
                        }
                    }

                    is NewContent.Item.Quote -> Box(
                        Modifier
                            .padding(horizontal = 12.dp)
                            .clip(shapes.medium)
                            .background(colorScheme.surfaceContainer)
                    ) {
                        Column(
                            Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AsyncImageWithFallback(
                                    it.author.avatarUrl,
                                    Modifier
                                        .clip(shapes.medium)
                                        .height(120.dp)
                                        .aspectRatio(3 / 4f, true)
                                )

                                WHATIcons.Quote.Show(
                                    Modifier
                                        .wiggle(15f)
                                        .size(48.dp),
                                    colorScheme.tertiary
                                )
                            }

                            Gap(18)

                            Text(
                                it.data,
                                color = colorScheme.onSurface,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Gap(20)

                            Column {
                                Text(
                                    it.author.name,
                                    color = colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    it.author.role,
                                    color = colorScheme.secondary,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}