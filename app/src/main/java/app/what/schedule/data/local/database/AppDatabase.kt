package app.what.schedule.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        LessonDBO::class,
        OneTimeUnitDBO::class,
        GroupDBO::class,
        TeacherDBO::class,
        DayScheduleDBO::class,
        RequestDBO::class
    ],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val lessonsDao: LessonDAO
    abstract val otUnitsDao: OtUnitDAO
    abstract val groupsDao: GroupsDAO
    abstract val teachersDao: TeachersDAO
    abstract val requestsDao: RequestsDAO
    abstract val daySchedulesDao: DayScheduleDAO
}