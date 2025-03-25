package app.what.schedule.data.local.settings

import android.content.Context
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.Teacher

class AppSettingsRepository(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("MY_APP_PREFERENCES", Context.MODE_PRIVATE)

    object Keys {
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val LAST_SEARCHED_GROUP = "last_searched_group"
        const val LAST_SEARCHED_TEACHER = "last_searched_teacher"
        const val USED_SERVER = "used_server"
        const val INSTITUTION = "institution"
    }

    enum class AppServers {
        TURTLE, RKSI
    }

    fun isFirstLaunch(): Boolean = sharedPreferences.getBoolean(Keys.IS_FIRST_LAUNCH, true)

    fun setFirstLaunch(value: Boolean) =
        sharedPreferences.edit().putBoolean(Keys.IS_FIRST_LAUNCH, value).apply()

    fun getLastSearchedGroup(): Group? = sharedPreferences
        .getString(Keys.LAST_SEARCHED_GROUP, null)
        ?.let { val raw = it.split("|"); Group(raw[0], raw[1]) }

    fun setLastSearchedGroup(value: Group) = sharedPreferences
        .edit()
        .putString(Keys.LAST_SEARCHED_GROUP, "${value.name}|${value.id}")
        .apply()

    fun getLastSearchedTeacher(): Teacher? = sharedPreferences
        .getString(Keys.LAST_SEARCHED_TEACHER, null)
        ?.let { val raw = it.split("|"); Teacher(raw[0], raw[1]) }

    fun setLastSearchedTeacher(value: Teacher) = sharedPreferences
        .edit()
        .putString(Keys.LAST_SEARCHED_TEACHER, "${value.name}|${value.id}")
        .apply()

    fun getUsedServer(): AppServers =
        AppServers.entries[sharedPreferences.getInt(Keys.USED_SERVER, AppServers.RKSI.ordinal)]

    fun setUsedServer(value: AppServers) =
        sharedPreferences.edit().putInt(Keys.USED_SERVER, value.ordinal).apply()

    fun getInstitutionData(): Triple<String, String, String>? =
        sharedPreferences.getString(Keys.INSTITUTION, null)?.split("|")
            ?.let { if (it.size != 3) null else Triple(it[0], it[1], it[2]) }

    fun setInstitutionData(value: Triple<String, String, String>) = sharedPreferences.edit()
        .putString(Keys.INSTITUTION, "${value.first}|${value.second}|${value.third}").apply()
}