package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentEntity::class,
        SubjectEntity::class,
        MarkEntity::class,
        AttendanceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EduMetricsDatabase : RoomDatabase() {
    abstract fun academicDao(): AcademicDao

    companion object {
        @Volatile
        private var INSTANCE: EduMetricsDatabase? = null

        fun getInstance(context: Context): EduMetricsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EduMetricsDatabase::class.java,
                    "edumetrics_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
