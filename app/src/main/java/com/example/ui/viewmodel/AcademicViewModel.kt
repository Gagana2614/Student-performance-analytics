package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EduMetricsDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.repository.AcademicRepository
import com.example.data.repository.LocalAcademicRepository
import com.example.domain.model.AcademicSettings
import com.example.domain.model.AnalyticsEngine
import com.example.domain.model.AppNotification
import com.example.domain.model.AttendanceRecord
import com.example.domain.model.Department
import com.example.domain.model.DepartmentBenchmark
import com.example.domain.model.MarkRecord
import com.example.domain.model.NotificationSeverity
import com.example.domain.model.PerformanceTier
import com.example.domain.model.SemesterTrend
import com.example.domain.model.Student
import com.example.domain.model.StudentAnalytics
import com.example.domain.model.StudentStatus
import com.example.domain.model.Subject
import com.example.domain.model.SubjectAnalytics
import com.example.domain.model.UserRole
import com.example.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UiMessage(val text: String, val isError: Boolean = false)

data class AcademicUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val students: List<Student> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val marks: List<MarkRecord> = emptyList(),
    val attendance: List<AttendanceRecord> = emptyList(),
    val settings: AcademicSettings = AcademicSettings(),
    val studentAnalytics: List<StudentAnalytics> = emptyList(),
    val subjectAnalytics: List<SubjectAnalytics> = emptyList(),
    val semesterTrends: List<SemesterTrend> = emptyList(),
    val departmentBenchmarks: List<DepartmentBenchmark> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val globalSearch: String = ""
)

class AcademicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AcademicRepository = LocalAcademicRepository(
        dao = EduMetricsDatabase.getInstance(application).academicDao(),
        settingsStore = SettingsDataStore(application)
    )

    private val isLoadingFlow = MutableStateFlow(true)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val globalSearchFlow = MutableStateFlow("")
    private val readNotificationIds = MutableStateFlow<Set<String>>(emptySet())

    private val _uiMessages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)
    val uiMessages = _uiMessages.asSharedFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            isLoadingFlow.value = true
            errorFlow.value = null
            try {
                repository.ensureInitialSeed()
            } catch (e: Exception) {
                errorFlow.value = e.message ?: "Unable to initialize academic database."
            } finally {
                isLoadingFlow.value = false
            }
        }
    }

    private val baseDataFlow = combine(
        repository.studentsFlow,
        repository.subjectsFlow,
        repository.marksFlow,
        repository.attendanceFlow,
        repository.settingsFlow
    ) { students, subjects, marks, attendance, settings ->
        BaseData(students, subjects, marks, attendance, settings)
    }

    private data class BaseData(
        val students: List<Student>,
        val subjects: List<Subject>,
        val marks: List<MarkRecord>,
        val attendance: List<AttendanceRecord>,
        val settings: AcademicSettings
    )

    val uiState: StateFlow<AcademicUiState> = combine(
        baseDataFlow,
        isLoadingFlow,
        errorFlow,
        globalSearchFlow,
        readNotificationIds
    ) { base, loading, error, search, readIds ->
        val studentAnalytics = AnalyticsEngine.computeStudentAnalytics(
            students = base.students,
            subjects = base.subjects,
            marks = base.marks,
            attendance = base.attendance,
            settings = base.settings
        )
        val subjectAnalytics = AnalyticsEngine.computeSubjectAnalytics(
            subjects = base.subjects,
            marks = base.marks,
            attendance = base.attendance,
            settings = base.settings
        )
        val semesterTrends = AnalyticsEngine.computeSemesterTrends(
            subjects = base.subjects,
            marks = base.marks,
            attendance = base.attendance,
            settings = base.settings
        )
        val deptBenchmarks = AnalyticsEngine.computeDepartmentBenchmarks(studentAnalytics)

        val notifications = buildNotifications(
            studentAnalytics = studentAnalytics,
            subjectAnalytics = subjectAnalytics,
            settings = base.settings,
            readIds = readIds
        )

        AcademicUiState(
            isLoading = loading,
            error = error,
            students = base.students,
            subjects = base.subjects,
            marks = base.marks,
            attendance = base.attendance,
            settings = base.settings,
            studentAnalytics = studentAnalytics,
            subjectAnalytics = subjectAnalytics,
            semesterTrends = semesterTrends,
            departmentBenchmarks = deptBenchmarks,
            notifications = notifications,
            globalSearch = search
        )
    }.catch { err ->
        emit(AcademicUiState(isLoading = false, error = err.message ?: "Database error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AcademicUiState(isLoading = true)
    )

    private fun buildNotifications(
        studentAnalytics: List<StudentAnalytics>,
        subjectAnalytics: List<SubjectAnalytics>,
        settings: AcademicSettings,
        readIds: Set<String>
    ): List<AppNotification> {
        val list = mutableListOf<AppNotification>()
        val atRiskCount = studentAnalytics.count { it.tier == PerformanceTier.AT_RISK }
        val lowAttCount = studentAnalytics.count {
            it.attendancePercentage > 0.0 && it.attendancePercentage < settings.attendanceThreshold
        }
        val toughSubjects = subjectAnalytics.filter { it.enrolledCount > 0 && it.passRate < 75.0 }

        if (atRiskCount > 0) {
            val id = "notif-risk-$atRiskCount"
            list.add(
                AppNotification(
                    id = id,
                    title = "$atRiskCount At-Risk Students Flagged",
                    message = "Triggered by failing assessments or attendance shortfalls.",
                    severity = NotificationSeverity.DANGER,
                    targetRoute = Routes.AI_INSIGHTS,
                    isRead = readIds.contains(id)
                )
            )
        }
        if (lowAttCount > 0) {
            val id = "notif-att-$lowAttCount"
            list.add(
                AppNotification(
                    id = id,
                    title = "$lowAttCount Attendance Warnings",
                    message = "Students below the ${settings.attendanceThreshold}% attendance minimum.",
                    severity = NotificationSeverity.WARNING,
                    targetRoute = Routes.ATTENDANCE,
                    isRead = readIds.contains(id)
                )
            )
        }
        if (toughSubjects.isNotEmpty()) {
            val id = "notif-sub-${toughSubjects.size}"
            list.add(
                AppNotification(
                    id = id,
                    title = "Subject Pass Rate Notice",
                    message = "${toughSubjects.joinToString(", ") { it.subject.code }} under 75% pass rate.",
                    severity = NotificationSeverity.INFO,
                    targetRoute = Routes.ANALYTICS,
                    isRead = readIds.contains(id)
                )
            )
        }
        return list
    }

    fun setGlobalSearch(query: String) {
        globalSearchFlow.value = query
    }

    fun markAllNotificationsRead() {
        readNotificationIds.value = uiState.value.notifications.map { it.id }.toSet()
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val next = !uiState.value.settings.darkTheme
            repository.toggleTheme(next)
        }
    }

    fun setTheme(dark: Boolean) {
        viewModelScope.launch {
            repository.toggleTheme(dark)
        }
    }

    fun updateRole(role: UserRole) {
        viewModelScope.launch {
            repository.updateRole(role)
            _uiMessages.emit(UiMessage("Switched role view to ${role.label}"))
        }
    }

    // Student CRUD
    fun saveStudent(
        existingId: String?,
        rollNumber: String,
        fullName: String,
        email: String,
        department: Department,
        semester: Int,
        enrollmentYear: Int,
        status: StudentStatus
    ) {
        viewModelScope.launch {
            try {
                val existing = existingId?.let { id -> uiState.value.students.find { it.id == id } }
                val student = Student(
                    id = existing?.id ?: "stu-${System.currentTimeMillis()}",
                    rollNumber = rollNumber.trim().uppercase(),
                    fullName = fullName.trim(),
                    email = email.trim(),
                    department = department,
                    currentSemester = semester,
                    enrollmentYear = enrollmentYear,
                    status = status,
                    isDemo = false,
                    createdAt = existing?.createdAt ?: todayString()
                )
                repository.upsertStudent(student)
                _uiMessages.emit(
                    UiMessage(if (existing == null) "${student.fullName} enrolled." else "${student.fullName} updated.")
                )
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to save student.", isError = true))
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student.id)
                _uiMessages.emit(UiMessage("Removed ${student.fullName} and linked records."))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to delete student.", isError = true))
            }
        }
    }

    // Subject CRUD
    fun saveSubject(
        existingId: String?,
        code: String,
        name: String,
        department: Department,
        semester: Int,
        credits: Int,
        facultyName: String,
        maxInternal: Int,
        maxExternal: Int
    ) {
        viewModelScope.launch {
            try {
                val subject = Subject(
                    id = existingId ?: "sub-${System.currentTimeMillis()}",
                    code = code.trim().uppercase(),
                    name = name.trim(),
                    department = department,
                    semester = semester,
                    credits = credits,
                    facultyName = facultyName.trim(),
                    maxInternal = maxInternal,
                    maxExternal = maxExternal,
                    isDemo = false
                )
                repository.upsertSubject(subject)
                _uiMessages.emit(
                    UiMessage(if (existingId == null) "Subject ${subject.code} created." else "Subject ${subject.code} updated.")
                )
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to save subject.", isError = true))
            }
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            try {
                repository.deleteSubject(subject.id)
                _uiMessages.emit(UiMessage("Deleted ${subject.code} and linked assessments."))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to delete subject.", isError = true))
            }
        }
    }

    // Marks CRUD
    fun saveMark(
        existingId: String?,
        studentId: String,
        subjectId: String,
        internalMarks: Int,
        externalMarks: Int
    ) {
        viewModelScope.launch {
            try {
                val sub = uiState.value.subjects.find { it.id == subjectId }
                val match = existingId?.let { id -> uiState.value.marks.find { it.id == id } }
                    ?: uiState.value.marks.find { it.studentId == studentId && it.subjectId == subjectId }

                val record = MarkRecord(
                    id = match?.id ?: "mrk-${System.currentTimeMillis()}",
                    studentId = studentId,
                    subjectId = subjectId,
                    semester = sub?.semester ?: 1,
                    internalMarks = internalMarks,
                    externalMarks = externalMarks,
                    recordedAt = todayString(),
                    isDemo = false
                )
                repository.upsertMark(record)
                _uiMessages.emit(UiMessage(if (match == null) "Marks recorded." else "Marks updated."))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to save marks.", isError = true))
            }
        }
    }

    fun deleteMark(markId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMark(markId)
                _uiMessages.emit(UiMessage("Mark record deleted."))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to delete mark.", isError = true))
            }
        }
    }

    // Attendance CRUD
    fun saveAttendance(
        existingId: String?,
        studentId: String,
        subjectId: String,
        classesAttended: Int,
        totalClasses: Int
    ) {
        viewModelScope.launch {
            try {
                val sub = uiState.value.subjects.find { it.id == subjectId }
                val match = existingId?.let { id -> uiState.value.attendance.find { it.id == id } }
                    ?: uiState.value.attendance.find { it.studentId == studentId && it.subjectId == subjectId }

                val record = AttendanceRecord(
                    id = match?.id ?: "att-${System.currentTimeMillis()}",
                    studentId = studentId,
                    subjectId = subjectId,
                    semester = sub?.semester ?: 1,
                    classesAttended = classesAttended,
                    totalClasses = totalClasses,
                    lastUpdated = todayString(),
                    isDemo = false
                )
                repository.upsertAttendance(record)
                _uiMessages.emit(UiMessage(if (match == null) "Attendance log added." else "Attendance updated."))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to save attendance.", isError = true))
            }
        }
    }

    fun quickIncrementAttendance(record: AttendanceRecord, present: Boolean) {
        viewModelScope.launch {
            val updated = record.copy(
                classesAttended = record.classesAttended + if (present) 1 else 0,
                totalClasses = record.totalClasses + 1,
                lastUpdated = todayString(),
                isDemo = false
            )
            repository.upsertAttendance(updated)
            _uiMessages.emit(UiMessage(if (present) "Marked +1 Present" else "Marked +1 Absent"))
        }
    }

    fun deleteAttendance(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteAttendance(id)
                _uiMessages.emit(UiMessage("Attendance record removed."))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage(e.message ?: "Failed to delete attendance.", isError = true))
            }
        }
    }

    // Settings & Demo Data
    fun saveSettings(settings: AcademicSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _uiMessages.emit(UiMessage("System configuration saved."))
        }
    }

    fun restoreDemoData() {
        viewModelScope.launch {
            repository.resetToDemoData()
            _uiMessages.emit(UiMessage("Demonstration dataset restored."))
        }
    }

    fun clearDemoRecordsOnly() {
        viewModelScope.launch {
            repository.clearDemoRecordsOnly()
            _uiMessages.emit(UiMessage("Demo records cleared. Custom records preserved."))
        }
    }

    fun buildReportCsv(reportType: String): String {
        val state = uiState.value
        return when (reportType) {
            "subject-summary" -> {
                buildString {
                    appendLine("Code,Subject,Department,Semester,Evaluated,AvgScore,PassRate,AvgAttendance")
                    state.subjectAnalytics.forEach { sa ->
                        appendLine(
                            "${sa.subject.code},\"${sa.subject.name}\",\"${sa.subject.department.displayName}\",${sa.subject.semester},${sa.enrolledCount},${sa.averagePercentage},${sa.passRate},${sa.averageAttendance}"
                        )
                    }
                }
            }
            else -> {
                val filtered = when (reportType) {
                    "at-risk" -> state.studentAnalytics.filter { it.tier == PerformanceTier.AT_RISK }
                    "attendance-compliance" -> state.studentAnalytics.filter {
                        it.attendancePercentage < state.settings.attendanceThreshold
                    }
                    else -> state.studentAnalytics
                }
                buildString {
                    appendLine("RollNo,Name,Department,Semester,AvgScore,GPA,Attendance,Tier,RiskScore")
                    filtered.forEach { r ->
                        appendLine(
                            "${r.student.rollNumber},\"${r.student.fullName}\",\"${r.student.department.displayName}\",${r.student.currentSemester},${r.averagePercentage},${r.gpa},${r.attendancePercentage},${r.tier.label},${r.riskScore}"
                        )
                    }
                }
            }
        }
    }

    private fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
