package app.what.schedule.utils

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

object Analytics {
    private val fb = Firebase.analytics

    fun logUniversitySelect(uniName: String) {
        fb.logEvent("select_university") {
            param("uni_name", uniName)
        }
    }

    fun logScheduleRequest(targetName: String, type: String) { // type: "student" или "teacher"
        fb.logEvent("request_schedule") {
            param("target_name", targetName)
            param("target_type", type)
        }
    }

    fun logNewsOpen(newsId: String, link: String, title: String) {
        fb.logEvent("news_view_detail") {
            param("news_id", newsId)
            param("news_link", link)
            param("news_title", title)
        }
    }


    fun logSettingChanged(settingKey: String, value: String) {
        fb.logEvent("settings_update") {
            param("setting_name", settingKey)
            param("new_value", value)
        }
    }


    fun logShare(contentType: String, url: String) {
        fb.logEvent(FirebaseAnalytics.Event.SHARE) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param("url", url)
        }
    }

    fun logEasterEggFound(method: String) {
        fb.logEvent("easter_egg_unlocked") {
            param("method", method)
        }

        fb.setUserProperty("is_developer", "true")
    }

    fun logDevPanelOpen() {
        fb.logEvent("dev_panel_open") {

        }
    }

    fun logScreenView(screenName: String) {
        fb.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }
}