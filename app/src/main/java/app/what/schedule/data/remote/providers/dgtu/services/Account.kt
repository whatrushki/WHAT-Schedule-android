package app.what.schedule.data.remote.providers.dgtu.services

import androidx.core.net.toUri
import app.what.schedule.data.remote.providers.dgtu.ApiResponse
import app.what.schedule.data.remote.providers.dgtu.DGTUApi
import app.what.schedule.utils.setData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.math.absoluteValue

class DGTUAccountService(
    private val client: HttpClient
) {
    companion object {
        private const val ACCOUNT_BASE_URL = "https://lk.donstu.ru/api"
    }
    
    fun generateImageLink(url: String) = "https://" + ACCOUNT_BASE_URL.toUri().host + url
    
    suspend fun auth(
        login: String,
        password: String,
        fingerprint: String
    ) = client.post("$ACCOUNT_BASE_URL/tokenauth") {
        contentType(ContentType.Application.Json)
        setBody(DGTUApi.Auth.Login(login, password, fingerprint))
    }.body<ApiResponse<ApiResponse<DGTUApi.Auth.LoginResponse>>>()
    
    suspend fun getUnreadMessagesId(token: String) =
        client.get("$ACCOUNT_BASE_URL/Mail/CheckMail") {
            bearerAuth(token)
        }.body<ApiResponse<DGTUApi.Mails.GetUnreadIdsResponse>>()
    
    suspend fun getMails(
        token: String,
        data: DGTUApi.Mails.GetAllRequest
    ) = client.get("$ACCOUNT_BASE_URL/Mail/InboxMail") {
        bearerAuth(token)
        setData(data)
    }.body<ApiResponse<DGTUApi.Mails.GetAllResponse>>()
    
    suspend fun getDetailMail(
        token: String,
        threadId: Int,
        messageId: Int
    ) = client.get("$ACCOUNT_BASE_URL/Mail/InboxMail") {
        bearerAuth(token)
        parameter("id", threadId)
        parameter("messageID", messageId)
        parameter("type", "0")
    }.body<ApiResponse<DGTUApi.Mails.GetAllResponse>>()
    
    suspend fun getZachBook(
        token: String
    ) = client.get("$ACCOUNT_BASE_URL/EducationalActivity/ZachBook?studentID=undefined") {
        bearerAuth(token)
        parameter("studentID", "undefined")
    }.body<ApiResponse<DGTUApi.ZachBook.GetResponse>>()
    
    suspend fun getStudentInfo(
        token: String,
        studentId: Int
    ) = client.get("$ACCOUNT_BASE_URL/UserInfo/Student") {
        bearerAuth(token)
        parameter("studentID", studentId)
    }.body<ApiResponse<DGTUApi.Profile.GetStudentInfoResponse>>()
    
    suspend fun generatePassNumber(
        token: String
    ) = client.get("$ACCOUNT_BASE_URL/UserInfo/PassGeneration") {
        bearerAuth(token)
    }.body<ApiResponse<Int>>()
    
    suspend fun getEvents(
        token: String
    ) = client.get("$ACCOUNT_BASE_URL/EventsCalendar") {
        bearerAuth(token)
    }.body<ApiResponse<DGTUApi.Events.GetAllResponse>>()
    
    
    suspend fun getDetailEvent(
        token: String, eventId: String
    ) = client.get("$ACCOUNT_BASE_URL/EventsCalendar/Event") {
        bearerAuth(token)
        parameter("eventID", eventId)
    }.body<ApiResponse<DGTUApi.Events.GetDetailEventInfo>>()
    
    suspend fun getFeed(
        token: String
    ) = client.get("$ACCOUNT_BASE_URL/Feed") {
        bearerAuth(token)
    }.body<ApiResponse<DGTUApi.Feeds.GetAllResponse>>()
    
    suspend fun getMarksCount(
        token: String,
        studentId: Int
    ) = client.get("$ACCOUNT_BASE_URL/EducationalActivity/StatisticsMarksCount") {
        bearerAuth(token)
        parameter("studentID", studentId.absoluteValue)
    }.body<ApiResponse<DGTUApi.Stats.GetMarksCountResponse>>()
    
    suspend fun getAvgMark(
        token: String,
        studentId: Int
    ) = client.get("$ACCOUNT_BASE_URL/EducationalActivity/StudentAvgMark") {
        bearerAuth(token)
        parameter("studentID", studentId.absoluteValue)
    }.body<ApiResponse<DGTUApi.Stats.GetAvgMarkResponse>>()
}
