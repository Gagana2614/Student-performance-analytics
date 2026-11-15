package com.example.domain.model

enum class Department(val displayName: String, val shortName: String) {
    COMPUTER_SCIENCE("Computer Science", "CS"),
    DATA_SCIENCE("Data Science", "DS"),
    INFORMATION_TECHNOLOGY("Information Technology", "IT"),
    ELECTRONICS("Electronics & Comm.", "EC")
}

enum class StudentStatus(val label: String) {
    ACTIVE("Active"),
    PROBATION("Probation"),
    INACTIVE("Inactive")
}

enum class PerformanceTier(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    AVERAGE("Average"),
    AT_RISK("At Risk")
}

enum class UserRole(val label: String) {
    ACADEMIC_ADMIN("Academic Admin"),
    SENIOR_LECTURER("Senior Lecturer"),
    DEPARTMENT_HEAD("Department Head")
}

data class Student(
    val id: String,
    val rollNumber: String,
    val fullName: String,
    val email: String,
    val department: Department,
    val currentSemester: Int,
    val enrollmentYear: Int,
    val status: StudentStatus,
    val isDemo: Boolean = false,
    val createdAt: String
)

data class Subject(
    val id: String,
    val code: String,
    val name: String,
    val department: Department,
    val semester: Int,
    val credits: Int,
    val facultyName: String,
    val maxInternal: Int,
    val maxExternal: Int,
    val isDemo: Boolean = false
)

data class MarkRecord(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val semester: Int,
    val internalMarks: Int,
    val externalMarks: Int,
    val recordedAt: String,
    val isDemo: Boolean = false
)

data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val semester: Int,
    val classesAttended: Int,
    val totalClasses: Int,
    val lastUpdated: String,
    val isDemo: Boolean = false
)

data class AcademicSettings(
    val institutionName: String = "EduMetrics Institute of Technology",
    val academicYear: String = "2026–2027",
    val activeUserName: String = "Dr. Elena Rostova",
    val activeUserRole: UserRole = UserRole.ACADEMIC_ADMIN,
    val passPercentage: Int = 40,
    val attendanceThreshold: Int = 75,
    val excellenceThreshold: Int = 85,
    val darkTheme: Boolean = false
)

data class StudentAnalytics(
    val student: Student,
    val averagePercentage: Double,
    val gpa: Double,
    val attendancePercentage: Double,
    val totalSubjectsEvaluated: Int,
    val failedSubjectsCount: Int,
    val tier: PerformanceTier,
    val riskScore: Int,
    val riskReasons: List<String>
)

data class SubjectAnalytics(
    val subject: Subject,
    val enrolledCount: Int,
    val averagePercentage: Double,
    val highestScore: Double,
    val lowestScore: Double,
    val passRate: Double,
    val averageAttendance: Double
)

data class SemesterTrend(
    val semesterNum: Int,
    val label: String,
    val avgMarks: Double,
    val avgAttendance: Double,
    val passRate: Double,
    val recordsCount: Int
)

data class DepartmentBenchmark(
    val department: Department,
    val studentCount: Int,
    val avgScore: Double,
    val avgAttendance: Double
)

enum class NotificationSeverity { DANGER, WARNING, INFO }

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val severity: NotificationSeverity,
    val targetRoute: String,
    val isRead: Boolean
)
