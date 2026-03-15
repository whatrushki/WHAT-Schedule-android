package app.what.schedule.features.insts.dgtu.domain.models

import app.what.schedule.data.remote.api.models.ScheduleSearch

sealed interface DgtuAction {
    object OpenAuth : DgtuAction
    object OpenMain : DgtuAction
    object OpenNews : DgtuAction
    data class OpenSchedule(val search: ScheduleSearch) : DgtuAction
    object OpenMail : DgtuAction
    object OpenCertificate : DgtuAction
    object OpenEvent : DgtuAction
    data class OpenNewDetail(
        val id: String,
        val url: String,
        val bannerUrl: String,
        val title: String,
        val description: String?
    ) : DgtuAction
}