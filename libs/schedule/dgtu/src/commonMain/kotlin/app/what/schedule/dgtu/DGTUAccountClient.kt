package app.what.schedule.dgtu

import app.what.schedule.core.network.setData
import app.what.schedule.dgtu.models.ApiResponse
import app.what.schedule.dgtu.models.DGTUApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlin.math.absoluteValue

class DGTUAccountClient(
    private val client: HttpClient,
    private val accountBaseUrl: String = "https://lk.donstu.ru/api"
) {
    fun generateImageLink(url: String): String {
        val host = try {
            Url(accountBaseUrl).host
        } catch (_: Exception) {
            "lk.donstu.ru"
        }
        return "https://$host$url"
    }

    suspend fun auth(
        login: String,
        password: String,
        fingerprint: String
    ): ApiResponse<ApiResponse<DGTUApi.Auth.LoginResponse>> = client.post("$accountBaseUrl/tokenauth") {
        contentType(ContentType.Application.Json)
        setBody(DGTUApi.Auth.Login(login, password, fingerprint))
    }.body()

    suspend fun getUnreadMessagesId(token: String): ApiResponse<DGTUApi.Mails.GetUnreadIdsResponse> =
        client.get("$accountBaseUrl/Mail/CheckMail") {
            bearerAuth(token)
        }.body()

    suspend fun getMails(
        token: String,
        data: DGTUApi.Mails.GetAllRequest
    ): ApiResponse<DGTUApi.Mails.GetAllResponse> = client.get("$accountBaseUrl/Mail/InboxMail") {
        bearerAuth(token)
        setData(data)
    }.body()

    suspend fun getDetailMail(
        token: String,
        threadId: Int,
        messageId: Int
    ): ApiResponse<DGTUApi.Mails.GetAllResponse> = client.get("$accountBaseUrl/Mail/InboxMail") {
        bearerAuth(token)
        parameter("id", threadId)
        parameter("messageID", messageId)
        parameter("type", "0")
    }.body()

    suspend fun getZachBook(token: String): ApiResponse<DGTUApi.ZachBook.GetResponse> =
        client.get("$accountBaseUrl/EducationalActivity/ZachBook?studentID=undefined") {
            bearerAuth(token)
            parameter("studentID", "undefined")
        }.body()

    suspend fun getStudentInfo(
        token: String,
        studentId: Int
    ): ApiResponse<DGTUApi.Profile.GetStudentInfoResponse> =
        client.get("$accountBaseUrl/UserInfo/Student") {
            bearerAuth(token)
            parameter("studentID", studentId)
        }.body()

    suspend fun generatePassNumber(token: String): ApiResponse<Int> =
        client.get("$accountBaseUrl/UserInfo/PassGeneration") {
            bearerAuth(token)
        }.body()

    suspend fun getEvents(token: String): ApiResponse<DGTUApi.Events.GetAllResponse> =
        client.get("$accountBaseUrl/EventsCalendar") {
            bearerAuth(token)
        }.body()

    suspend fun getDetailEvent(
        token: String,
        eventId: String
    ): ApiResponse<DGTUApi.Events.GetDetailEventInfo> =
        client.get("$accountBaseUrl/EventsCalendar/Event") {
            bearerAuth(token)
            parameter("eventID", eventId)
        }.body()

    suspend fun getFeed(token: String): ApiResponse<DGTUApi.Feeds.GetAllResponse> =
        client.get("$accountBaseUrl/Feed") {
            bearerAuth(token)
        }.body()

    suspend fun getMarksCount(
        token: String,
        studentId: Int
    ): ApiResponse<DGTUApi.Stats.GetMarksCountResponse> =
        client.get("$accountBaseUrl/EducationalActivity/StatisticsMarksCount") {
            bearerAuth(token)
            parameter("studentID", studentId.absoluteValue)
        }.body()

    suspend fun getAvgMark(
        token: String,
        studentId: Int
    ): ApiResponse<DGTUApi.Stats.GetAvgMarkResponse> =
        client.get("$accountBaseUrl/EducationalActivity/StudentAvgMark") {
            bearerAuth(token)
            parameter("studentID", studentId.absoluteValue)
        }.body()
}
