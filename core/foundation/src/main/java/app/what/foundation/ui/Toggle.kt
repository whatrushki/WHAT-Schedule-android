package app.what.foundation.ui

fun <T> List<T>.toggle(item: T): List<T> =
    if (this.contains(item)) this - item else this + item

fun <T> MutableList<T>.toggle(item: T) =
    if (this.contains(item)) this.remove(item) else this.add(item)