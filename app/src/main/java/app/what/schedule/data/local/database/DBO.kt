package app.what.schedule.data.local.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.what.schedule.data.remote.api.LessonState
import app.what.schedule.data.remote.api.LessonType
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "lessons")
data class LessonDBO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val institutionId: String,
    val date: LocalDate,
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
    val groupInstitutionId: String,
    val groupId: String,
    val teacherInstitutionId: String,
    val teacherId: String,
    val auditory: String,
    val building: String,
)

@Entity(tableName = "groups", primaryKeys = ["institutionId", "id"])
data class GroupDBO(
    val institutionId: String,
    val name: String,
    val id: String = name,
    val year: Int? = null
)

@Entity(tableName = "teachers", primaryKeys = ["institutionId", "id"])
data class TeacherDBO(
    val institutionId: String,
    val name: String,
    val id: String = name
)

data class LessonWithOtUnitsDBO(
    @Embedded val lesson: LessonDBO,
    @Relation(parentColumn = "id", entityColumn = "lessonId")
    val otUnits: List<OneTimeUnitDBO>
)

data class OneTimeUnitWithRelations(
    @Embedded val unit: OneTimeUnitDBO,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "id",
        entity = GroupDBO::class
    )
    val group: GroupDBO,
    @Relation(
        parentColumn = "teacherId",
        entityColumn = "id",
        entity = TeacherDBO::class
    )
    val teacher: TeacherDBO
)