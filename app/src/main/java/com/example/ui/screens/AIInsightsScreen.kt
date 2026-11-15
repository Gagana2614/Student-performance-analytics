package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AnalyticsEngine
import com.example.ui.components.EduBadge
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.SectionCard
import com.example.ui.components.StatKpiCard
import com.example.ui.components.TierBadge
import com.example.ui.navigation.Routes
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal600
import com.example.ui.theme.Violet500
import com.example.ui.theme.Violet600
import com.example.ui.viewmodel.AcademicUiState

@Composable
fun AIInsightsScreen(
    uiState: AcademicUiState,
    onNavigate: (String) -> Unit
) {
    val diagnostics = remember(uiState.studentAnalytics, uiState.subjectAnalytics, uiState.settings) {
        val evaluated = uiState.studentAnalytics.filter { it.totalSubjectsEvaluated > 0 }
        val r = AnalyticsEngine.computePearsonCorrelation(
            evaluated.map { it.attendancePercentage to it.averagePercentage }
        )

        val highAtt = evaluated.filter { it.attendancePercentage >= uiState.settings.attendanceThreshold }
        val lowAtt = evaluated.filter { it.attendancePercentage < uiState.settings.attendanceThreshold }

        val highAvg = if (highAtt.isNotEmpty()) AnalyticsEngine.round1(highAtt.map { it.averagePercentage }.average()) else 0.0
        val lowAvg = if (lowAtt.isNotEmpty()) AnalyticsEngine.round1(lowAtt.map { it.averagePercentage }.average()) else 0.0
        val gap = AnalyticsEngine.round1(highAvg - lowAvg)

        val priorityQueue = uiState.studentAnalytics
            .filter { it.riskScore > 0 }
            .sortedByDescending { it.riskScore }

        val bottleneckSubject = uiState.subjectAnalytics
            .filter { it.enrolledCount > 0 }
            .minByOrNull { it.averagePercentage }

        Triple(
            Triple(r, gap, highAvg to lowAvg),
            priorityQueue,
            bottleneckSubject
        )
    }

    val (corrTriple, priorityQueue, bottleneckSubject) = diagnostics
    val (pearsonR, scoreGap, highLowPair) = corrTriple

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeaderBlock(
                title = "Academic Intelligence & Warnings",
                subtitle = "Deterministic statistical diagnostics and early-warning risk index computed from live records.",
                action = {
                    OutlinedButton(onClick = { onNavigate(Routes.REPORTS) }) {
                        Text("Export Report")
                    }
                }
            )
        }

        // Transparent methodology card (Zero fake AI claims)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Transparent Statistical Engine: Every alert and risk score below is computed deterministically from your live Room records using Pearson correlation (r = $pearsonR) and configured thresholds (Pass ≥ ${uiState.settings.passPercentage}%, Attendance ≥ ${uiState.settings.attendanceThreshold}%).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Diagnostic summary cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Attendance Impact",
                    value = if (scoreGap >= 0) "+$scoreGap%" else "$scoreGap%",
                    badgeText = "r = $pearsonR",
                    subtitle = "${highLowPair.first}% vs ${highLowPair.second}%",
                    icon = Icons.Filled.TrendingUp,
                    gradientColors = listOf(Teal600, Emerald500),
                    modifier = Modifier.weight(1f)
                )
                StatKpiCard(
                    title = "Students Flagged",
                    value = priorityQueue.size.toString(),
                    badgeText = "${uiState.students.size} total",
                    subtitle = "Risk > 0",
                    icon = Icons.Filled.Shield,
                    gradientColors = listOf(Rose600, Amber500),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (bottleneckSubject != null) {
            item {
                SectionCard(
                    title = "Lowest Scoring Subject",
                    subtitle = "Course-level difficulty & pass rate diagnostic"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Book, contentDescription = null, tint = Violet600)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${bottleneckSubject.subject.code} · ${bottleneckSubject.subject.name}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Faculty: ${bottleneckSubject.subject.facultyName} · ${bottleneckSubject.enrolledCount} evaluated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${bottleneckSubject.averagePercentage}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Violet500
                            )
                            Text(
                                text = "Pass ${bottleneckSubject.passRate}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionCard(
                title = "Early Warning Intervention Queue (${priorityQueue.size})",
                subtitle = "Ranked by deterministic risk index (0–100) from failing grades & attendance shortfalls"
            ) {
                if (priorityQueue.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald100.copy(alpha = 0.6f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald600)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "No academic risk warnings detected under current threshold settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Emerald600
                        )
                    }
                } else {
                    Text(
                        text = "Scroll below to review each flagged student's specific risk triggers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(priorityQueue, key = { it.student.id }) { item ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.student.fullName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${item.student.rollNumber} · ${item.student.department.displayName} · Sem ${item.student.currentSemester}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        EduBadge(
                            text = "Risk ${item.riskScore}/100",
                            containerColor = if (item.riskScore >= 60) Rose100 else Amber100,
                            contentColor = if (item.riskScore >= 60) Rose600 else Amber600
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TierBadge(tier = item.tier)
                    Spacer(modifier = Modifier.height(8.dp))

                    item.riskReasons.forEach { reason ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = Amber500,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Avg: ${item.averagePercentage}% · Att: ${item.attendancePercentage}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Rose500
                        )
                        Button(
                            onClick = { onNavigate(Routes.STUDENTS) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Review", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}
