package app.what.schedule.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate

@Dao
interface RequestsDAO {
    @Insert
    suspend fun insert(request: RequestDBO): Long

    @Insert
    suspend fun insert(requests: List<RequestDBO>)

    @Query("SELECT * FROM requests")
    suspend fun selectAll(): List<RequestDBO>

    @Transaction
    @Query(
        """
        SELECT * FROM requests 
        WHERE institutionId = :institutionId 
        AND createdAt > :afterDate 
        AND `query` = :query
        ORDER BY createdAt DESC 
        LIMIT 1
    """
    )
    suspend fun selectLast(
        institutionId: String,
        query: String,
        afterDate: LocalDate = LocalDate.MIN
    ): RequestSDBO?

    @Update
    suspend fun update(request: RequestDBO)


    @Query("DELETE FROM requests WHERE requests.createdAt < :currentDate")
    suspend fun deleteOld(currentDate: LocalDate = LocalDate.now())

    @Delete
    suspend fun delete(request: RequestDBO)
}

@Dao
interface DayScheduleDAO {
    @Insert
    suspend fun insert(daySchedule: DayScheduleDBO): Long

    @Insert
    suspend fun insert(daySchedules: List<DayScheduleDBO>)

    @Update
    suspend fun update(daySchedule: DayScheduleDBO)

    @Delete
    suspend fun delete(daySchedule: DayScheduleDBO)
}

@Dao
interface LessonDAO {
    @Insert
    suspend fun insert(lesson: LessonDBO): Long

    @Insert
    suspend fun insert(lessons: List<LessonDBO>)

    @Update
    suspend fun update(lesson: LessonDBO)

    @Delete
    suspend fun delete(lesson: LessonDBO)
}

@Dao
interface OtUnitDAO {
    @Insert
    suspend fun insert(unit: OneTimeUnitDBO): Long

    @Insert
    suspend fun insert(unit: List<OneTimeUnitDBO>)

    @Update
    suspend fun update(unit: OneTimeUnitDBO)

    @Delete
    suspend fun delete(unit: OneTimeUnitDBO)
}

@Dao
interface GroupsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: GroupDBO): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: List<GroupDBO>)

    @Update
    suspend fun update(group: GroupDBO)

    @Query("SELECT * FROM groups WHERE groups.institutionId = :institutionId")
    suspend fun selectByInstitution(institutionId: String): List<GroupDBO>

    @Query("SELECT * FROM groups WHERE groups.id = :id")
    suspend fun selectById(id: String): GroupDBO

    @Query("SELECT groups.id FROM groups WHERE groups.institutionId = :institutionId AND groups.groupId = :id")
    suspend fun selectIdByGroupId(institutionId: String, id: String): Long?

    @Query("SELECT * FROM groups WHERE groups.name = :name")
    suspend fun selectByName(name: String): GroupDBO

    @Query("SELECT * FROM groups WHERE groups.year = :year")
    suspend fun selectByYear(year: Int): GroupDBO

    @Delete
    suspend fun delete(group: GroupDBO)
}

@Dao
interface TeachersDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(teacher: TeacherDBO): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(teacher: List<TeacherDBO>)

    @Update
    suspend fun update(teacher: TeacherDBO)

    @Query("SELECT * FROM teachers WHERE teachers.institutionId = :institutionId")
    suspend fun selectByInstitution(institutionId: String): List<TeacherDBO>

    @Query("SELECT * FROM teachers WHERE teachers.id = :id")
    suspend fun selectById(id: String): TeacherDBO

    @Query("SELECT teachers.id FROM teachers WHERE teachers.institutionId = :institutionId AND teachers.teacherId = :id")
    suspend fun selectIdByTeacherId(institutionId: String, id: String): Long?

    @Query("SELECT * FROM teachers WHERE teachers.name = :name")
    suspend fun selectByName(name: String): TeacherDBO

    @Delete
    suspend fun delete(teacher: TeacherDBO)
}