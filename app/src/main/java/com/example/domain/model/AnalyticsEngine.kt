package com.example.domain.model

import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object AnalyticsEngine {

    fun round1(value: Double): Double = (value * 10.0).roundToInt() / 10.0
    fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0

    fun computeMarkPercentage(
        internalMarks: Int,
        externalMarks: Int,
        maxInternal: Int = 40,
        maxExternal: Int = 60
    ): Double {
        val maxTotal = maxInternal + maxExternal
        if (maxTotal <= 0) return 0.0
        return round1(((internalMarks + externalMarks).toDouble() / maxTotal.toDouble()) * 100.0)
    }

    fun getLetterGrade(percentage: Double): Pair<String, Int> = when {
        percentage >= 90.0 -> "A+" to 10
        percentage >= 80.0 -> "A" to 9
        percentage >= 70.0 -> "B+" to 8
        percentage >= 60.0 -> "B" to 7
        percentage >= 50.0 -> "C" to 6
        percentage >= 40.0 -> "D" to 5
        else -> "F" to 0
    }

    fun computeStudentAnalytics(
        students: List<Student>,
        subjects: List<Subject>,
        marks: List<MarkRecord>,
        attendance: List<AttendanceRecord>,
        settings: AcademicSettings
    ): List<StudentAnalytics> {
        val subjectMap = subjects.associateBy { it.id }
        val marksByStudent = marks.groupBy { it.studentId }
        val attByStudent = attendance.groupBy { it.studentId }

        return students.map { student ->
            val stuMarks = marksByStudent[student.id].orEmpty()
            val stuAtt = attByStudent[student.id].orEmpty()

            var totalPct = 0.0
            var totalPoints = 0
            var totalCredits = 0
            var failedCount = 0

            stuMarks.forEach { m ->
                val sub = subjectMap[m.subjectId]
                val pct = computeMarkPercentage(
                    m.internalMarks,
                    m.externalMarks,
                    sub?.maxInternal ?: 40,
                    sub?.maxExternal ?: 60
                )
                totalPct += pct
                val credits = sub?.credits ?: 4
                val (_, points) = getLetterGrade(pct)
                totalPoints += points * credits
                totalCredits += credits
                if (pct < settings.passPercentage) failedCount++
            }

            val avgPercentage = if (stuMarks.isNotEmpty()) round1(totalPct / stuMarks.size) else 0.0
            val gpa = if (totalCredits > 0) round2(totalPoints.toDouble() / totalCredits.toDouble()) else 0.0

            val attendedSum = stuAtt.sumOf { it.classesAttended }
            val totalClassSum = stuAtt.sumOf { it.totalClasses }
            val attPercentage = if (totalClassSum > 0) {
                round1((attendedSum.toDouble() / totalClassSum.toDouble()) * 100.0)
            } else 0.0

            val tier = when {
                stuMarks.isEmpty() -> {
                    if (stuAtt.isNotEmpty() && attPercentage < settings.attendanceThreshold) {
                        PerformanceTier.AT_RISK
                    } else {
                        PerformanceTier.AVERAGE
                    }
                }
                avgPercentage < settings.passPercentage + 10 ||
                    failedCount > 0 ||
                    (stuAtt.isNotEmpty() && attPercentage < settings.attendanceThreshold - 8) -> {
                    PerformanceTier.AT_RISK
                }
                avgPercentage >= settings.excellenceThreshold -> PerformanceTier.EXCELLENT
                avgPercentage >= 68.0 -> PerformanceTier.GOOD
                else -> PerformanceTier.AVERAGE
            }

            val reasons = mutableListOf<String>()
            var riskScore = 0

            if (stuMarks.isNotEmpty() && avgPercentage < settings.passPercentage) {
                riskScore += 45
                reasons.add("Mean score ($avgPercentage%) is below pass threshold (${settings.passPercentage}%)")
            } else if (stuMarks.isNotEmpty() && avgPercentage < 55.0) {
                riskScore += 25
                reasons.add("Marginal mean score ($avgPercentage%) requires academic reinforcement")
            }

            if (failedCount > 0) {
                riskScore += min(30, failedCount * 15)
                reasons.add("Failed $failedCount subject assessment${if (failedCount > 1) "s" else ""}")
            }

            if (stuAtt.isNotEmpty() && attPercentage < settings.attendanceThreshold) {
                val gap = settings.attendanceThreshold - attPercentage
                riskScore += min(35, (15.0 + gap).roundToInt())
                reasons.add("Attendance ($attPercentage%) is below ${settings.attendanceThreshold}% policy")
            }

            StudentAnalytics(
                student = student,
                averagePercentage = avgPercentage,
                gpa = gpa,
                attendancePercentage = attPercentage,
                totalSubjectsEvaluated = stuMarks.size,
                failedSubjectsCount = failedCount,
                tier = tier,
                riskScore = min(100, riskScore),
                riskReasons = reasons
            )
        }
    }

    fun computeSubjectAnalytics(
        subjects: List<Subject>,
        marks: List<MarkRecord>,
        attendance: List<AttendanceRecord>,
        settings: AcademicSettings
    ): List<SubjectAnalytics> {
        val marksBySubject = marks.groupBy { it.subjectId }
        val attBySubject = attendance.groupBy { it.subjectId }

        return subjects.map { subject ->
            val subMarks = marksBySubject[subject.id].orEmpty()
            val subAtt = attBySubject[subject.id].orEmpty()

            val scores = subMarks.map { m ->
                computeMarkPercentage(m.internalMarks, m.externalMarks, subject.maxInternal, subject.maxExternal)
            }

            val avgPct = if (scores.isNotEmpty()) round1(scores.average()) else 0.0
            val highest = scores.maxOrNull() ?: 0.0
            val lowest = scores.minOrNull() ?: 0.0
            val passedCount = scores.count { it >= settings.passPercentage }
            val passRate = if (scores.isNotEmpty()) round1((passedCount.toDouble() / scores.size) * 100.0) else 0.0

            val attended = subAtt.sumOf { it.classesAttended }
            val total = subAtt.sumOf { it.totalClasses }
            val avgAtt = if (total > 0) round1((attended.toDouble() / total.toDouble()) * 100.0) else 0.0

            SubjectAnalytics(
                subject = subject,
                enrolledCount = maxOf(subMarks.size, subAtt.size),
                averagePercentage = avgPct,
                highestScore = highest,
                lowestScore = lowest,
                passRate = passRate,
                averageAttendance = avgAtt
            )
        }
    }

    fun computePearsonCorrelation(points: List<Pair<Double, Double>>): Double {
        if (points.size < 2) return 0.0
        val n = points.size.toDouble()
        val sumX = points.sumOf { it.first }
        val sumY = points.sumOf { it.second }
        val sumXY = points.sumOf { it.first * it.second }
        val sumX2 = points.sumOf { it.first * it.first }
        val sumY2 = points.sumOf { it.second * it.second }

        val numerator = n * sumXY - sumX * sumY
        val denominator = sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))
        if (denominator == 0.0) return 0.0
        return round2(numerator / denominator)
    }

    fun computeSemesterTrends(
        subjects: List<Subject>,
        marks: List<MarkRecord>,
        attendance: List<AttendanceRecord>,
        settings: AcademicSettings
    ): List<SemesterTrend> {
        val semesters = (marks.map { it.semester } + attendance.map { it.semester }).distinct().sorted()
        val subjectMap = subjects.associateBy { it.id }

        return semesters.map { sem ->
            val semMarks = marks.filter { it.semester == sem }
            val semAtt = attendance.filter { it.semester == sem }

            val scores = semMarks.map { m ->
                val sub = subjectMap[m.subjectId]
                computeMarkPercentage(
                    m.internalMarks,
                    m.externalMarks,
                    sub?.maxInternal ?: 40,
                    sub?.maxExternal ?: 60
                )
            }
            val avgMarks = if (scores.isNotEmpty()) round1(scores.average()) else 0.0
            val passRate = if (scores.isNotEmpty()) {
                round1((scores.count { it >= settings.passPercentage }.toDouble() / scores.size) * 100.0)
            } else 0.0

            val attSum = semAtt.sumOf { it.classesAttended }
            val totSum = semAtt.sumOf { it.totalClasses }
            val avgAtt = if (totSum > 0) round1((attSum.toDouble() / totSum.toDouble()) * 100.0) else 0.0

            SemesterTrend(
                semesterNum = sem,
                label = "Sem $sem",
                avgMarks = avgMarks,
                avgAttendance = avgAtt,
                passRate = passRate,
                recordsCount = semMarks.size
            )
        }
    }

    fun computeDepartmentBenchmarks(studentAnalytics: List<StudentAnalytics>): List<DepartmentBenchmark> {
        return Department.entries.mapNotNull { dept ->
            val group = studentAnalytics.filter { it.student.department == dept && it.totalSubjectsEvaluated > 0 }
            if (group.isEmpty()) null
            else DepartmentBenchmark(
                department = dept,
                studentCount = group.size,
                avgScore = round1(group.map { it.averagePercentage }.average()),
                avgAttendance = round1(group.map { it.attendancePercentage }.average())
            )
        }
    }
}
