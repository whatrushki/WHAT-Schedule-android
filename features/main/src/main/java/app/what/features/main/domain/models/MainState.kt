package app.what.schedule.features.main.domain.models

import app.what.foundation.core.UIComponent

data class MainState(
    val hasProfilePage: Boolean = false,
    val ui: UIComponent? = null
)