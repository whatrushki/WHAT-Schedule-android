package app.what.foundation.utils


val <T : Any> T?.orThrow
    get() = this ?: throw NullPointerException("value is null")

fun <T : Any> T?.orThrow(message: String): T = this ?: throw NullPointerException(message)

inline fun <T : Any> T?.orThrow(message: () -> String): T = this.orThrow(message())
