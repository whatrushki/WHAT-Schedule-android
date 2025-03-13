package app.what.foundation.utils

typealias Extension<T> = T.() -> Unit
typealias Extension1<T, A1> = T.(A1) -> Unit

// RE - Resultant Extension
typealias RE<T, R> = T.() -> R
typealias RE1<T, A1, R> = T.(A1) -> R

// SE - Suspend Extension
typealias SE<T> = suspend T.() -> Unit
typealias SE1<T, A1> = suspend T.(A1) -> Unit


// SRE - Suspend Resultant Extension
typealias SRE<T, R> = suspend T.() -> R
typealias SRE1<T, A1, R> = suspend T.(A1) -> R