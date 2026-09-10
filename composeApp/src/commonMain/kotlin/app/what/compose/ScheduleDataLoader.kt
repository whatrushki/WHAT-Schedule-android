package app.what.compose

import app.what.domain.models.DaySchedule
import app.what.domain.models.ScheduleSearch
import app.what.schedule.dgtu.DGTUScheduleClient
import app.what.schedule.iubip.IUBIPScheduleClient
import app.what.schedule.rinh.RINHScheduleClient
import app.what.schedule.rksi.RKSIScheduleClient
import app.what.schedule.rksi.parser.XlsxReader
import io.ktor.client.HttpClient

interface ScheduleDataLoader {
    suspend fun getSearches(university: University): List<ScheduleSearch>
    suspend fun getSchedule(university: University, search: ScheduleSearch): List<DaySchedule>
}

class DefaultScheduleDataLoader(
    private val httpClient: HttpClient,
    xlsxReader: XlsxReader = createXlsxReader()
) : ScheduleDataLoader {
    private val rksiClient = RKSIScheduleClient(client = httpClient, xlsxReader = xlsxReader)
    private val dgtuClient = DGTUScheduleClient(httpClient)
    private val iubipClient = IUBIPScheduleClient(httpClient)
    private val rinhClient = RINHScheduleClient(httpClient)

    override suspend fun getSearches(university: University): List<ScheduleSearch> {
        return when (university) {
            University.RKSI -> {
                val groups = rksiClient.getGroups().map { ScheduleSearch.Group(it.name, it.id) }
                val teachers = rksiClient.getTeachers().map { ScheduleSearch.Teacher(it.name, it.id) }
                groups + teachers
            }
            University.DGTU -> dgtuClient.getGroups().map { ScheduleSearch.Group(it.name, it.id) }
            University.IUBIP -> iubipClient.getGroups().map { ScheduleSearch.Group(it.name, it.id) }
            University.RINH -> rinhClient.getGroups().map { ScheduleSearch.Group(it.name, it.id) }
        }
    }

    override suspend fun getSchedule(university: University, search: ScheduleSearch): List<DaySchedule> {
        val dtos = when (university) {
            University.RKSI -> {
                if (search is ScheduleSearch.Teacher) rksiClient.getTeacherSchedule(search.name)
                else rksiClient.getGroupSchedule(search.name)
            }
            University.DGTU -> {
                if (search is ScheduleSearch.Teacher) dgtuClient.getTeacherSchedule(search.id)
                else dgtuClient.getGroupSchedule(search.id)
            }
            University.IUBIP -> {
                if (search is ScheduleSearch.Teacher) iubipClient.getTeacherSchedule(search.id)
                else iubipClient.getGroupSchedule(search.name)
            }
            University.RINH -> {
                if (search is ScheduleSearch.Teacher) rinhClient.getTeacherSchedule(search.id)
                else rinhClient.getGroupSchedule(search.name)
            }
        }
        return dtos.map { it.toDomain() }
    }
}
