package com.example.data.local

import com.example.domain.model.AttendanceRecord
import com.example.domain.model.Department
import com.example.domain.model.MarkRecord
import com.example.domain.model.Student
import com.example.domain.model.StudentStatus
import com.example.domain.model.Subject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object SeedData {

    data class SeedBundle(
        val students: List<Student>,
        val subjects: List<Subject>,
        val marks: List<MarkRecord>,
        val attendance: List<AttendanceRecord>
    )

    private data class Profile(val intBase: Int, val extBase: Int, val attRatio: Double)

    private val profiles = mapOf(
        "stu-1" to Profile(36, 54, 0.95),
        "stu-2" to Profile(30, 44, 0.86),
        "stu-3" to Profile(14, 21, 0.58),
        "stu-4" to Profile(27, 40, 0.81),
        "stu-5" to Profile(23, 35, 0.76),
        "stu-6" to Profile(35, 51, 0.92),
        "stu-7" to Profile(16, 24, 0.62),

        "stu-8" to Profile(37, 55, 0.96),
        "stu-9" to Profile(29, 43, 0.84),
        "stu-10" to Profile(25, 38, 0.79),
        "stu-11" to Profile(15, 22, 0.55),
        "stu-12" to Profile(34, 50, 0.91),
        "stu-13" to Profile(26, 41, 0.82),
        "stu-14" to Profile(17, 25, 0.66),
        "stu-15" to Profile(24, 37, 0.77),

        "stu-16" to Profile(38, 56, 0.97),
        "stu-17" to Profile(33, 49, 0.89),
        "stu-18" to Profile(28, 42, 0.83),
        "stu-19" to Profile(13, 20, 0.52),
        "stu-20" to Profile(25, 39, 0.78),
        "stu-21" to Profile(31, 46, 0.88),
        "stu-22" to Profile(18, 26, 0.68)
    )

    fun createSeedBundle(): SeedBundle {
        val students = listOf(
            Student("stu-1", "CS2601", "Aarav Sharma", "aarav.sharma@edumetrics.edu", Department.COMPUTER_SCIENCE, 2, 2025, StudentStatus.ACTIVE, true, "2025-08-12"),
            Student("stu-2", "CS2602", "Diya Nair", "diya.nair@edumetrics.edu", Department.COMPUTER_SCIENCE, 2, 2025, StudentStatus.ACTIVE, true, "2025-08-12"),
            Student("stu-3", "DS2603", "Rohan Verma", "rohan.verma@edumetrics.edu", Department.DATA_SCIENCE, 2, 2025, StudentStatus.PROBATION, true, "2025-08-13"),
            Student("stu-4", "IT2604", "Meera Krishnan", "meera.k@edumetrics.edu", Department.INFORMATION_TECHNOLOGY, 2, 2025, StudentStatus.ACTIVE, true, "2025-08-14"),
            Student("stu-5", "EC2605", "Kabir Malhotra", "kabir.m@edumetrics.edu", Department.ELECTRONICS, 2, 2025, StudentStatus.ACTIVE, true, "2025-08-14"),
            Student("stu-6", "CS2606", "Ananya Deshmukh", "ananya.d@edumetrics.edu", Department.COMPUTER_SCIENCE, 2, 2025, StudentStatus.ACTIVE, true, "2025-08-15"),
            Student("stu-7", "DS2607", "Vikram Joshi", "vikram.j@edumetrics.edu", Department.DATA_SCIENCE, 2, 2025, StudentStatus.PROBATION, true, "2025-08-16"),

            Student("stu-8", "CS2501", "Priya Menon", "priya.menon@edumetrics.edu", Department.COMPUTER_SCIENCE, 4, 2024, StudentStatus.ACTIVE, true, "2024-08-10"),
            Student("stu-9", "DS2502", "Siddharth Rao", "siddharth.rao@edumetrics.edu", Department.DATA_SCIENCE, 4, 2024, StudentStatus.ACTIVE, true, "2024-08-10"),
            Student("stu-10", "IT2503", "Ishita Banerjee", "ishita.b@edumetrics.edu", Department.INFORMATION_TECHNOLOGY, 4, 2024, StudentStatus.ACTIVE, true, "2024-08-11"),
            Student("stu-11", "EC2504", "Arjun Kulkarni", "arjun.k@edumetrics.edu", Department.ELECTRONICS, 4, 2024, StudentStatus.PROBATION, true, "2024-08-11"),
            Student("stu-12", "CS2505", "Neha Kapoor", "neha.kapoor@edumetrics.edu", Department.COMPUTER_SCIENCE, 4, 2024, StudentStatus.ACTIVE, true, "2024-08-12"),
            Student("stu-13", "DS2506", "Aditya Choudhary", "aditya.c@edumetrics.edu", Department.DATA_SCIENCE, 4, 2024, StudentStatus.ACTIVE, true, "2024-08-12"),
            Student("stu-14", "IT2507", "Tanvi Saxena", "tanvi.s@edumetrics.edu", Department.INFORMATION_TECHNOLOGY, 4, 2024, StudentStatus.PROBATION, true, "2024-08-13"),
            Student("stu-15", "EC2508", "Devansh Patel", "devansh.p@edumetrics.edu", Department.ELECTRONICS, 4, 2024, StudentStatus.ACTIVE, true, "2024-08-14"),

            Student("stu-16", "CS2401", "Sanya Mirza", "sanya.m@edumetrics.edu", Department.COMPUTER_SCIENCE, 6, 2023, StudentStatus.ACTIVE, true, "2023-08-08"),
            Student("stu-17", "DS2402", "Karanvir Gill", "karanvir.g@edumetrics.edu", Department.DATA_SCIENCE, 6, 2023, StudentStatus.ACTIVE, true, "2023-08-08"),
            Student("stu-18", "IT2403", "Riya Chatterjee", "riya.c@edumetrics.edu", Department.INFORMATION_TECHNOLOGY, 6, 2023, StudentStatus.ACTIVE, true, "2023-08-09"),
            Student("stu-19", "CS2404", "Nikhil Bhatia", "nikhil.b@edumetrics.edu", Department.COMPUTER_SCIENCE, 6, 2023, StudentStatus.PROBATION, true, "2023-08-09"),
            Student("stu-20", "EC2405", "Shruti Hegde", "shruti.h@edumetrics.edu", Department.ELECTRONICS, 6, 2023, StudentStatus.ACTIVE, true, "2023-08-10"),
            Student("stu-21", "DS2406", "Varun Iyengar", "varun.i@edumetrics.edu", Department.DATA_SCIENCE, 6, 2023, StudentStatus.ACTIVE, true, "2023-08-10"),
            Student("stu-22", "IT2407", "Pooja Deshpande", "pooja.d@edumetrics.edu", Department.INFORMATION_TECHNOLOGY, 6, 2023, StudentStatus.PROBATION, true, "2023-08-11")
        )

        val subjects = listOf(
            Subject("sub-1", "CS201", "Data Structures & Algorithms", Department.COMPUTER_SCIENCE, 2, 4, "Dr. Kavita Iyer", 40, 60, true),
            Subject("sub-2", "MA202", "Discrete Mathematics & Logic", Department.DATA_SCIENCE, 2, 4, "Prof. Rajeshwari Sen", 40, 60, true),
            Subject("sub-3", "CS401", "Database Management Systems", Department.COMPUTER_SCIENCE, 4, 4, "Dr. Arvind Subramanian", 40, 60, true),
            Subject("sub-4", "AI402", "Machine Learning Foundations", Department.DATA_SCIENCE, 4, 4, "Dr. Sneha Kulkarni", 40, 60, true),
            Subject("sub-5", "CS601", "Distributed Cloud Systems", Department.INFORMATION_TECHNOLOGY, 6, 4, "Prof. Vikramaditya Bose", 40, 60, true),
            Subject("sub-6", "EC602", "Embedded & IoT Architecture", Department.ELECTRONICS, 6, 3, "Dr. Harishankar Prasad", 40, 60, true)
        )

        val marks = mutableListOf<MarkRecord>()
        val attendance = mutableListOf<AttendanceRecord>()
        var markIdx = 1
        var attIdx = 1

        students.forEachIndexed { sIndex, student ->
            val profile = profiles[student.id] ?: Profile(26, 38, 0.80)
            val relevantSubjects = subjects.filter { it.semester <= student.currentSemester }

            relevantSubjects.forEachIndexed { subIndex, sub ->
                val variance = ((sIndex + subIndex) % 5) - 2
                val semBoost = if (sub.semester < student.currentSemester) 2 else 0

                val internalMarks = max(4, min(sub.maxInternal, profile.intBase + variance + if (semBoost > 0) 1 else 0))
                val externalMarks = max(8, min(sub.maxExternal, profile.extBase + variance * 2 + semBoost))

                marks.add(
                    MarkRecord(
                        id = "mrk-${markIdx++}",
                        studentId = student.id,
                        subjectId = sub.id,
                        semester = sub.semester,
                        internalMarks = internalMarks,
                        externalMarks = externalMarks,
                        recordedAt = "2026-10-${(10 + (subIndex % 15)).toString().padStart(2, '0')}",
                        isDemo = true
                    )
                )

                val totalClasses = 40 + (subIndex % 3) * 4
                val attended = max(
                    10,
                    min(totalClasses, (totalClasses * profile.attRatio).roundToInt() + if (subIndex % 2 == 0) 1 else -1)
                )

                attendance.add(
                    AttendanceRecord(
                        id = "att-${attIdx++}",
                        studentId = student.id,
                        subjectId = sub.id,
                        semester = sub.semester,
                        classesAttended = attended,
                        totalClasses = totalClasses,
                        lastUpdated = "2026-11-10",
                        isDemo = true
                    )
                )
            }
        }

        return SeedBundle(students, subjects, marks, attendance)
    }
}
