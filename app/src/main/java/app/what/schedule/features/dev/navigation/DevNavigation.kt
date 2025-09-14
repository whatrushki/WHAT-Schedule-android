package app.what.schedule.features.dev.navigation

import androidx.navigation.compose.composable
import app.what.navigation.core.Registry
import app.what.navigation.core.SheetProvider
import app.what.schedule.features.dev.DevFeature
import kotlinx.serialization.Serializable

@Serializable
object DevProvider : SheetProvider {
    override val cancellable: Boolean = true
}

val devRegistry: Registry = {
    composable<DevProvider> { DevFeature() }
}