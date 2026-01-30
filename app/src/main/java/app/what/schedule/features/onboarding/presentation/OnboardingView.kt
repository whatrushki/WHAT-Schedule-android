package app.what.schedule.features.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.what.foundation.data.settings.types.asSheet
import app.what.foundation.data.settings.types.asSwitch
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick
import app.what.schedule.data.local.settings.rememberAppValues
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.features.onboarding.domain.models.OnboardingEvent
import app.what.schedule.features.onboarding.domain.models.OnboardingState
import app.what.schedule.ui.components.PolicyView
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Crown
import app.what.schedule.ui.theme.icons.filled.Support
import app.what.schedule.utils.Analytics
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// OnboardingView.kt

@Composable
fun OnboardingView(
    state: OnboardingState,
    listener: (OnboardingEvent) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    // Анимированный фон (градиенты меняются в зависимости от страницы)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        OnboardingBackground(pagerState.currentPage)

        Column(modifier = Modifier.fillMaxSize()) {
            // Контент Пейджера
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = true
            ) { page ->
                // Эффект "сжатия" при скролле
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val scale = 1f - (0.1f * pageOffset.absoluteValue).coerceIn(0f, 0.2f)
                val alpha = 1f - (0.5f * pageOffset.absoluteValue).coerceIn(0f, 0.5f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> IntroPage()
                        1 -> LegalAndAnalyticsPage()
                        2 -> InstitutionSelectionPage(state, listener)
                    }
                }
            }

            // Навигация (Индикаторы и Кнопки)
            OnboardingNavigation(
                pagesCount = 3,
                pagerState = pagerState,
                canFinish = state.canFinish,
                onNext = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                onFinish = {
                    Analytics.logUniversitySelect(state.selectedInstitutionId ?: "not seleted")
                    listener(OnboardingEvent.FinishOnboarding)
                }
            )
        }
    }
}

@Composable
fun IntroPage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF94FF28).copy(0.3f), Color.Transparent)
                    )
                )
            }
            Icon(
                imageVector = WHATIcons.Crown,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = colorScheme.primary
            )
        }

        Gap(32)

        Text(
            text = "WHAT Schedule",
            style = typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        Gap(16)
        Text(
            text = "Твое идеальное расписание.\nБыстро. Удобно. Красиво.",
            style = typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = colorScheme.secondary
        )
    }
}

@Composable
fun LegalAndAnalyticsPage() {
    val app = rememberAppValues()
    val fb = Firebase.analytics

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WHATIcons.Support.Show(
            modifier = Modifier.size(64.dp),
            color = colorScheme.primary
        )

        Gap(24)
        Text(
            "Приватность и Данные",
            style = typography.headlineSmall,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Gap(8)
        Text(
            "Мы уважаем ваши данные. Настройте, чем вы хотите делиться.",
            textAlign = TextAlign.Center,
            color = colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Gap(32)

        app.isAnalyticsEnabled.asSwitch {
            fb.setAnalyticsCollectionEnabled(it)
        }.content(Modifier)

        app.thePolicy.asSheet { _, _ ->
            PolicyView()
        }.content(Modifier)
    }
}

@Composable
fun InstitutionSelectionPage(
    state: OnboardingState,
    listener: (OnboardingEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Где вы учитесь?",
            style = typography.headlineSmall,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Это необходимо для загрузки расписания",
            style = typography.bodyMedium,
            color = colorScheme.secondary
        )

        Gap(24)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.institutions) { inst ->
                InstitutionCard(
                    institution = inst,
                    isSelected = state.selectedInstitutionId == inst.metadata.id,
                    onClick = { listener(OnboardingEvent.SelectInstitution(inst.metadata.id)) }
                )
            }
        }
    }
}

@Composable
fun InstitutionCard(
    institution: Institution.Factory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(
            0.2f
        )
    )
    val containerColor by animateColorAsState(
        if (isSelected) colorScheme.primaryContainer.copy(0.3f) else colorScheme.surfaceContainer
    )

    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium)
            .bclick(block = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colorScheme.primary.copy(0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                institution.metadata.name.take(1),
                style = typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
        }
        Gap(12)
        Text(
            institution.metadata.name,
            style = typography.titleMedium,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            institution.metadata.fullName,
            style = typography.labelSmall,
            color = colorScheme.secondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun OnboardingNavigation(
    pagerState: PagerState,
    pagesCount: Int,
    canFinish: Boolean,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val isLastPage = pagerState.currentPage == pagesCount - 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(), // Учитываем жесты
        contentAlignment = Alignment.Center
    ) {
        // Индикаторы страниц
        Row(
            Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pagesCount) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    colorScheme.primary
                else
                    colorScheme.outlineVariant

                val width by animateDpAsState(if (pagerState.currentPage == iteration) 24.dp else 8.dp)

                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Кнопка действия (Справа)
        Box(Modifier.align(Alignment.CenterEnd)) {
            AnimatedContent(targetState = isLastPage, label = "ButtonAnim") { last ->
                if (last) {
                    Button(
                        onClick = onFinish,
                        enabled = canFinish, // Блокируем, если не выбрано
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("Начать")
                        Gap(8)
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                    }
                } else {
                    FilledIconButton(
                        onClick = onNext,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingBackground(page: Int) {
    val color1 by animateColorAsState(
        when (page) {
            0 -> colorScheme.primaryContainer.copy(0.4f)
            1 -> colorScheme.tertiaryContainer.copy(0.4f)
            2 -> colorScheme.secondaryContainer.copy(0.4f)
            else -> colorScheme.surface
        }
    )

    val surface = colorScheme.surface

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(color1, surface)
            )
        )
    }
}