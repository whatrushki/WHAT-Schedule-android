package app.what.schedule.wasm.model

import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.GroupDto
import app.what.schedule.core.models.TeacherDto
import kotlinx.serialization.Serializable

data class WebSearchItem(
    val id: String,
    val title: String,
    val isTeacher: Boolean = false
)

@Serializable
data class WebInstitutionData(
    val lastSync: String = "",
    val institution: String = "",
    val groups: List<GroupDto> = emptyList(),
    val teachers: List<TeacherDto> = emptyList(),
    val schedules: Map<String, List<DayScheduleDto>> = emptyMap()
)

enum class WebUniversity(val code: String, val title: String) {
    RKSI("rksi", "РКСИ"),
    DGTU("dgtu", "ДГТУ"),
    IUBIP("iubip", "ИУБиП"),
    RINH("rinh", "РИНХ")
}
