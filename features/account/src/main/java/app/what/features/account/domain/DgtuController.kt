package app.what.schedule.features.insts.dgtu.domain

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.utils.launchIO
import app.what.foundation.utils.launchSafe
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.domain.models.ScheduleSearch
import app.what.schedule.dgtu.models.DGTUApi
import app.what.schedule.dgtu.DGTUAccountClient
import app.what.schedule.features.insts.dgtu.domain.models.DgtuAction
import app.what.schedule.features.insts.dgtu.domain.models.DgtuAction.OpenNewDetail
import app.what.schedule.features.insts.dgtu.domain.models.DgtuEvent
import app.what.schedule.features.insts.dgtu.domain.models.DgtuState
import app.what.schedule.features.insts.dgtu.domain.models.DgtuStudentInfo
import app.what.schedule.features.insts.dgtu.domain.models.DgtuStudentStatInfo
import app.what.schedule.features.insts.dgtu.domain.models.EventDetailItem
import app.what.schedule.features.insts.dgtu.domain.models.EventListItem
import app.what.schedule.features.insts.dgtu.domain.models.Mail
import app.what.schedule.features.insts.dgtu.domain.models.Notification
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

class DgtuController(
    private val institutionManager: InstitutionManager,
    private val accountService: DGTUAccountClient,
    private val appValues: AppValues
) : UIController<DgtuState, DgtuAction, DgtuEvent>(DgtuState()) {
    
    private val debug
        get() = appValues.debugMode.get() == true
    
    private val newsService
        get() = institutionManager.getSavedInstitution()!!.newsService
    
    init {
        val token = appValues.dgtuToken.get()
        
        updateState { copy(token = token, studentId = appValues.dgtuStudentId.get()) }
        
        if (token != null) viewModelScope.launchIO {
            val testData = accountService.getUnreadMessagesId(token)
            if (testData.state == -1 && testData.msg?.contains("unauth", true) == true) {
                appValues.dgtuToken.set(null)
                appValues.dgtuStudentId.set(null)
                
                updateState { copy(token = null, studentId = null) }
            }
        }.invokeOnCompletion {
            if (appValues.dgtuToken.get() == null) return@invokeOnCompletion
            loadAllData()
        }
    }
    
    fun loadAllData() {
        getProfileInfo()
        getStudentStatInfo()
        loadNews()
        getNotifications()
        loadEvents()
    }
    
    override fun obtainEvent(viewEvent: DgtuEvent) {
        when (viewEvent) {
            is DgtuEvent.AuthClicked -> auth(viewEvent)
            DgtuEvent.MainOpened -> {
                if (viewState.studentInfo == null && viewState.token != null) {
                    loadAllData()
                }
            }
            DgtuEvent.OnGroupClicked -> setAction(DgtuAction.OpenSchedule(viewState.studentInfo!!.group))
            DgtuEvent.OnShowAllNewsClicked -> setAction(DgtuAction.OpenNews)
            DgtuEvent.MailsOpened -> loadMails(true)
            DgtuEvent.OnMailsListEndingScrolled -> loadMails()
            DgtuEvent.GenerateAccessQrCodeClicked -> generateQr()
            is DgtuEvent.OnEventClicked -> loadEventDetail(viewEvent.id)
            is DgtuEvent.OnNewClicked -> {
                val new = viewState.news.first { it.id == viewEvent.id }
                setAction(OpenNewDetail(new.id, new.url, new.bannerUrl, new.title, new.description))
            }
        }
    }
    
    private fun auth(viewEvent: DgtuEvent.AuthClicked) {
        viewModelScope.launchSafe {
            val response = accountService.auth(
                viewEvent.login,
                viewEvent.password,
                appValues.userId.get() ?: ""
            )
            
            appValues.dgtuToken.set(response.data.data.accessToken)
            appValues.dgtuStudentId.set(response.data.data.id)
            updateState {
                copy(
                    token = response.data.data.accessToken,
                    studentId = response.data.data.id
                )
            }
            loadAllData()
            setAction(DgtuAction.OpenMain)
        }
    }
    
    private fun loadEventDetail(id: String) {
        updateState { copy(eventDetailFetchState = RemoteState.Loading) }
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(eventDetailFetchState = RemoteState.Error(it)) }
            }
        ) {
            val response = accountService.getDetailEvent(
                appValues.dgtuToken.get()!!,
                id
            )
            
            val evetDetail = with(response.data) {
                EventDetailItem(
                    eventInfo.entryEnd,
                    eventInfo.eventId,
                    eventInfo.name,
                    eventInfo.description,
                    eventInfo.target,
                    eventInfo.dateStart,
                    eventInfo.dateEnd,
                    eventInfo.place,
                    eventInfo.registration,
                    eventInfo.isDelete,
                    eventInfo.contactDetailsFio,
                    eventInfo.contactDetailsPhone,
                    eventInfo.isArchive,
                    eventInfo.initiator,
                    eventInfo.levelName,
                    eventInfo.typeEventName,
                    eventInfo.typeName,
                    eventInfo.categoryName,
                    isRegistered,
                    allowRegister
                )
            }
            
            updateState {
                copy(eventDetailFetchState = RemoteState.Success, eventDetail = evetDetail)
            }
        }
    }
    
    private fun loadNews() {
        updateState { copy(newsFetchState = RemoteState.Loading) }
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(newsFetchState = RemoteState.Error(it)) }
            }
        ) {
            val response = newsService.getNews(1).take(6)
            updateState { copy(newsFetchState = RemoteState.Success, news = response) }
        }
    }
    
    private fun generateQr() {
        if (viewState.accessQr != null) return
        
        updateState { copy(accessQrFetchState = RemoteState.Loading) }
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(accessQrFetchState = RemoteState.Error(it)) }
            }
        ) {
            val response = accountService.generatePassNumber(appValues.dgtuToken.get()!!)
            
            updateState { copy(accessQrFetchState = RemoteState.Success, accessQr = response.data) }
        }
    }
    
    
    private fun loadEvents() {
        if (viewState.accessQr != null) return
        
        updateState { copy(eventsFetchState = RemoteState.Loading) }
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(eventsFetchState = RemoteState.Error(it)) }
            }
        ) {
            val response = accountService.getEvents(appValues.dgtuToken.get()!!)
            
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val data = response.data.events.mapNotNull {
                if (it.dateStart.date < today) null
                else EventListItem(
                    it.eventId,
                    it.dateStart,
                    it.name,
                    it.place
                )
            }.sortedBy { it.date }
            
            updateState { copy(eventsFetchState = RemoteState.Success, events = data) }
        }
    }
    
    private fun loadMails(rollback: Boolean = false) {
        val page = if (rollback) 1 else viewState.mailsPage
        updateState { copy(mailsFetchState = RemoteState.Loading) }
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(mailsFetchState = RemoteState.Error(it)) }
            }
        ) {
            val response = accountService.getMails(
                appValues.dgtuToken.get()!!,
                DGTUApi.Mails.GetAllRequest(page = page, 50)
            )
            
            val data = response.data.messageThreads.map {
                Mail(
                    it.id,
                    title = it.theme,
                    description = AnnotatedString(
                        AnnotatedString.fromHtml(
                            it.message.message.takeIf { it.isNotBlank() }
                                ?: it.message.markdownMessage
                                ?: ""
                        ).text),
                    sender = it.userIdFromMessage,
                    photoLink = accountService.generateImageLink(it.photoLinkUserID),
                    sendDateTime = it.message.dispatchDate
                )
            }
            
            updateState {
                copy(
                    mailsFetchState = RemoteState.Success,
                    mails = if (rollback) data else viewState.mails + data,
                    mailsPage = page + 1
                )
            }
        }
    }
    
    private fun getStudentStatInfo() {
        if (viewState.studentInfo != null) return
        
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(studentStatInfoFetchState = RemoteState.Error(it)) }
            }
        ) {
            updateState { copy(studentStatInfoFetchState = RemoteState.Loading) }
            
            val response = accountService.getMarksCount(
                appValues.dgtuToken.get()!!,
                appValues.dgtuStudentId.get()!!
            )
            
            val stats = with(response.data) {
                DgtuStudentStatInfo(
                    avgCourse = "%.2f".format(
                        Locale.US,
                        markCountStatistic.sumOf { it.mark * it.count } / count.toFloat())
                        .toFloat(),
                    avg3 = markCountStatistic.firstOrNull { it.mark == 3 }?.avg ?: 0f,
                    avg4 = markCountStatistic.firstOrNull { it.mark == 4 }?.avg ?: 0f,
                    avg5 = markCountStatistic.firstOrNull { it.mark == 5 }?.avg ?: 0f
                )
            }
            
            updateState {
                copy(studentStatInfoFetchState = RemoteState.Success, studentStatInfo = stats)
            }
        }
    }
    
    private fun getNotifications() {
        if (viewState.studentInfo != null) return
        
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(notificationsFetchState = RemoteState.Error(it)) }
            }
        ) {
            updateState { copy(notificationsFetchState = RemoteState.Loading) }
            
            val response = accountService.getFeed(appValues.dgtuToken.get()!!)
            
            val notifications = response.data.feed.mapIndexed { i, it ->
                Notification(
                    id = i,
                    category = it.category,
                    date = it.fullDate,
                    link = run {
                        val linkText = it.linkText?.takeIf { it.isNotBlank() }
                        val link = it.link?.takeIf { it.isNotBlank() }
                        if (linkText != null && link != null) linkText to link else null
                    },
                    it.html?.let { AnnotatedString.fromHtml(it) }
                        ?: it.text?.let { AnnotatedString.fromHtml(it) }
                        ?: buildAnnotatedString({}),
                )
            }
            
            updateState {
                copy(notificationsFetchState = RemoteState.Success, notifications = notifications)
            }
        }
    }
    
    private fun getProfileInfo() {
        if (viewState.studentInfo != null) return
        
        viewModelScope.launchSafe(
            debug = debug,
            onFailure = {
                updateState { copy(studentInfoFetchState = RemoteState.Error(it)) }
            }
        ) {
            updateState { copy(studentInfoFetchState = RemoteState.Loading) }
            
            val response = accountService.getStudentInfo(
                appValues.dgtuToken.get()!!,
                appValues.dgtuStudentId.get()!!
            )
            
            val profileInfo = with(response.data) {
                DgtuStudentInfo(
                    name = name,
                    surname = surname,
                    group = ScheduleSearch.Group(group?.item1 ?: "", (group?.item2 ?: 0).toString()),
                    kafName = kaf?.kafName ?: "",
                    fullName = fullName,
                    middleName = middleName,
                    numRecordBook = numRecordBook,
                    numberMobile = numberMobile,
                    birthday = birthday,
                    nationality = nationality,
                    email = email,
                    login = login,
                    admissionYear = admissionYear,
                    lastEnterDateLocalDate = lastEnterDate,
                    course = course,
                    faculty = facul?.faculName ?: faculty,
                    conditionsEducation = conditionsEducation,
                    trainingDirection = trainingDirection,
                    photoLink = photoLink?.let { accountService.generateImageLink(it) } ?: "",
                    online = online,
                    message = message,
                    htmlBlock = htmlBlock,
                    vkID = vkID,
                    googleID = googleID,
                    yandexID = yandexID,
                    telegramID = telegramID,
                    maxID = maxID,
                    chatLink = chatLink ?: ""
                )
            }
            
            updateState {
                copy(
                    studentInfoFetchState = RemoteState.Success,
                    studentInfo = profileInfo
                )
            }
        }
    }
}