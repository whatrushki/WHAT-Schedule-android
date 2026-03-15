package app.what.schedule.features.insts.dgtu.domain.models

sealed interface DgtuEvent {
    data class AuthClicked(val login: String, val password: String) : DgtuEvent
    object MainOpened : DgtuEvent

    object MailsOpened : DgtuEvent
    object OnMailsListEndingScrolled : DgtuEvent
    object GenerateAccessQrCodeClicked : DgtuEvent
    object OnShowAllNewsClicked : DgtuEvent
    object OnGroupClicked : DgtuEvent
    data class OnNewClicked(val id: String) : DgtuEvent
    data class OnEventClicked(val id: String) : DgtuEvent
}