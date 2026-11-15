package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AnalyticsEngine
import com.example.domain.model.AttendanceRecord
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DemoBadge
import com.example.ui.components.EduBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterDropdown
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.PaginationBar
import com.example.ui.components.SearchField
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.Violet100
import com.example.ui.theme.Violet600
import com.example.ui.viewmodel.AcademicUiState
import kotlin.math.ceil
import kotlin.math.max

private data class EnrichedAttendanceRow(
    val record: AttendanceRecord,
    val studentName: String,
    val rollNumber: String,
    val subjectCode: String,
    val subjectName: String,
    val percentage: Double
)

@Composable
fun AttendanceScreen(
    uiState: AcademicUiState,
    onSaveAttendance: (existingId: String?, studentId: String, subjectId: String, attended: Int, total: Int) -> Unit,
    onQuickIncrement: (AttendanceRecord, Boolean) -> Unit,
    onDeleteAttendance: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var onlyBelowThreshold by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }

    var showDialog by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<EnrichedAttendanceRow?>(null) }
    var deletingRow by remember { mutableStateOf<EnrichedAttendanceRow?>(null) }

    val enriched = remember(uiState.attendance, uiState.students, uiState.subjects, uiState.settings.attendanceThreshold, search, onlyBelowThreshold) {
        val stuMap = uiState.students.associateBy { it.id }
        val subMap = uiState.subjects.associateBy { it.id }
        val q = search.trim()

        uiState.attendance.map { rec ->
            val stu = stuMap[rec.studentId]
            val sub = subMap[rec.subjectId]
            val pct = if (rec.totalClasses > 0) {
                AnalyticsEngine.round1((rec.classesAttended.toDouble() / rec.totalClasses.toDouble()) * 100.0)
            } else 0.0
            EnrichedAttendanceRow(
                record = rec,
                studentName = stu?.fullName ?: "Unknown",
                rollNumber = stu?.rollNumber ?: "—",
                subjectCode = sub?.code ?: "—",
                subjectName = sub?.name ?: "Unknown",
                percentage = pct
            )
        }.filter { row ->
            val matchQuery = q.isBlank() ||
                row.studentName.contains(q, ignoreCase = true) ||
                row.rollNumber.contains(q, ignoreCase = true) ||
                row.subjectCode.contains(q, ignoreCase = true)
            val matchAlert = !onlyBelowThreshold || row.percentage < uiState.settings.attendanceThreshold
            matchQuery && matchAlert
        }.sortedBy { it.percentage }
    }

    val pageSize = 10
    val totalPages = max(1, ceil(enriched.size / pageSize.toDouble()).toInt())
    val safePage = currentPage.coerceIn(1, totalPages)
    val pageItems = remember(enriched, safePage) {
        enriched.drop((safePage - 1) * pageSize).take(pageSize)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeaderBlock(
                title = "Attendance Register (${enriched.size})",
                subtitle = "Monitor subject attendance against the ${uiState.settings.attendanceThreshold}% policy.",
                action = {
                    Button(
                        onClick = {
                            editingRow = null
                            showDialog = true
                        },
                        enabled = uiState.students.isNotEmpty() && uiState.subjects.isNotEmpty(),
                        modifier = Modifier.testTag("add_attendance_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log")
                    }
                }
            )
        }

        item {
            SearchField(
                value = search,
                onValueChange = {
                    search = it
                    currentPage = 1
                },
                placeholder = "Search student, roll no, or subject..."
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = onlyBelowThreshold,
                    onCheckedChange = {
                        onlyBelowThreshold = it
                        currentPage = 1
                    }
                )
                Text(
                    text = "Show only logs below ${uiState.settings.attendanceThreshold}% threshold",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (pageItems.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No matching attendance logs",
                    description = "All records meet the filter criteria or none have been added yet."
                )
            }
        } else {
            items(pageItems, key = { it.record.id }) { row ->
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = row.studentName,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DemoBadge(isDemo = row.record.isDemo)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    EduBadge(text = row.subjectCode, containerColor = Violet100, contentColor = Violet600)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${row.rollNumber} · ${row.record.classesAttended}/${row.record.totalClasses} classes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            AttendanceBadge(
                                percentage = row.percentage,
                                threshold = uiState.settings.attendanceThreshold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = { onQuickIncrement(row.record, true) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+1 P", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                                FilledTonalButton(
                                    onClick = { onQuickIncrement(row.record, false) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+1 A", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row {
                                IconButton(onClick = {
                                    editingRow = row
                                    showDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit attendance")
                                }
                                IconButton(onClick = { deletingRow = row }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete attendance",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            PaginationBar(
                currentPage = safePage,
                totalPages = totalPages,
                totalItems = enriched.size,
                onPrevious = { currentPage = (safePage - 1).coerceAtLeast(1) },
                onNext = { currentPage = (safePage + 1).coerceAtMost(totalPages) }
            )
        }
    }

    if (showDialog) {
        AttendanceFormDialog(
            uiState = uiState,
            existing = editingRow,
            onDismiss = { showDialog = false },
            onSave = { stuId, subId, attended, total ->
                onSaveAttendance(editingRow?.record?.id, stuId, subId, attended, total)
                showDialog = false
            }
        )
    }

    deletingRow?.let { row ->
        ConfirmDeleteDialog(
            title = "Delete Attendance Log",
            message = "Remove attendance log for ${row.studentName} in ${row.subjectCode}?",
            onConfirm = {
                onDeleteAttendance(row.record.id)
                deletingRow = null
            },
            onDismiss = { deletingRow = null }
        )
    }
}

@Composable
private fun AttendanceFormDialog(
    uiState: AcademicUiState,
    existing: EnrichedAttendanceRow?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int) -> Unit
) {
    var studentId by remember { mutableStateOf(existing?.record?.studentId ?: uiState.students.firstOrNull()?.id.orEmpty()) }
    var subjectId by remember { mutableStateOf(existing?.record?.subjectId ?: uiState.subjects.firstOrNull()?.id.orEmpty()) }
    var attendedText by remember { mutableStateOf((existing?.record?.classesAttended ?: 32).toString()) }
    var totalText by remember { mutableStateOf((existing?.record?.totalClasses ?: 40).toString()) }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Attendance Log" else "Edit Attendance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (existing == null) {
                    FilterDropdown(
                        label = "Student",
                        selectedLabel = uiState.students.find { it.id == studentId }?.fullName ?: "Select",
                        options = uiState.students.map { it.id to "${it.rollNumber} — ${it.fullName}" },
                        onSelect = { studentId = it }
                    )
                    FilterDropdown(
                        label = "Subject",
                        selectedLabel = uiState.subjects.find { it.id == subjectId }?.code ?: "Select",
                        options = uiState.subjects.map { it.id to "${it.code} — ${it.name}" },
                        onSelect = { subjectId = it }
                    )
                } else {
                    Text("${existing.studentName} · ${existing.subjectCode}", style = MaterialTheme.typography.titleSmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValidatedTextField(
                        value = attendedText,
                        onValueChange = { attendedText = it.filter { c -> c.isDigit() }.take(4); err = null },
                        label = "Attended",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    ValidatedTextField(
                        value = totalText,
                        onValueChange = { totalText = it.filter { c -> c.isDigit() }.take(4); err = null },
                        label = "Total Classes",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (err != null) {
                    Text(err!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val att = attendedText.toIntOrNull()
                    val tot = totalText.toIntOrNull()
                    if (att == null || tot == null || tot <= 0 || att < 0 || att > tot) {
                        err = "Attended must be between 0 and Total (> 0)."
                    } else if (studentId.isNotBlank() && subjectId.isNotBlank()) {
                        onSave(studentId, subjectId, att, tot)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
