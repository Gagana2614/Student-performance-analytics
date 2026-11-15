package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.AttendanceRecord
import com.example.domain.model.Department
import com.example.domain.model.MarkRecord
import com.example.domain.model.Student
import com.example.domain.model.StudentStatus
import com.example.domain.model.Subject

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val rollNumber: String,
    val fullName: String,
    val email: String,
    val department: String,
    val currentSemester: Int,
    val enrollmentYear: Int,
    val status: String,
    val isDemo: Boolean,
    val createdAt: String
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val department: String,
    val semester: Int,
    val credits: Int,
    val facultyName: String,
    val maxInternal: Int,
    val maxExternal: Int,
    val isDemo: Boolean
)

@Entity(
    tableName = "marks",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("subjectId")]
)
data class MarkEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val subjectId: String,
    val semester: Int,
    val internalMarks: Int,
    val externalMarks: Int,
    val recordedAt: String,
    val isDemo: Boolean
)

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("subjectId")]
)
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val subjectId: String,
    val semester: Int,
    val classesAttended: Int,
    val totalClasses: Int,
    val lastUpdated: String,
    val isDemo: Boolean
)

fun StudentEntity.toDomain(): Student = Student(
    id = id,
    rollNumber = rollNumber,
    fullName = fullName,
    email = email,
    department = Department.entries.find { it.name == department } ?: Department.COMPUTER_SCIENCE,
    currentSemester = currentSemester,
    enrollmentYear = enrollmentYear,
    status = StudentStatus.entries.find { it.name == status } ?: StudentStatus.ACTIVE,
    isDemo = isDemo,
    createdAt = createdAt
)

fun Student.toEntity(): StudentEntity = StudentEntity(
    id = id,
    rollNumber = rollNumber,
    fullName = fullName,
    email = email,
    department = department.name,
    currentSemester = currentSemester,
    enrollmentYear = enrollmentYear,
    status = status.name,
    isDemo = isDemo,
    createdAt = createdAt
)

fun SubjectEntity.toDomain(): Subject = Subject(
    id = id,
    code = code,
    name = name,
    department = Department.entries.find { it.name == department } ?: Department.COMPUTER_SCIENCE,
    semester = semester,
    credits = credits,
    facultyName = facultyName,
    maxInternal = maxInternal,
    maxExternal = maxExternal,
    isDemo = isDemo
)

fun Subject.toEntity(): SubjectEntity = SubjectEntity(
    id = id,
    code = code,
    name = name,
    department = department.name,
    semester = semester,
    credits = credits,
    facultyName = facultyName,
    maxInternal = maxInternal,
    maxExternal = maxExternal,
    isDemo = isDemo
)

fun MarkEntity.toDomain(): MarkRecord = MarkRecord(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    semester = semester,
    internalMarks = internalMarks,
    externalMarks = externalMarks,
    recordedAt = recordedAt,
    isDemo = isDemo
)

fun MarkRecord.toEntity(): MarkEntity = MarkEntity(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    semester = semester,
    internalMarks = internalMarks,
    externalMarks = externalMarks,
    recordedAt = recordedAt,
    isDemo = isDemo
)

fun AttendanceEntity.toDomain(): AttendanceRecord = AttendanceRecord(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    semester = semester,
    classesAttended = classesAttended,
    totalClasses = totalClasses,
    lastUpdated = lastUpdated,
    isDemo = isDemo
)

fun AttendanceRecord.toEntity(): AttendanceEntity = AttendanceEntity(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    semester = semester,
    classesAttended = classesAttended,
    totalClasses = totalClasses,
    lastUpdated = lastUpdated,
    isDemo = isDemo
)
