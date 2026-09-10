package app.what.schedule.dgtu.models

import app.what.schedule.core.utils.LocalDateSerializer
import app.what.schedule.core.utils.LocalDateTimeSerializer
import app.what.schedule.core.utils.parseMonth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@Serializable
data class ApiResponse<T>(
    @SerialName("data") val _data: T? = null,
    val state: Int = 1,
    val msg: String? = null,
    val time: Float? = null
) {
    val data: T get() = _data!!
}

object DGTUApi {
    object Auth {
        @Serializable
        data class Login(
            val userName: String,
            val password: String,
            val fingerprint: String,
            val isParent: Boolean = false,
            val captchaKey: String = "",
            val captchaCode: String = "",
            val redirect: Boolean = false
        )
        
        @Serializable
        data class LoginResponse(
            val userName: String,
            val requertAt: String,
            val accessToken: String,
            val refreshToken: String,
            val uid_1c: String,
            val id: Int
        )
    }
    
    object Stats {
        @Serializable
        data class GetAvgMarkResponse(
            val avgMark: Float
        )
        
        @Serializable
        data class GetMarksCountResponse(
            val count: Int,
            val markCountStatistic: List<Models.MarkCountItem>
        )
    }
    
    object Events {
        @Serializable
        data class GetAllResponse(
            val events: List<Models.Event>,
            val levels: List<Models.Level>,
            val categories: List<Models.Category>,
            val types: List<Models.Type>,
            val typesEvents: List<Models.TypesEvent>,
            val allowAdd: Boolean,
        )
        
        @Serializable
        data class GetDetailEventInfo(
            val eventInfo: EventInfo,
//            val organizersList: List<Any?>,
            val accessArray: List<AccessItem>,
            val isRegistered: Boolean,
            val allowRegister: Boolean,
            val isOrg: Boolean,
//            val request: Any?,
            val isAuthor: Boolean,
        )
        
        @Serializable
        data class EventInfo(
            val entryEnd: Boolean,
            @SerialName("eventID")
            val eventId: Long,
            val name: String,
            @SerialName("levelID")
            val levelId: Long,
            @SerialName("typeEventID")
            val typeEventId: Long,
            @SerialName("categoryID")
            val categoryId: Long?,
//            @SerialName("typeID")
//            val typeId: Any?,
            val description: String,
            val target: String,
//            val link: Any?,
            @Serializable(LocalDateTimeSerializer::class) val dateStart: LocalDateTime,
            @Serializable(LocalDateTimeSerializer::class) val dateEnd: LocalDateTime,
//            val organizer: String?,
            val place: String,
//            val contactDetails: Any?,
            val registration: Boolean,
//            val limitParticipants: Any?,
            val press: Boolean,
//            val photo: Any?,
//            val fileRegulations: Any?,
//            val fileOrder: Any?,
//            val verificationMessage: Any?,
            val verified: Boolean,
            val refinement: Boolean,
            @SerialName("userID")
            val userId: Long,
            val dateCreate: String,
            val dateEdit: String,
            val isDelete: Boolean,
//            val dateDelete: Any?,
            val dateVerification: String,
            @SerialName("verificatorID")
            val verificatorId: Long,
            val color: String?,
//            val confirmationCode: Any?,
//            val entryBefore: Any?,
//            val minParticipants: Any?,
//            val countParticipants: Any?,
//            val countViewers: Any?,
//            val verifierAdditions: Any?,
            @SerialName("contactDetailsFIO")
            val contactDetailsFio: String?,
            val contactDetailsPhone: String?,
//            val helpBooking: Any?,
            val linkOrganizer: String?,
//            val skipByReason: Any?,
//            val noCancelRecord: Any?,
            val photoPach: String,
            val allowEdit: Boolean,
            val isArchive: Boolean,
            val initiator: Initiator?,
//            val organizersList: List<Any?>,
//            val participantsList: List<Any?>,
            val levelName: String,
            val typeEventName: String,
            val typeName: String,
            val categoryName: String,
        )
        
        @Serializable
        data class Initiator(
            val name: String,
            @SerialName("userID")
            val userId: Long,
//            val course: Any?,
//            @SerialName("groupID")
//            val groupId: Any?,
            val photo: String,
//            val category: Any?,
            val visible: Boolean,
//            val date: Any?,
//            val confirmed: Any?,
        )
        
        @Serializable
        data class AccessItem(
            @SerialName("accessID")
            val accessId: Long,
            @SerialName("siteEventID")
            val siteEventId: Long,
            val students: Boolean,
            val teachers: Boolean,
            val users: Boolean,
//            @SerialName("facultyID")
//            val facultyId: Any?,
//            @SerialName("kafedraID")
//            val kafedraId: Any?,
//            @SerialName("groupID")
//            val groupId: Any?,
//            val cours: Any?,
            val forGraduates: Boolean,
            val forDorms: Boolean,
//            @SerialName("levelID")
//            val levelId: Any?,
//            @SerialName("dormID")
//            val dormId: Any?,
//            val siteEvent: Any?,
        )
    }
    
    object ZachBook {
        @Serializable
        data class GetResponse(
            val showVedButton: Boolean,
            val showPrintForm: Boolean,
            val hideZET: Boolean,
            val showPersonalCard: Boolean,
            val groupID: Int,
            val markCountStatistic: List<Models.MarkCount>,
            val avgCourseStatistic: List<Models.AvgCourse>,
            val avg: Float?,
            val zachBook: List<Models.ZachItem>,
            val groupedZachBook: List<Models.ZachGroupedItem>,
            val studentName: String,
            val recordbook: String,
            val studentInfo: Models.StudentInfo,
            val avgPoint: Float,
            val currentSem: Int,
            val photo: String?,
            val isZaoch: Boolean,
            val studentZachBooks: List<Models.ZachBook>,
            val showDebts: Boolean
        )
    }
    
    object Profile {
        @Serializable
        data class GetStudentInfoResponse(
            val studentID: Int,
            val fullName: String = "",
            val showZachBook: Boolean = false,
            val domintoryNumber: String? = null,
//            val numberRoom: Any?,
            val fullNameT: String? = null,
            val name: String = "",
            val middleName: String = "",
            val migrRegistrationAddressProduction: String? = null,
            val migrRegistrationDateToMigration: String? = null,
            val migrRegistrationDateToStudyVisa: String? = null,
            val isAgreementPersonalData: Boolean = false,
            val agreementProcessingPersonalData: Boolean = false,
            val agreementTransferPersonalData: Boolean = false,
            val numRecordBook: String = "",
            val numberMobile: String? = null,
            val surname: String = "",
            @SerialName("birthday") val birthdayRaw: String? = null,
            val nationality: String = "",
            val group: Models.ProfileGroup? = null,
            val email: String = "",
            val login: String = "",
            val emailForTeams: String? = null,
            val admissionYear: String = "",
            @SerialName("lastEnterDate") val lastEnterDateRaw: String? = null,
            val course: String = "",
            val faculty: String = "",
            val plan: Models.ProfilePlan? = null,
            val conditionsEducation: Int = 0,
            val trainingDirection: String = "",
            var photoLink: String? = null,
            val verPhoto: String? = null,
            val activeSwapPhotoAndVerification: Boolean = false,
            val photoFormatAspectRatio: String? = null,
            val activeMigrationRegistration: Boolean = false,
            val isMigrStud: Boolean = false,
//            val scientificDirector: Any?,
            val allowChangePass: Boolean = false,
            val showRaspButton: Boolean = false,
//            val linkRaspButton: Any?,
            val showGraphButton: Boolean = false,
            val showVedButton: Boolean = false,
            val maxFileSize: String? = null,
            val showResultButton: Boolean = false,
            val isLocked: Boolean = false,
            val isLockedVed: Boolean = false,
//            val libraryСard: Any?,
            val online: Boolean = false,
            val hideLinks: Boolean = false,
            val message: String? = null,
            val htmlBlock: String = "",
            val activeSwapPhoto: Boolean = false,
            val status: Int = 0,
            val ratingActivation: Boolean = false,
            val linkPsychology: String? = null,
            val portfolioIncluded: Boolean = false,
            val debtsGraphIncluded: Boolean = false,
            val needDormitory: Boolean = false,
            val vkID: Int? = null,
            val googleID: Int? = null,
            val yandexID: Int? = null,
            val telegramID: Int? = null,
            val maxID: Int? = null,
            val allowChangePassStudent: Boolean = false,
            val hidePlan: Boolean = false,
            val chatLink: String? = null,
            val hideMoveStory: Boolean = false,
//            val eliteEducationID: Int?,
//            val scopusID: Any?,
            val isDstu: Boolean = true,
            val kaf: Models.ProfileKafedra? = null,
            val facul: Models.ProfileFacul? = null
        ) {
            @Transient
            val birthday: LocalDate = birthdayRaw?.split(" ")?.let {
                try {
                    LocalDate(it[2].toInt(), parseMonth(it[1]), it[0].toInt())
                } catch (_: Exception) {
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
            } ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            @Transient
            val lastEnterDate: LocalDate = lastEnterDateRaw?.split(" ")?.let {
                try {
                    LocalDate(it[2].toInt(), parseMonth(it[1]), it[0].toInt())
                } catch (_: Exception) {
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
            } ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        
        @Serializable
        data class GetStatisticsResponse(
            val markCountStatistic: List<Models.MarkCountItem>,
            val count: Int
        )
    }
    
    object Mails {
        @Serializable
        data class GetUnreadIdsResponse(
            val messagesIDs: List<Int>,
            val count: Int
        )
        
        @Serializable
        data class GetAllRequest(
            val page: Int,
            val pageEl: Int = 25,
            val unreadMessages: Boolean = false,
            val modeParent: Int = 0,
            val searchQuery: String? = null,
            val senderIDs: List<Int>? = null,
            @Serializable(LocalDateSerializer::class)
            val dateFrom: LocalDate? = null,
            @Serializable(LocalDateSerializer::class)
            val dateTo: LocalDate? = null,
            val messageTypeIDs: List<Int>? = null,
            val folderID: Int? = null
        )
        
        @Serializable
        data class GetAllResponse(
            val page: Int,
            val totalPages: Int,
            val hiddenNextPage: Boolean,
            val showParent: Boolean,
            @SerialName("messages") val messageThreads: List<Models.MessageThread>
        )
    }
    
    object Feeds {
        @Serializable
        data class GetAllRequest(
            val userID: Int
        )
        
        @Serializable
        data class GetAllResponse(
//            val benchmark: Any?
            val categories: List<String>,
            val feed: List<Models.FeedItem>,
            val showMore: Boolean,
            val time: Float
        )
    }
    
    object Schedule {
        @Serializable
        data class ListYears(
            val years: List<String>
        )
        
        @Serializable
        data class Get(
            val isCyclicalSchedule: Boolean,
            val rasp: List<Models.DGTULesson>
        )
    }
    
    object Models {
        @Serializable
        data class Event(
            @SerialName("eventID")
            val eventId: Int,
            @Serializable(LocalDateTimeSerializer::class) val dateStart: LocalDateTime,
            @Serializable(LocalDateTimeSerializer::class) val dateEnd: LocalDateTime,
            val entryBefore: String?,
            val description: String,
            val name: String,
            @SerialName("userID")
            val userId: Int?,
            @SerialName("categoryID")
            val categoryId: Int?,
            @SerialName("typeID")
            val typeId: Int?,
            val refinement: Boolean,
            val verified: Boolean,
            val place: String?,
//            val photo: Any?,
            val color: String?,
            val isArchive: Boolean,
            val isDelete: Boolean,
            val accessVerification: Boolean,
            @SerialName("objectID")
            val objectId: String,
            @SerialName("typeEventID")
            val typeEventId: Long,
            val limitParticipants: Long?,
            val participant: Boolean,
            val participantMark: Long,
            val participantComment: String,
            val participationConfirmed: Boolean,
            val eventEnd: Boolean,
            val org: Boolean,
            val month: String,
            val helpBooking: Boolean?,
            val verifierAdditions: String,
            val skipByReason: Boolean?,
//            val noCancelRecord: Any?,
        )
        
        @Serializable
        data class Level(
            @SerialName("levelID")
            val levelId: Long,
            val name: String,
        )
        
        @Serializable
        data class Category(
            val name: String,
            @SerialName("categoryID")
            val categoryId: Long,
            val color: String?,
        )
        
        @Serializable
        data class Type(
            val name: String?,
            @SerialName("typeID")
            val typeId: Long,
            @SerialName("categoryID")
            val categoryId: Long?,
        )
        
        @Serializable
        data class TypesEvent(
            @SerialName("typeID")
            val typeId: Long,
            val name: String,
            val available: Boolean,
//            val isCuratorial: Any?,
        )
        
        @Serializable
        data class ZachBook(
            val studentID: Int,
            val zachBook: String
        )
        
        @Serializable
        data class StudentInfo(
            val name: String,
            val group: String,
            val specialty: String
        )
        
        @Serializable
        data class ZachGroupedItem(
            val key: String,
            val year: String,
            val session: Int,
            val course: Int,
            val sem: Int,
            val controlForm: String,
            val marks: List<ZachItem>,
            val order: Int
        )
        
        @Serializable
        data class ZachItem(
            val key: Int,
            val course: Int,
            val sem: Int,
            val session: Int,
            val dis: String,
            val mark: String,
            val hours: Int,
            val vedID: Int,
            val block: String,
            val controlForm: String,
            val date: String,
            val teacherName: String,
            val year: String,
            val markNumber: Int,
            val zet: Float,
            val closed: Boolean
        )
        
        @Serializable
        data class AvgCourse(
            val course: Int,
            val avg: Float
        )
        
        @Serializable
        data class MarkCount(
            val mark: String,
            val count: Int,
            val percent: Float
        )
        
        @Serializable
        data class ProfileGroup(
            val item1: String,
            val item2: Int,
            val formID: Int
        )
        
        @Serializable
        data class ProfilePlan(
            val item1: String,
            val item2: Int,
            val item3: Boolean
        )
        
        @Serializable
        data class ProfileKafedra(
            val kafID: Int,
            val kafName: String,
            val aud: String,
            val phone: String
        )
        
        @Serializable
        data class ProfileFacul(
            val faculID: Int,
            val faculName: String,
            val aud: String,
            val phone: String
        )
        
        @Serializable
        data class MarkCountItem(
            val mark: Int,
            val markName: String,
            val count: Int,
            val avg: Float
        )
        
        @Serializable
        data class MessageThread(
            val id: Int,
            val folderID: Int?,
            val recipientID: Int,
            val recipientsCount: Int,
            val photoLinkRecipientID: String,
            val photoLinkUserID: String,
            val isDelete: Int?,
            @Serializable(LocalDateTimeSerializer::class)
            val dateRead: LocalDateTime?,
            val starMessage: Int?,
            val userIdFromMessage: String,
            val userIdGroupFromMessage: String,
            val userIdGroupToMessage: String,
            val userIdToMessage: String,
            val emailUserID: String,
            val emailRecipientID: String,
            val messageID: Int,
            @Serializable(LocalDateTimeSerializer::class)
            val dispatchDate: LocalDateTime,
            val userID: Int,
            val typeID: Int?,
            val typeName: String,
            val theme: String,
            val messageName: String,
            val messageIsDelete: Int?,
//            val recipient: Any?,
            val message: Message,
//            val type: Any?,
            val files: List<File>
        )
        
        @Serializable
        data class Message(
            val messageID: Int,
            val userID: Int,
            val typeID: Int,
            val parentID: Int?,
            val parentFamilyID: Int?,
            val theme: String,
            val htmlMessage: String?,
            val markdownMessage: String?,
            val message: String,
            @Serializable(LocalDateTimeSerializer::class)
            val dispatchDate: LocalDateTime,
//            val attachment: Any?,
            val messageImportant: Boolean?,
            val isDelete: Int?,
            val disciplineID: Int?,
//                val files: Any?,
//                val type: Any?,
//                val user: Any?
        )
        
        @Serializable
        data class File(
            val attachmentID: Int,
            val messageID: Int,
            val fileName: String,
            val path: String,
            val size: Int,
            val typeFile: String,
            val userID: Int,
            val sessionID: Int?,
            val isDelete: Int?,
            val deletedUserID: Int?
        )
        
        @Serializable
        data class FeedItem(
            val notificationID: Int,
            val userID: Int,
            val objectID: Int,
            val text: String?,
            val html: String?,
            @Serializable(LocalDateTimeSerializer::class) val fullDate: LocalDateTime,
            val category: String,
            val link: String?,
            val linkText: String?,
            val isNew: Boolean,
            val date: String,
            val time: String,
            val questionaryID: Int?,
            val published: Boolean,
            val forStudents: Boolean,
            val forTeachers: Boolean,
//            val views: Any?,
            val color: String
        )
        
        @Serializable
        data class DGTUTeacher(
            val name: String,
            val id: Int
        )
        
        @Serializable
        data class DGTUGroup(
            val name: String,
            val id: Int,
            val kurs: Int?
        )
        
        @Serializable
        data class DGTULesson(
            @SerialName("код") val code: Int = 0,
            @SerialName("дата") @Serializable(LocalDateTimeSerializer::class)
            val date: LocalDateTime,
            @SerialName("датаНачала") @Serializable(LocalDateTimeSerializer::class)
            val startTime: LocalDateTime,
            @SerialName("датаОкончания") @Serializable(LocalDateTimeSerializer::class)
            val endTime: LocalDateTime,
            @SerialName("перерыв") val breakTime: Float? = null,
            @SerialName("начало") val start: String? = null,
            @SerialName("конец") val end: String? = null,
            @SerialName("деньНедели") val weekDays: Int? = null,
            @SerialName("день_недели") val weekDay: String? = null,
            @SerialName("почта") val email: String? = null,
            @SerialName("день") val day: String? = null,
            @SerialName("код_Семестра") val codeSemester: Int? = null,
            @SerialName("типНедели") val weekType: Int? = null,
            @SerialName("номерПодгруппы") val numberSubgroup: Int? = null,
            @SerialName("часов") val hoursOf: String? = null,
            @SerialName("дисциплина") val subject: String = "",
            @SerialName("преподаватель") val teacher: String? = null,
            @SerialName("должность") val position: String? = null,
            @SerialName("аудитория") val auditory: String? = null,
            @SerialName("учебныйГод") val studyYear: String? = null,
            @SerialName("группа") val group: String? = null,
            @SerialName("custom1") val custom1: String? = null,
            @SerialName("часы") val hours: String? = null,
            @SerialName("неделяНачала") val weekOfStart: Int? = null,
            @SerialName("неделяОкончания") val weekOfEnd: Int? = null,
            @SerialName("замена") val replacement: Boolean? = null,
            @SerialName("кодПреподавателя") val codeTeacher: Int? = null,
            @SerialName("кодГруппы") val codeGroup: Int? = null,
            @SerialName("фиоПреподавателя") val teacherName: String? = null,
            @SerialName("кодПользователя") val codeUser: Int? = null,
            @SerialName("элементЦиклРасписания") val cycleElement: Boolean = false,
            @SerialName("элементГрафика") val graphElement: Boolean = false,
            @SerialName("тема") val theme: String? = null,
            @SerialName("номерЗанятия") val number: Int = 0,
            @SerialName("ссылка") val link: String? = null,
            @SerialName("созданиеВебинара") val createWebinar: Boolean = false,
            @SerialName("кодВебинара") val codeWebinar: Int? = null,
            @SerialName("вебинарЗапущен") val webinarStarted: Boolean = false,
            @SerialName("показатьЖурнал") val showJournal: Boolean = false,
            @SerialName("кодыСтрок") val codeLines: List<Int> = emptyList(),
            @SerialName("цвет") val color: String? = null
        )
    }
}
