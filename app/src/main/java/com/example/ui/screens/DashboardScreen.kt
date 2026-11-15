package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.AnalyticsEngine
import com.example.domain.model.PerformanceTier
import com.example.ui.components.BarDatum
import com.example.ui.components.DemoBadge
import com.example.ui.components.DualTrendAreaChart
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.SectionCard
import com.example.ui.components.StatKpiCard
import com.example.ui.components.TierBadge
import com.example.ui.components.VerticalBarChart
import com.example.ui.navigation.Routes
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Blue600
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy950
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Violet500
import com.example.ui.theme.Violet600
import com.example.ui.viewmodel.AcademicUiState

@Composable
fun DashboardScreen(
    uiState: AcademicUiState,
    onNavigate: (String) -> Unit
) {
    val totalStudents = uiState.students.size
    val demoCount = uiState.students.count { it.isDemo }
    val customCount = totalStudents - demoCount

    val evaluated = remember(uiState.studentAnalytics) {
        uiState.studentAnalytics.filter { it.totalSubjectsEvaluated > 0 }
    }
    val meanScore = remember(evaluated) {
        if (evaluated.isNotEmpty()) AnalyticsEngine.round1(evaluated.map { it.averagePercentage }.average()) else 0.0
    }
    val attList = remember(uiState.studentAnalytics) {
        uiState.studentAnalytics.filter { it.attendancePercentage > 0.0 }
    }
    val meanAttendance = remember(attList) {
        if (attList.isNotEmpty()) AnalyticsEngine.round1(attList.map { it.attendancePercentage }.average()) else 0.0
    }
    val atRiskList = remember(uiState.studentAnalytics) {
        uiState.studentAnalytics.filter { it.tier == PerformanceTier.AT_RISK }.sortedByDescending { it.riskScore }
    }
    val topPerformers = remember(evaluated) {
        evaluated.sortedByDescending { it.averagePercentage }.take(5)
    }

    val tierBars = remember(uiState.studentAnalytics) {
        val counts = uiState.studentAnalytics.groupingBy { it.tier }.eachCount()
        listOf(
            BarDatum("Excellent", (counts[PerformanceTier.EXCELLENT] ?: 0).toFloat(), Emerald500, "${counts[PerformanceTier.EXCELLENT] ?: 0}"),
            BarDatum("Good", (counts[PerformanceTier.GOOD] ?: 0).toFloat(), Blue500, "${counts[PerformanceTier.GOOD] ?: 0}"),
            BarDatum("Average", (counts[PerformanceTier.AVERAGE] ?: 0).toFloat(), Amber500, "${counts[PerformanceTier.AVERAGE] ?: 0}"),
            BarDatum("At Risk", (counts[PerformanceTier.AT_RISK] ?: 0).toFloat(), Rose500, "${counts[PerformanceTier.AT_RISK] ?: 0}")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_academic_banner_1794724416276),
                        contentDescription = "EduMetrics academic performance banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Navy950.copy(alpha = 0.88f), Navy950.copy(alpha = 0.45f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Violet600.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "AY ${uiState.settings.academicYear} · LIVE ANALYTICS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFDDD6FE),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Academic Performance Overview",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.subjects.size} subjects · ${uiState.marks.size} mark entries · ${uiState.attendance.size} attendance logs",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onNavigate(Routes.STUDENTS) },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("dashboard_manage_students")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Students", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { onNavigate(Routes.AI_INSIGHTS) },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("dashboard_ai_insights")
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Early Warnings (${atRiskList.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            PageHeaderBlock(
                title = "Key Indicators",
                subtitle = "Calculated deterministically from current student, mark, and attendance records."
            )
        }

        // KPI Grid (2x2 on mobile/tablet)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatKpiCard(
                        title = "Enrolled Students",
                        value = totalStudents.toString(),
                        badgeText = "$customCount custom",
                        subtitle = "$demoCount demo",
                        icon = Icons.Filled.Group,
                        gradientColors = listOf(Violet600, Blue600),
                        modifier = Modifier.weight(1f)
                    )
                    StatKpiCard(
                        title = "Mean Score",
                        value = "$meanScore%",
                        badgeText = "Pass ≥${uiState.settings.passPercentage}%",
                        subtitle = "Evaluated",
                        icon = Icons.Filled.TrendingUp,
                        gradientColors = listOf(Blue600, Teal500),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatKpiCard(
                        title = "Mean Attendance",
                        value = "$meanAttendance%",
                        badgeText = "Min ${uiState.settings.attendanceThreshold}%",
                        subtitle = "Policy",
                        icon = Icons.Filled.EventAvailable,
                        gradientColors = listOf(Teal600, Emerald500),
                        modifier = Modifier.weight(1f)
                    )
                    StatKpiCard(
                        title = "At-Risk Watchlist",
                        value = atRiskList.size.toString(),
                        badgeText = if (totalStudents > 0) "${(atRiskList.size * 100) / totalStudents}%" else "0%",
                        subtitle = "Flagged",
                        icon = Icons.Filled.WarningAmber,
                        gradientColors = listOf(Rose600, Amber500),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Semester Trend Chart
        item {
            SectionCard(
                title = "Semester Performance & Attendance Trend",
                subtitle = "Progression across active academic semesters",
                action = {
                    TextButton(onClick = { onNavigate(Routes.ANALYTICS) }) {
                        Text("Deep Analytics")
                    }
                }
            ) {
                DualTrendAreaChart(
                    labels = uiState.semesterTrends.map { it.label },
                    seriesA = uiState.semesterTrends.map { it.avgMarks.toFloat() },
                    seriesB = uiState.semesterTrends.map { it.avgAttendance.toFloat() },
                    seriesALabel = "Mean Marks %",
                    seriesBLabel = "Mean Attendance %",
                    seriesAColor = Violet500,
                    seriesBColor = Teal500
                )
            }
        }

        // Performance Tiers Chart
        item {
            SectionCard(
                title = "Performance Tier Distribution",
                subtitle = "Student standing breakdown across cohorts"
            ) {
                VerticalBarChart(data = tierBars)
            }
        }

        // Top Academic Performers
        item {
            SectionCard(
                title = "Top Academic Performers",
                subtitle = "Highest cumulative percentage averages",
                action = {
                    TextButton(onClick = { onNavigate(Routes.STUDENTS) }) {
                        Text("View All")
                    }
                }
            ) {
                if (topPerformers.isEmpty()) {
                    EmptyStateView(title = "No marks recorded", description = "Add mark entries to rank top students.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        topPerformers.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .clickable { onNavigate(Routes.STUDENTS) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Brush.linearGradient(listOf(Emerald600, Teal500))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${idx + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.student.fullName,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            DemoBadge(isDemo = item.student.isDemo)
                                        }
                                        Text(
                                            text = "${item.student.rollNumber} · ${item.student.department.shortName} · Sem ${item.student.currentSemester}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${item.averagePercentage}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald600
                                    )
                                    Text(
                                        text = "GPA ${item.gpa}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // At-Risk Priority Watchlist
        item {
            SectionCard(
                title = "At-Risk Priority Watchlist",
                subtitle = "Students requiring faculty intervention",
                action = {
                    TextButton(onClick = { onNavigate(Routes.AI_INSIGHTS) }) {
                        Text("Open Insights")
                    }
                }
            ) {
                if (atRiskList.isEmpty()) {
                    EmptyStateView(
                        title = "Zero students at risk",
                        description = "All evaluated students meet passing and attendance policies."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        atRiskList.take(5).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
                                    .clickable { onNavigate(Routes.AI_INSIGHTS) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.student.fullName,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        TierBadge(tier = item.tier)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.riskReasons.firstOrNull() ?: "Score ${item.averagePercentage}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${item.averagePercentage}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Rose600
                                    )
                                    Text(
                                        text = "Att ${item.attendancePercentage}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subject Snapshot
        item {
            SectionCard(
                title = "Subject Performance Snapshot",
                subtitle = "Mean score, pass rate, and attendance by course",
                action = {
                    TextButton(onClick = { onNavigate(Routes.SUBJECTS) }) {
                        Text("Subjects")
                    }
                }
            ) {
                if (uiState.subjectAnalytics.isEmpty()) {
                    EmptyStateView(title = "No subjects configured", description = "Add subjects in the curriculum catalog.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.subjectAnalytics.forEach { sa ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${sa.subject.code} · ${sa.subject.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Sem ${sa.subject.semester} · ${sa.subject.facultyName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Avg ${sa.averagePercentage}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pass ${sa.passRate}% · Att ${sa.averageAttendance}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Teal600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
