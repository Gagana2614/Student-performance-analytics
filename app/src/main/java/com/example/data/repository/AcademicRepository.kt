package com.example.data.repository

import com.example.data.local.AcademicDao
import com.example.data.local.SeedData
import com.example.data.local.SettingsDataStore
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.AcademicSettings
import com.example.domain.model.AttendanceRecord
import com.example.domain.model.MarkRecord
import com.example.domain.model.Student
import com.example.domain.model.Subject
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Backend-ready repository interface.
 * Swap LocalAcademicRepository for a remote Retrofit/REST/Firestore implementation
 * without changing ViewModel or Compose UI layers.
 */
interface AcademicRepository {
    val studentsFlow: Flow<List<Student>>
    val subjectsFlow: Flow<List<Subject>>
    val marksFlow: Flow<List<MarkRecord>>
    val attendanceFlow: Flow<List<AttendanceRecord>>
    val settingsFlow: Flow<AcademicSettings>

    suspend fun ensureInitialSeed()
    suspend fun upsertStudent(student: Student)
    suspend fun deleteStudent(id: String)

    suspend fun upsertSubject(subject: Subject)
    suspend fun deleteSubject(id: String)

    suspend fun upsertMark(mark: MarkRecord)
    suspend fun deleteMark(id: String)

    suspend fun upsertAttendance(attendance: AttendanceRecord)
    suspend fun deleteAttendance(id: String)

    suspend fun updateSettings(settings: AcademicSettings)
    suspend fun toggleTheme(darkTheme: Boolean)
    suspend fun updateRole(role: UserRole)
    suspend fun resetToDemoData()
    suspend fun clearDemoRecordsOnly()
}

class LocalAcademicRepository(
    private val dao: AcademicDao,
    private val settingsStore: SettingsDataStore
) : AcademicRepository {

    override val studentsFlow: Flow<List<Student>> =
        dao.observeStudents().map { list -> list.map { it.toDomain() } }

    override val subjectsFlow: Flow<List<Subject>> =
        dao.observeSubjects().map { list -> list.map { it.toDomain() } }

    override val marksFlow: Flow<List<MarkRecord>> =
        dao.observeMarks().map { list -> list.map { it.toDomain() } }

    override val attendanceFlow: Flow<List<AttendanceRecord>> =
        dao.observeAttendance().map { list -> list.map { it.toDomain() } }

    override val settingsFlow: Flow<AcademicSettings> = settingsStore.settingsFlow

    override suspend fun ensureInitialSeed() {
        val seeded = settingsStore.hasSeededFlow.first()
        if (!seeded && dao.getStudentCount() == 0 && dao.getSubjectCount() == 0) {
            val bundle = SeedData.createSeedBundle()
            dao.replaceAllWithSeed(
                students = bundle.students.map { it.toEntity() },
                subjects = bundle.subjects.map { it.toEntity() },
                marks = bundle.marks.map { it.toEntity() },
                attendance = bundle.attendance.map { it.toEntity() }
            )
            settingsStore.markSeeded()
        }
    }

    override suspend fun upsertStudent(student: Student) = dao.upsertStudent(student.toEntity())
    override suspend fun deleteStudent(id: String) = dao.deleteStudentById(id)

    override suspend fun upsertSubject(subject: Subject) = dao.upsertSubject(subject.toEntity())
    override suspend fun deleteSubject(id: String) = dao.deleteSubjectById(id)

    override suspend fun upsertMark(mark: MarkRecord) = dao.upsertMark(mark.toEntity())
    override suspend fun deleteMark(id: String) = dao.deleteMarkById(id)

    override suspend fun upsertAttendance(attendance: AttendanceRecord) = dao.upsertAttendance(attendance.toEntity())
    override suspend fun deleteAttendance(id: String) = dao.deleteAttendanceById(id)

    override suspend fun updateSettings(settings: AcademicSettings) = settingsStore.updateSettings(settings)
    override suspend fun toggleTheme(darkTheme: Boolean) = settingsStore.toggleTheme(darkTheme)
    override suspend fun updateRole(role: UserRole) = settingsStore.updateRole(role)

    override suspend fun resetToDemoData() {
        val bundle = SeedData.createSeedBundle()
        dao.replaceAllWithSeed(
            students = bundle.students.map { it.toEntity() },
            subjects = bundle.subjects.map { it.toEntity() },
            marks = bundle.marks.map { it.toEntity() },
            attendance = bundle.attendance.map { it.toEntity() }
        )
        settingsStore.markSeeded()
    }

    override suspend fun clearDemoRecordsOnly() {
        dao.clearDemoRecordsOnly()
        settingsStore.markSeeded()
    }
}
