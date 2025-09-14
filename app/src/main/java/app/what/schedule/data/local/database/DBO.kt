package app.what.schedule.data.local.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.what.schedule.data.remote.api.LessonState
import app.what.schedule.data.remote.api.LessonType
import app.what.schedule.data.remote.api.LessonsScheduleType
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "requests")
data class RequestDBO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val institutionId: String,
    val query: String,
    val createdAt: LocalDate = LocalDate.now(),
)

@Entity(
    tableName = "days",
    foreignKeys = [ForeignKey(
        parentColumns = ["id"],
        childColumns = ["fromRequest"],
        entity = RequestDBO::class,
        onDelete = ForeignKey.CASCADE
    )]
)
data class DayScheduleDBO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromRequest: Long,
    val date: LocalDate,
    val scheduleType: LessonsScheduleType,
)

@Entity(
    tableName = "lessons",
    foreignKeys = [ForeignKey(
        parentColumns = ["id"],
        childColumns = ["fromDay"],
        entity = DayScheduleDBO::class,
        onDelete = ForeignKey.CASCADE
    )]
)
data class LessonDBO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromDay: Long,
    val number: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val subject: String,
    val type: LessonType,
    val state: LessonState = LessonState.COMMON,
)

@Entity(
    tableName = "ot_units",
    foreignKeys = [ForeignKey(
        entity = LessonDBO::class,
        parentColumns = ["id"],
        childColumns = ["lessonId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class OneTimeUnitDBO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lessonId: Long,
    val groupId: Long,
    val teacherId: Long,
    val auditory: String,
    val building: String,
)

@Entity(
    tableName = "groups",
    indices = [Index(value = ["institutionId", "groupId"], unique = true)]
)
data class GroupDBO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val institutionId: String,
    val name: String,
    val groupId: String = name,
    val year: Int? = null,
    val favorite: Boolean = false
)

@Entity(
    tableName = "teachers",
    indices = [Index(value = ["institutionId", "teacherId"], unique = true)]
)
data class TeacherDBO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val institutionId: String,
    val name: String,
    val teacherId: String = name,
    val favorite: Boolean = false
)

data class RequestSDBO(
    @Embedded val request: RequestDBO,
    @Relation(parentColumn = "id", entityColumn = "fromRequest", entity = DayScheduleDBO::class)
    val daySchedules: List<DayScheduleSDBO>
)

data class DayScheduleSDBO(
    @Embedded val daySchedule: DayScheduleDBO,
    @Relation(parentColumn = "id", entityColumn = "fromDay", entity = LessonDBO::class)
    val lessons: List<LessonSDBO>
)

data class LessonSDBO(
    @Embedded val lesson: LessonDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId",
        entity = OneTimeUnitDBO::class
    )
    val otUnits: List<OtUnitSDBO>
)

data class OtUnitSDBO(
    @Embedded val unit: OneTimeUnitDBO,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "id",
        entity = GroupDBO::class
    ) val group: GroupDBO,
    @Relation(
        parentColumn = "teacherId",
        entityColumn = "id",
        entity = TeacherDBO::class
    ) val teacher: TeacherDBO
)