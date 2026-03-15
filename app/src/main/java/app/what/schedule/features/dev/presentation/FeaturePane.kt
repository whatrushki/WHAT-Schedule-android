package app.what.schedule.features.dev.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.ui.components.Fallback
import org.koin.compose.koinInject

@Composable
fun FeaturePane() = Column {
    val settings = koinInject<AppValues>()

    Fallback(
        text = "В разработке",
        modifier = Modifier.fillMaxSize()
    )
}