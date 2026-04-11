package app.what.schedule.data.remote.providers.dgtu

import app.what.schedule.data.remote.utils.LocalDateSerializer
import app.what.schedule.data.remote.utils.LocalDateTimeSerializer
import app.what.schedule.data.remote.utils.parseMonth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDate
import java.time.LocalDateTime


@Serializable
data class ApiResponse<T>(
    @SerialName("data") val _data: T?,
    val state: Int,
    val msg: String?,
    val time: Float?
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
            val fullName: String,
            val showZachBook: Boolean,
            val domintoryNumber: String,
//            val numberRoom: Any?,
            val fullNameT: String,
            val name: String,
            val middleName: String,
            val migrRegistrationAddressProduction: String?,
            val migrRegistrationDateToMigration: String?,
            val migrRegistrationDateToStudyVisa: String?,
            val isAgreementPersonalData: Boolean,
            val agreementProcessingPersonalData: Boolean,
            val agreementTransferPersonalData: Boolean,
            val numRecordBook: String,
            val numberMobile: String,
            val surname: String,
            @SerialName("birthday") val birthdayRaw: String,
            val nationality: String,
            val group: Models.ProfileGroup,
            val email: String,
            val login: String,
            val emailForTeams: String?,
            val admissionYear: String,
            @SerialName("lastEnterDate") val lastEnterDateRaw: String,
            val course: String,
            val faculty: String,
            val plan: Models.ProfilePlan,
            val conditionsEducation: Int,
            val trainingDirection: String,
            var photoLink: String,
            val verPhoto: String?,
            val activeSwapPhotoAndVerification: Boolean,
            val photoFormatAspectRatio: String,
            val activeMigrationRegistration: Boolean,
            val isMigrStud: Boolean,
//            val scientificDirector: Any?,
            val allowChangePass: Boolean,
            val showRaspButton: Boolean,
//            val linkRaspButton: Any?,
            val showGraphButton: Boolean,
            val showVedButton: Boolean,
            val maxFileSize: String,
            val showResultButton: Boolean,
            val isLocked: Boolean,
            val isLockedVed: Boolean,
//            val libraryСard: Any?,
            val online: Boolean,
            val hideLinks: Boolean,
            val message: String?,
            val htmlBlock: String,
            val activeSwapPhoto: Boolean,
            val status: Int,
            val ratingActivation: Boolean,
            val linkPsychology: String?,
            val portfolioIncluded: Boolean,
            val debtsGraphIncluded: Boolean,
            val needDormitory: Boolean,
            val vkID: Int?,
            val googleID: Int?,
            val yandexID: Int?,
            val telegramID: Int?,
            val maxID: Int?,
            val allowChangePassStudent: Boolean,
            val hidePlan: Boolean,
            val chatLink: String,
            val hideMoveStory: Boolean,
//            val eliteEducationID: Int?,
//            val scopusID: Any?,
            val isDstu: Boolean,
            val kaf: Models.ProfileKafedra,
            val facul: Models.ProfileFacul
        ) {
            @Transient
            val birthday = birthdayRaw.split(" ").let {
                LocalDate.of(it[2].toInt(), parseMonth(it[1]), it[0].toInt())
            }
            
            @Transient
            val lastEnterDate = lastEnterDateRaw.split(" ").let {
                LocalDate.of(it[2].toInt(), parseMonth(it[1]), it[0].toInt())
            }
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
            @SerialName("код") val code: Int,
            @SerialName("дата") @Serializable(LocalDateTimeSerializer::class)
            val date: LocalDateTime,
            @SerialName("датаНачала") @Serializable(LocalDateTimeSerializer::class)
            val startTime: LocalDateTime,
            @SerialName("датаОкончания") @Serializable(LocalDateTimeSerializer::class)
            val endTime: LocalDateTime,
            @SerialName("перерыв") val breakTime: Float?,
            @SerialName("начало") val start: String,
            @SerialName("конец") val end: String,
            @SerialName("деньНедели") val weekDays: Int,
            @SerialName("день_недели") val weekDay: String,
            @SerialName("почта") val email: String,
            @SerialName("день") val day: String,
            @SerialName("код_Семестра") val codeSemester: Int,
            @SerialName("типНедели") val weekType: Int,
            @SerialName("номерПодгруппы") val numberSubgroup: Int,
            @SerialName("часов") val hoursOf: String?,
            @SerialName("дисциплина") val subject: String,
            @SerialName("преподаватель") val teacher: String,
            @SerialName("должность") val position: String?,
            @SerialName("аудитория") val auditory: String,
            @SerialName("учебныйГод") val studyYear: String,
            @SerialName("группа") val group: String,
            @SerialName("custom1") val custom1: String,
            @SerialName("часы") val hours: String,
            @SerialName("неделяНачала") val weekOfStart: Int?,
            @SerialName("неделяОкончания") val weekOfEnd: Int?,
            @SerialName("замена") val replacement: Boolean?,
            @SerialName("кодПреподавателя") val codeTeacher: Int?,
            @SerialName("кодГруппы") val codeGroup: Int?,
            @SerialName("фиоПреподавателя") val teacherName: String,
            @SerialName("кодПользователя") val codeUser: Int?,
            @SerialName("элементЦиклРасписания") val cycleElement: Boolean,
            @SerialName("элементГрафика") val graphElement: Boolean,
            @SerialName("тема") val theme: String?,
            @SerialName("номерЗанятия") val number: Int,
            @SerialName("ссылка") val link: String?,
            @SerialName("созданиеВебинара") val createWebinar: Boolean,
            @SerialName("кодВебинара") val codeWebinar: Int?,
            @SerialName("вебинарЗапущен") val webinarStarted: Boolean,
            @SerialName("показатьЖурнал") val showJournal: Boolean,
            @SerialName("кодыСтрок") val codeLines: List<Int>,
            @SerialName("цвет") val color: String
        )
    }
}
