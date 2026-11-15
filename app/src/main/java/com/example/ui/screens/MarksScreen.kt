package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.domain.model.MarkRecord
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DemoBadge
import com.example.ui.components.EduBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterDropdown
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.PaginationBar
import com.example.ui.components.SearchField
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.theme.Violet100
import com.example.ui.theme.Violet600
import com.example.ui.viewmodel.AcademicUiState
import kotlin.math.ceil
import kotlin.math.max

private data class EnrichedMarkRow(
    val record: MarkRecord,
    val studentName: String,
    val rollNumber: String,
    val subjectCode: String,
    val subjectName: String,
    val maxInternal: Int,
    val maxExternal: Int,
    val percentage: Double,
    val grade: String,
    val passed: Boolean
)

@Composable
fun MarksScreen(
    uiState: AcademicUiState,
    onSaveMark: (existingId: String?, studentId: String, subjectId: String, internal: Int, external: Int) -> Unit,
    onDeleteMark: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var subjectFilter by remember { mutableStateOf<String?>(null) }
    var semFilter by remember { mutableStateOf<Int?>(null) }
    var sortDesc by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(1) }

    var showDialog by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<EnrichedMarkRow?>(null) }
    var deletingRow by remember { mutableStateOf<EnrichedMarkRow?>(null) }

    val enriched = remember(uiState.marks, uiState.students, uiState.subjects, uiState.settings.passPercentage, search, subjectFilter, semFilter, sortDesc) {
        val stuMap = uiState.students.associateBy { it.id }
        val subMap = uiState.subjects.associateBy { it.id }
        val q = search.trim()

        uiState.marks.map { rec ->
            val stu = stuMap[rec.studentId]
            val sub = subMap[rec.subjectId]
            val maxInt = sub?.maxInternal ?: 40
            val maxExt = sub?.maxExternal ?: 60
            val pct = AnalyticsEngine.computeMarkPercentage(rec.internalMarks, rec.externalMarks, maxInt, maxExt)
            val (grade, _) = AnalyticsEngine.getLetterGrade(pct)
            EnrichedMarkRow(
                record = rec,
                studentName = stu?.fullName ?: "Unknown Student",
                rollNumber = stu?.rollNumber ?: "—",
                subjectCode = sub?.code ?: "—",
                subjectName = sub?.name ?: "Unknown Subject",
                maxInternal = maxInt,
                maxExternal = maxExt,
                percentage = pct,
                grade = grade,
                passed = pct >= uiState.settings.passPercentage
            )
        }.filter { row ->
            val matchQuery = q.isBlank() ||
                row.studentName.contains(q, ignoreCase = true) ||
                row.rollNumber.contains(q, ignoreCase = true) ||
                row.subjectCode.contains(q, ignoreCase = true)
            val matchSub = subjectFilter == null || row.record.subjectId == subjectFilter
            val matchSem = semFilter == null || row.record.semester == semFilter
            matchQuery && matchSub && matchSem
        }.let { list ->
            if (sortDesc) list.sortedByDescending { it.percentage } else list.sortedBy { it.percentage }
        }
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
                title = "Marks & Assessments (${enriched.size})",
                subtitle = "Record internal & external scores with automatic grading.",
                action = {
                    Button(
                        onClick = {
                            editingRow = null
                            showDialog = true
                        },
                        enabled = uiState.students.isNotEmpty() && uiState.subjects.isNotEmpty(),
                        modifier = Modifier.testTag("add_mark_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record")
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
                placeholder = "Search student, roll no, or subject code..."
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    label = "Subject",
                    selectedLabel = uiState.subjects.find { it.id == subjectFilter }?.code ?: "All",
                    options = listOf(null to "All Subjects") + uiState.subjects.map { it.id to "${it.code} — ${it.name}" },
                    onSelect = {
                        subjectFilter = it
                        currentPage = 1
                    }
                )
                FilterDropdown(
                    label = "Sem",
                    selectedLabel = semFilter?.let { "Sem $it" } ?: "All",
                    options = listOf(null to "All") + (1..8).map { it to "Sem $it" },
                    onSelect = {
                        semFilter = it
                        currentPage = 1
                    }
                )
                FilterDropdown(
                    label = "Sort",
                    selectedLabel = if (sortDesc) "Highest %" else "Lowest %",
                    options = listOf(true to "Highest %", false to "Lowest %"),
                    onSelect = { sortDesc = it }
                )
            }
        }

        if (pageItems.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No assessment marks found",
                    description = "Adjust filters or record marks for an enrolled student.",
                    actionLabel = if (uiState.students.isNotEmpty() && uiState.subjects.isNotEmpty()) "Record Marks" else null,
                    onAction = {
                        editingRow = null
                        showDialog = true
                    }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    text = "${row.rollNumber} · Sem ${row.record.semester}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Int ${row.record.internalMarks}/${row.maxInternal} + Ext ${row.record.externalMarks}/${row.maxExternal}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${row.percentage}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            EduBadge(
                                text = "${row.grade} · ${if (row.passed) "Pass" else "Fail"}",
                                containerColor = if (row.passed) Emerald100 else Rose100,
                                contentColor = if (row.passed) Emerald600 else Rose600
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        editingRow = row
                                        showDialog = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit marks", modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { deletingRow = row },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete marks",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
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
        MarkFormDialog(
            uiState = uiState,
            existing = editingRow,
            onDismiss = { showDialog = false },
            onSave = { stuId, subId, internal, external ->
                onSaveMark(editingRow?.record?.id, stuId, subId, internal, external)
                showDialog = false
            }
        )
    }

    deletingRow?.let { row ->
        ConfirmDeleteDialog(
            title = "Delete Mark Record",
            message = "Delete assessment marks for ${row.studentName} in ${row.subjectCode}?",
            onConfirm = {
                onDeleteMark(row.record.id)
                deletingRow = null
            },
            onDismiss = { deletingRow = null }
        )
    }
}

@Composable
private fun MarkFormDialog(
    uiState: AcademicUiState,
    existing: EnrichedMarkRow?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int) -> Unit
) {
    var studentId by remember { mutableStateOf(existing?.record?.studentId ?: uiState.students.firstOrNull()?.id.orEmpty()) }
    var subjectId by remember { mutableStateOf(existing?.record?.subjectId ?: uiState.subjects.firstOrNull()?.id.orEmpty()) }
    var intText by remember { mutableStateOf((existing?.record?.internalMarks ?: 30).toString()) }
    var extText by remember { mutableStateOf((existing?.record?.externalMarks ?: 45).toString()) }
    var intErr by remember { mutableStateOf<String?>(null) }
    var extErr by remember { mutableStateOf<String?>(null) }

    val selectedSub = uiState.subjects.find { it.id == subjectId }
    val maxInt = selectedSub?.maxInternal ?: 40
    val maxExt = selectedSub?.maxExternal ?: 60

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Record Marks" else "Edit Mark Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (existing == null) {
                    FilterDropdown(
                        label = "Student",
                        selectedLabel = uiState.students.find { it.id == studentId }?.let { "${it.rollNumber} ${it.fullName}" } ?: "Select",
                        options = uiState.students.map { it.id to "${it.rollNumber} — ${it.fullName}" },
                        onSelect = { studentId = it }
                    )
                    FilterDropdown(
                        label = "Subject",
                        selectedLabel = selectedSub?.let { "${it.code} (Sem ${it.semester})" } ?: "Select",
                        options = uiState.subjects.map { it.id to "${it.code} — ${it.name}" },
                        onSelect = { subjectId = it }
                    )
                } else {
                    Text(
                        text = "${existing.studentName} · ${existing.subjectCode}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValidatedTextField(
                        value = intText,
                        onValueChange = { intText = it.filter { c -> c.isDigit() }.take(3); intErr = null },
                        label = "Internal (0–$maxInt)",
                        error = intErr,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    ValidatedTextField(
                        value = extText,
                        onValueChange = { extText = it.filter { c -> c.isDigit() }.take(3); extErr = null },
                        label = "External (0–$maxExt)",
                        error = extErr,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val iVal = intText.toIntOrNull()
                    val eVal = extText.toIntOrNull()
                    intErr = if (iVal == null || iVal < 0 || iVal > maxInt) "0 to $maxInt" else null
                    extErr = if (eVal == null || eVal < 0 || eVal > maxExt) "0 to $maxExt" else null
                    if (intErr == null && extErr == null && studentId.isNotBlank() && subjectId.isNotBlank()) {
                        onSave(studentId, subjectId, iVal!!, eVal!!)
                    }
                }
            ) {
                Text("Save Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
