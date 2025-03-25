package app.what.schedule.features.onboarding.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.core.Listener
import app.what.foundation.ui.Gap
import app.what.foundation.ui.keyboardAsState
import app.what.schedule.features.onboarding.domain.models.OnboardingEvent
import app.what.schedule.features.onboarding.domain.models.OnboardingState
import app.what.schedule.features.onboarding.presentation.pages.InstitutionSelectPage

@Composable
fun OnboardingView(
    state: OnboardingState,
    listener: Listener<OnboardingEvent>
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .background(colorScheme.background)
) {
    val keyboardState by keyboardAsState()

    Box(
        Modifier
            .animateContentSize()
            .height(if (keyboardState) 20.dp else 120.dp)
    )

    Text(
        "Выбери\nучебное заведение",
        style = typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp,
        color = colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
    )

    Text(
        "+ провайдера расписания",
        color = colorScheme.primary,
        style = typography.bodyMedium,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Gap(16)

    InstitutionSelectPage(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(horizontal = 12.dp),
        institutions = state.institutions
    ) { i, f, p ->
        listener(OnboardingEvent.InstitutionAndProviderSelected(i, f, p))
    }
}


@Preview(showSystemUi = true)
@Composable
fun OnboardingViewPrev() {
    OnboardingView(OnboardingState()) { }
}
