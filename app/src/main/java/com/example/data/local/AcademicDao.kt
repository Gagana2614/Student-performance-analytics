package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicDao {

    // Students
    @Query("SELECT * FROM students ORDER BY createdAt DESC, fullName ASC")
    fun observeStudents(): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: String)

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY semester ASC, code ASC")
    fun observeSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)

    // Marks
    @Query("SELECT * FROM marks ORDER BY recordedAt DESC")
    fun observeMarks(): Flow<List<MarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMark(mark: MarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<MarkEntity>)

    @Query("DELETE FROM marks WHERE id = :id")
    suspend fun deleteMarkById(id: String)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY lastUpdated DESC")
    fun observeAttendance(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendance: List<AttendanceEntity>)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: String)

    // Demo & Reset Operations
    @Query("DELETE FROM marks WHERE isDemo = 1")
    suspend fun deleteDemoMarks()

    @Query("DELETE FROM attendance WHERE isDemo = 1")
    suspend fun deleteDemoAttendance()

    @Query("DELETE FROM students WHERE isDemo = 1")
    suspend fun deleteDemoStudents()

    @Query("DELETE FROM subjects WHERE isDemo = 1")
    suspend fun deleteDemoSubjects()

    @Query("DELETE FROM marks")
    suspend fun clearAllMarks()

    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendance()

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    @Query("DELETE FROM subjects")
    suspend fun clearAllSubjects()

    @Transaction
    suspend fun clearDemoRecordsOnly() {
        deleteDemoMarks()
        deleteDemoAttendance()
        deleteDemoStudents()
        deleteDemoSubjects()
    }

    @Transaction
    suspend fun replaceAllWithSeed(
        students: List<StudentEntity>,
        subjects: List<SubjectEntity>,
        marks: List<MarkEntity>,
        attendance: List<AttendanceEntity>
    ) {
        clearAllMarks()
        clearAllAttendance()
        clearAllStudents()
        clearAllSubjects()
        insertStudents(students)
        insertSubjects(subjects)
        insertMarks(marks)
        insertAttendanceList(attendance)
    }
}
