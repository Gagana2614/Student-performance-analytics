package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.PerformanceTier
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterDropdown
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.TierBadge
import com.example.ui.viewmodel.AcademicUiState

private enum class ReportCategory(val key: String, val label: String) {
    COMPREHENSIVE("academic-summary", "Comprehensive Student Performance"),
    AT_RISK("at-risk", "At-Risk Intervention Roster"),
    ATTENDANCE_SHORTAGE("attendance-compliance", "Attendance Shortage List"),
    SUBJECT_SUMMARY("subject-summary", "Subject-Wise Outcome Summary")
}

@Composable
fun ReportsScreen(
    uiState: AcademicUiState,
    onBuildCsv: (String) -> String,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var selectedReport by remember { mutableStateOf(ReportCategory.COMPREHENSIVE) }

    val studentRows = remember(uiState.studentAnalytics, selectedReport, uiState.settings.attendanceThreshold) {
        when (selectedReport) {
            ReportCategory.AT_RISK -> uiState.studentAnalytics.filter { it.tier == PerformanceTier.AT_RISK }
            ReportCategory.ATTENDANCE_SHORTAGE -> uiState.studentAnalytics.filter {
                it.attendancePercentage < uiState.settings.attendanceThreshold
            }
            else -> uiState.studentAnalytics
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeaderBlock(
                title = "Academic Reports & Exports",
                subtitle = "${uiState.settings.institutionName} · AY ${uiState.settings.academicYear}"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropdown(
                    label = "Report",
                    selectedLabel = selectedReport.label,
                    options = ReportCategory.entries.map { it to it.label },
                    onSelect = { selectedReport = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val csv = onBuildCsv(selectedReport.key)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "EduMetrics Report — ${selectedReport.label}")
                            putExtra(Intent.EXTRA_TEXT, csv)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export EduMetrics CSV Report"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_csv_button")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export / Share CSV")
                }

                OutlinedButton(
                    onClick = {
                        val csv = onBuildCsv(selectedReport.key)
                        clipboard.setText(AnnotatedString(csv))
                        onShowMessage("CSV report copied to clipboard.")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy CSV")
                }
            }
        }

        if (selectedReport == ReportCategory.SUBJECT_SUMMARY) {
            if (uiState.subjectAnalytics.isEmpty()) {
                item {
                    EmptyStateView(title = "No subjects to report", description = "Add subjects to populate this report.")
                }
            } else {
                items(uiState.subjectAnalytics, key = { it.subject.id }) { sa ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${sa.subject.code} · ${sa.subject.name}", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${sa.subject.department.displayName} · Sem ${sa.subject.semester} · ${sa.enrolledCount} evaluated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Avg ${sa.averagePercentage}%", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Pass ${sa.passRate}% · Att ${sa.averageAttendance}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            if (studentRows.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No matching student records",
                        description = "No students fall under the selected report filter."
                    )
                }
            } else {
                items(studentRows, key = { it.student.id }) { r ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(r.student.fullName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${r.student.rollNumber} · ${r.student.department.shortName} · Sem ${r.student.currentSemester}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TierBadge(tier = r.tier)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${r.averagePercentage}% (GPA ${r.gpa})", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                AttendanceBadge(
                                    percentage = r.attendancePercentage,
                                    threshold = uiState.settings.attendanceThreshold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
