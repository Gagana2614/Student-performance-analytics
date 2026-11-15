package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.AnalyticsEngine
import com.example.domain.model.Department
import com.example.domain.model.PerformanceTier
import com.example.ui.components.AttendanceMarksScatterChart
import com.example.ui.components.DualTrendAreaChart
import com.example.ui.components.EduBadge
import com.example.ui.components.FilterDropdown
import com.example.ui.components.GroupedBarDatum
import com.example.ui.components.GroupedBenchmarkBarChart
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.ScatterDatum
import com.example.ui.components.SectionCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Teal500
import com.example.ui.theme.Violet100
import com.example.ui.theme.Violet500
import com.example.ui.theme.Violet600
import com.example.ui.viewmodel.AcademicUiState

@Composable
fun AnalyticsScreen(uiState: AcademicUiState) {
    var deptFilter by remember { mutableStateOf<Department?>(null) }

    val evaluated = remember(uiState.studentAnalytics, deptFilter) {
        uiState.studentAnalytics.filter {
            (deptFilter == null || it.student.department == deptFilter) && it.totalSubjectsEvaluated > 0
        }
    }

    val scatterPoints = remember(evaluated) {
        evaluated.map { s ->
            val color = when (s.tier) {
                PerformanceTier.EXCELLENT -> Emerald500
                PerformanceTier.GOOD -> Blue500
                PerformanceTier.AVERAGE -> Amber500
                PerformanceTier.AT_RISK -> Rose500
            }
            ScatterDatum(
                label = s.student.fullName,
                xPercent = s.attendancePercentage.toFloat(),
                yPercent = s.averagePercentage.toFloat(),
                color = color
            )
        }
    }

    val pearsonR = remember(evaluated) {
        AnalyticsEngine.computePearsonCorrelation(
            evaluated.map { it.attendancePercentage to it.averagePercentage }
        )
    }

    val deptBenchmarks = remember(uiState.departmentBenchmarks) {
        uiState.departmentBenchmarks.map { b ->
            GroupedBarDatum(
                label = "${b.department.displayName} (${b.studentCount})",
                primaryValue = b.avgScore.toFloat(),
                secondaryValue = b.avgAttendance.toFloat()
            )
        }
    }

    val subjectBenchmarks = remember(uiState.subjectAnalytics) {
        uiState.subjectAnalytics.map { sa ->
            GroupedBarDatum(
                label = "${sa.subject.code} — ${sa.subject.name}",
                primaryValue = sa.averagePercentage.toFloat(),
                secondaryValue = sa.passRate.toFloat()
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeaderBlock(
                title = "Deep Academic Analytics",
                subtitle = "Live statistical benchmarks calculated from active records.",
                action = {
                    FilterDropdown(
                        label = "Dept",
                        selectedLabel = deptFilter?.shortName ?: "All",
                        options = listOf(null to "All Departments") + Department.entries.map { it to it.displayName },
                        onSelect = { deptFilter = it }
                    )
                }
            )
        }

        item {
            SectionCard(
                title = "Attendance vs. Mean Score Correlation",
                subtitle = "Dashed guides mark ${uiState.settings.attendanceThreshold}% attendance and ${uiState.settings.passPercentage}% pass thresholds",
                action = {
                    EduBadge(text = "Pearson r = $pearsonR", containerColor = Violet100, contentColor = Violet600)
                }
            ) {
                AttendanceMarksScatterChart(
                    points = scatterPoints,
                    attendanceThreshold = uiState.settings.attendanceThreshold,
                    passThreshold = uiState.settings.passPercentage
                )
            }
        }

        item {
            SectionCard(
                title = "Department Benchmark",
                subtitle = "Average academic score vs. attendance rate per department"
            ) {
                GroupedBenchmarkBarChart(
                    data = deptBenchmarks,
                    primaryLabel = "Mean Score %",
                    secondaryLabel = "Mean Attendance %",
                    primaryColor = Blue500,
                    secondaryColor = Teal500
                )
            }
        }

        item {
            SectionCard(
                title = "Subject Mastery & Pass Rates",
                subtitle = "Course average percentages compared against pass rates"
            ) {
                GroupedBenchmarkBarChart(
                    data = subjectBenchmarks,
                    primaryLabel = "Class Average %",
                    secondaryLabel = "Pass Rate %",
                    primaryColor = Violet500,
                    secondaryColor = Emerald500
                )
            }
        }

        item {
            SectionCard(
                title = "Multi-Semester Pass Rate & Score Progression",
                subtitle = "Longitudinal academic performance across semesters"
            ) {
                DualTrendAreaChart(
                    labels = uiState.semesterTrends.map { it.label },
                    seriesA = uiState.semesterTrends.map { it.passRate.toFloat() },
                    seriesB = uiState.semesterTrends.map { it.avgMarks.toFloat() },
                    seriesALabel = "Pass Rate %",
                    seriesBLabel = "Mean Marks %",
                    seriesAColor = Emerald500,
                    seriesBColor = Violet500
                )
            }
        }
    }
}
