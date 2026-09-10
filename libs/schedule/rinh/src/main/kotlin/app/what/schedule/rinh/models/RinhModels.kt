package app.what.schedule.rinh.models

import kotlinx.serialization.Serializable

object RINHApi {
    object Schedule {
        object Responses {
            @Serializable
            data class ScheduleSearch(
                val id: Int,
                val name: String
            )

            @Serializable
            data class GetSchedule(
                val kind: String,
                val instance: String,
                val weeks: List<Week>
            )

            @Serializable
            data class Week(
                val id: Int,
                val name: String,
                val current: Boolean,
                val parity: Int,
                val days: List<Day>
            )

            @Serializable
            data class Day(
                val id: Int,
                val date: String,
                val name: String,
                val pairs: List<APair>
            )

            @Serializable
            data class APair(
                val id: Int,
                val startTime: String,
                val endTime: String,
                val lessons: List<RINHLesson>
            )

            @Serializable
            data class RINHLesson(
                val id: Int,
                val teacher: RINHTeacher,
                val subgroup: SubGroup,
                val subject: String,
                val group: String,
                val kind: Kind,
                val audience: String
            )

            @Serializable
            data class Kind(
                val id: Int,
                val name: String,
                val shortName: String
            )

            @Serializable
            data class SubGroup(
                val id: Int,
                val name: String
            )

            @Serializable
            data class RINHTeacher(
                val id: Int,
                val name: String
            )
        }
    }
}
