package app.what.navigation.core

import app.what.foundation.core.UIComponent

interface NavComponent<P : NavProvider> : UIComponent {
    val data: P
}