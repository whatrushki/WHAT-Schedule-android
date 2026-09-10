package app.what.schedule.utils

typealias Analytics = app.what.foundation.utils.Analytics
typealias AppUtils = app.what.foundation.utils.AppUtils
typealias LogScope = app.what.foundation.utils.LogScope
typealias LogCat = app.what.foundation.utils.LogCat

fun buildTag(scope: String, category: String, component: String? = null): String =
    app.what.foundation.utils.buildTag(scope, category, component)
