package app.what.schedule.data.local.settings

import android.content.Context

class AppSettingsRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MY_APP_PREFERENCES", Context.MODE_PRIVATE)

    object Keys {
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val LAST_SEARCHED_GROUP = "last_searched_group"
        const val LAST_SEARCHED_TEACHER = "last_searched_teacher"
        const val USED_SERVER = "used_server"
    }

    enum class AppServers {
        TURTLE, RKSI
    }

    fun isFirstLaunch(): Boolean = sharedPreferences.getBoolean(Keys.IS_FIRST_LAUNCH, true)

    fun setFirstLaunch(value: Boolean) = sharedPreferences.edit().putBoolean(Keys.IS_FIRST_LAUNCH, value).apply()

    fun getLastSearchedGroup(): String? = sharedPreferences.getString(Keys.LAST_SEARCHED_GROUP, null)

    fun setLastSearchedGroup(value: String) = sharedPreferences.edit().putString(Keys.LAST_SEARCHED_GROUP, value).apply()

    fun getLastSearchedTeacher(): String? = sharedPreferences.getString(Keys.LAST_SEARCHED_TEACHER, null)

    fun setLastSearchedTeacher(value: String) = sharedPreferences.edit().putString(Keys.LAST_SEARCHED_TEACHER, value).apply()

    fun getUsedServer(): AppServers = AppServers.entries[sharedPreferences.getInt(Keys.USED_SERVER, AppServers.RKSI.ordinal)]

    fun setUsedServer(value: AppServers) = sharedPreferences.edit().putInt(Keys.USED_SERVER, value.ordinal).apply()
}