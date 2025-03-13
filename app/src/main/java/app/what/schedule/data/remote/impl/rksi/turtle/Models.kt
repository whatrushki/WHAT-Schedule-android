package app.what.schedule.data.remote.impl.rksi.turtle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


object TurtleApi {
    object Schedule {
        object Responses {
            @Serializable
            data class GetSchedule(
                val days: List<ForDay>,
                val name: String
            )

            @Serializable
            data class ForDay(
                val day: String,
                val isoDateDay: String,
                val apairs: List<Apair>
            )

            @Serializable
            data class Apair(
                val time: String,
                val apair: List<ApairApair>,
                val isoDateStart: String,
                val isoDateEnd: String
            )

            @Serializable
            data class ApairApair(
                val doctrine: String,
                val teacher: String,
                @SerialName("auditoria") val auditory: String,
                val corpus: String,
                val number: Int,
                val start: String,
                val end: String,
                val warn: String
            )
        }
    }
}