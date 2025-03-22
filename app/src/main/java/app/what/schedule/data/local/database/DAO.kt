package app.what.schedule.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface LessonDAO {
    @Insert
    suspend fun insert(lesson: LessonDBO)

    @Insert
    suspend fun insert(lesson: List<LessonDBO>)

    @Update
    suspend fun update(lesson: LessonDBO)

    @Transaction
    @Query(
        """SELECT * FROM lessons
            WHERE lessons.institutionId = :institutionId
            GROUP BY lessons.id
            ORDER BY date ASC"""
    )
    fun selectByInstitution(institutionId: String): Flow<List<LessonWithOtUnitsDBO>>

    @Transaction
    @Query(
        """SELECT * FROM lessons
            WHERE lessons.institutionId = :institutionId
            GROUP BY lessons.id
            ORDER BY date ASC"""
    )
    suspend fun selectOnesByInstitution(institutionId: String): List<LessonWithOtUnitsDBO>

    @Transaction
    @Query(
        """SELECT * FROM lessons
            WHERE lessons.institutionId = :institutionId AND lessons.date = :date
            GROUP BY lessons.id
            ORDER BY lessons.date ASC"""
    )
    fun selectByInstitution(
        institutionId: String,
        date: LocalDate
    ): Flow<List<LessonWithOtUnitsDBO>>

    @Delete
    suspend fun delete(lesson: LessonDBO)
}

@Dao
interface OtUnitDAO {
    @Insert
    suspend fun insert(unit: OneTimeUnitDBO)

    @Insert
    suspend fun insert(unit: List<OneTimeUnitDBO>)

    @Update
    suspend fun update(unit: OneTimeUnitDBO)

    @Transaction
    @Query("SELECT * FROM ot_units WHERE ot_units.lessonId = :lessonId")
    suspend fun getByLesson(lessonId: String): List<OneTimeUnitWithRelations>

    @Delete
    suspend fun delete(unit: OneTimeUnitDBO)
}

@Dao
interface GroupsDAO {
    @Insert
    suspend fun insert(group: GroupDBO)

    @Insert
    suspend fun insert(group: List<GroupDBO>)

    @Update
    suspend fun update(group: GroupDBO)

    @Query("SELECT * FROM groups WHERE groups.institutionId = :institutionId")
    suspend fun selectByInstitution(institutionId: String): List<GroupDBO>

    @Query("SELECT * FROM groups WHERE groups.id = :id")
    suspend fun selectById(id: String): GroupDBO

    @Query("SELECT * FROM groups WHERE groups.name = :name")
    suspend fun selectByName(name: String): GroupDBO

    @Query("SELECT * FROM groups WHERE groups.year = :year")
    suspend fun selectByYear(year: Int): GroupDBO

    @Delete
    suspend fun delete(group: GroupDBO)
}

@Dao
interface TeachersDAO {
    @Insert
    suspend fun insert(teacher: TeacherDBO)

    @Insert
    suspend fun insert(teacher: List<TeacherDBO>)

    @Update
    suspend fun update(teacher: TeacherDBO)

    @Query("SELECT * FROM teachers WHERE teachers.institutionId = :institutionId")
    suspend fun selectByInstitution(institutionId: String): List<TeacherDBO>

    @Query("SELECT * FROM teachers WHERE teachers.id = :id")
    suspend fun selectById(id: String): TeacherDBO

    @Query("SELECT * FROM teachers WHERE teachers.name = :name")
    suspend fun selectByName(name: String): TeacherDBO

    @Delete
    suspend fun delete(teacher: TeacherDBO)
}