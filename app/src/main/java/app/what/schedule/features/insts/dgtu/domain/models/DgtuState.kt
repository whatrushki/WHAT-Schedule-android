package app.what.schedule.features.insts.dgtu.domain.models

import androidx.compose.ui.text.AnnotatedString
import app.what.foundation.data.RemoteState
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.data.remote.providers.dgtu.DGTUApi.Events.Initiator
import java.time.LocalDate
import java.time.LocalDateTime

data class DgtuState(
    val token: String? = null,
    val studentId: Int? = null,
    val mailsFetchState: RemoteState = RemoteState.Idle,
    val mails: List<Mail> = emptyList(),
    val mailsPage: Int = 1,
    val newsFetchState: RemoteState = RemoteState.Idle,
    val news: List<NewListItem> = emptyList(),
    val studentInfoFetchState: RemoteState = RemoteState.Idle,
    val studentInfo: DgtuStudentInfo? = null,
    val studentStatInfoFetchState: RemoteState = RemoteState.Idle,
    val studentStatInfo: DgtuStudentStatInfo? = null,
    val notificationsFetchState: RemoteState = RemoteState.Idle,
    val notifications: List<Notification> = emptyList(),
    val accessQrFetchState: RemoteState = RemoteState.Idle,
    val accessQr: Int? = null,
    val eventsFetchState: RemoteState = RemoteState.Idle,
    val events: List<EventListItem> = emptyList(),
    val eventDetailFetchState: RemoteState = RemoteState.Idle,
    val eventDetail: EventDetailItem? = null
)

data class EventListItem(
    val id: Int,
    val date: LocalDateTime,
    val title: String,
    val place: String?
)

class EventDetailItem(
    val entryEnd: Boolean,
    val eventId: Long,
    val name: String,
    val description: String,
    val target: String?,
    val dateStart: LocalDateTime,
    val dateEnd: LocalDateTime,
    val place: String,
    val registration: Boolean,
    val isDelete: Boolean,
    val contactDetailsFio: String?,
    val contactDetailsPhone: String?,
    val isArchive: Boolean,
    val initiator: Initiator?,
    val levelName: String?,
    val typeEventName: String,
    val typeName: String,
    val categoryName: String,
    val isRegistered: Boolean,
    val allowRegistration: Boolean,
)

data class Notification(
    val id: Int,
    val category: String,
    val date: LocalDateTime,
    val link: Pair<String, String>?,
    val content: AnnotatedString
)

data class Mail(
    val id: Int,
    val title: String,
    val description: AnnotatedString,
    val sender: String,
    val photoLink: String,
    val sendDateTime: LocalDateTime
)

data class DgtuStudentStatInfo(
    val avgCourse: Float,
    val avg3: Float,
    val avg4: Float,
    val avg5: Float
)

data class DgtuStudentInfo(
    val name: String,
    val surname: String,
    val group: ScheduleSearch.Group,
    val kafName: String,
    
    val fullName: String,
    
    val middleName: String,
    
    val numRecordBook: String,
    val numberMobile: String?,
    val birthday: LocalDate,
    val nationality: String,
    
    val email: String,
    val login: String,
    val admissionYear: String,
    val lastEnterDateLocalDate: LocalDate,
    val course: String,
    val faculty: String,
    
    val conditionsEducation: Int,
    val trainingDirection: String,
    var photoLink: String,
    val online: Boolean,
    
    val message: String?,
    val htmlBlock: String,
    
    val vkID: Int?,
    val googleID: Int?,
    val yandexID: Int?,
    val telegramID: Int?,
    val maxID: Int?,
    
    val chatLink: String,
)