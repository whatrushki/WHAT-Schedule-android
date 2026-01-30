package app.what.schedule.utils

object LogScope {
    const val CORE = "core"
    const val AUTH = "auth"
    const val SCHEDULE = "schedule"
    const val NEWS = "news"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"
    const val WIDGET = "widget"
    const val DATABASE = "database"
    const val NETWORK = "network"
    const val FILE = "file"
    const val UI = "ui"
}

object LogCat {
    const val UI = "ui"
    const val NET = "net"
    const val DB = "db"
    const val NAV = "nav"
    const val PERF = "perf"
    const val INIT = "init"
    const val ERROR = "error"
    const val STATE = "state"
}

fun buildTag(scope: String, category: String, component: String? = null): String {
    return if (component != null) "$scope.$category.$component" else "$scope.$category"
}
