package app.what.foundation.utils

sealed interface ShareVariant {
    object Clipboard : ShareVariant
    object SystemDefault : ShareVariant
}
