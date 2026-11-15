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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.example.domain.model.Department
import com.example.domain.model.Subject
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DemoBadge
import com.example.ui.components.EduBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterDropdown
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.SearchField
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Violet100
import com.example.ui.theme.Violet600
import com.example.ui.viewmodel.AcademicUiState

@Composable
fun SubjectsScreen(
    uiState: AcademicUiState,
    onSaveSubject: (
        existingId: String?,
        code: String,
        name: String,
        department: Department,
        semester: Int,
        credits: Int,
        facultyName: String,
        maxInternal: Int,
        maxExternal: Int
    ) -> Unit,
    onDeleteSubject: (Subject) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var semFilter by remember { mutableStateOf<Int?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Subject?>(null) }
    var deleting by remember { mutableStateOf<Subject?>(null) }

    val filtered = remember(uiState.subjectAnalytics, search, semFilter) {
        uiState.subjectAnalytics.filter { sa ->
            val q = search.trim()
            val matchQuery = q.isBlank() ||
                sa.subject.name.contains(q, ignoreCase = true) ||
                sa.subject.code.contains(q, ignoreCase = true) ||
                sa.subject.facultyName.contains(q, ignoreCase = true)
            val matchSem = semFilter == null || sa.subject.semester == semFilter
            matchQuery && matchSem
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeaderBlock(
                title = "Curriculum Subjects (${filtered.size})",
                subtitle = "Manage course codes, credits, faculty leads, and max assessment marks.",
                action = {
                    Button(
                        onClick = {
                            editing = null
                            showForm = true
                        },
                        modifier = Modifier.testTag("add_subject_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add")
                    }
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Search code, title, or faculty...",
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    label = "Sem",
                    selectedLabel = semFilter?.let { "Sem $it" } ?: "All",
                    options = listOf(null to "All") + (1..8).map { it to "Sem $it" },
                    onSelect = { semFilter = it }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No matching subjects",
                    description = "Adjust filters or create a new academic subject.",
                    actionLabel = "Add Subject",
                    onAction = {
                        editing = null
                        showForm = true
                    }
                )
            }
        } else {
            items(filtered, key = { it.subject.id }) { sa ->
                val sub = sa.subject
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
                                    EduBadge(text = sub.code, containerColor = Violet100, contentColor = Violet600)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DemoBadge(isDemo = sub.isDemo)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = sub.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${sub.department.displayName} · Sem ${sub.semester} · ${sub.credits} Credits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Faculty: ${sub.facultyName} · Max ${sub.maxInternal}+${sub.maxExternal}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            EduBadge(
                                text = "Pass ${sa.passRate}%",
                                containerColor = if (sa.passRate >= 75.0) Emerald100 else Amber100,
                                contentColor = if (sa.passRate >= 75.0) Emerald600 else Amber600
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Class Avg: ${sa.averagePercentage}% · ${sa.enrolledCount} evaluated",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row {
                                IconButton(onClick = {
                                    editing = sub
                                    showForm = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit subject")
                                }
                                IconButton(onClick = { deleting = sub }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete subject",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        SubjectFormDialog(
            existing = editing,
            existingCodes = uiState.subjects
                .filter { it.id != editing?.id }
                .map { it.code.uppercase() }
                .toSet(),
            onDismiss = { showForm = false },
            onSave = { code, name, dept, sem, credits, faculty, maxInt, maxExt ->
                onSaveSubject(editing?.id, code, name, dept, sem, credits, faculty, maxInt, maxExt)
                showForm = false
            }
        )
    }

    deleting?.let { sub ->
        ConfirmDeleteDialog(
            title = "Delete Subject",
            message = "Deleting ${sub.code} (${sub.name}) will remove all linked student marks and attendance logs.",
            onConfirm = {
                onDeleteSubject(sub)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun SubjectFormDialog(
    existing: Subject?,
    existingCodes: Set<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Department, Int, Int, String, Int, Int) -> Unit
) {
    var code by remember { mutableStateOf(existing?.code ?: "") }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var department by remember { mutableStateOf(existing?.department ?: Department.COMPUTER_SCIENCE) }
    var semester by remember { mutableIntStateOf(existing?.semester ?: 2) }
    var creditsText by remember { mutableStateOf((existing?.credits ?: 4).toString()) }
    var facultyName by remember { mutableStateOf(existing?.facultyName ?: "") }
    var maxIntText by remember { mutableStateOf((existing?.maxInternal ?: 40).toString()) }
    var maxExtText by remember { mutableStateOf((existing?.maxExternal ?: 60).toString()) }

    var codeErr by remember { mutableStateOf<String?>(null) }
    var nameErr by remember { mutableStateOf<String?>(null) }
    var facErr by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Create Academic Subject" else "Edit Subject") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ValidatedTextField(
                    value = code,
                    onValueChange = { code = it; codeErr = null },
                    label = "Subject Code (e.g. CS501)",
                    error = codeErr
                )
                ValidatedTextField(
                    value = name,
                    onValueChange = { name = it; nameErr = null },
                    label = "Subject Title",
                    error = nameErr
                )
                ValidatedTextField(
                    value = facultyName,
                    onValueChange = { facultyName = it; facErr = null },
                    label = "Faculty Name",
                    error = facErr
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterDropdown(
                        label = "Dept",
                        selectedLabel = department.shortName,
                        options = Department.entries.map { it to it.displayName },
                        onSelect = { department = it }
                    )
                    FilterDropdown(
                        label = "Sem",
                        selectedLabel = "Sem $semester",
                        options = (1..8).map { it to "Semester $it" },
                        onSelect = { semester = it }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValidatedTextField(
                        value = creditsText,
                        onValueChange = { creditsText = it.filter { c -> c.isDigit() }.take(1) },
                        label = "Credits",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    ValidatedTextField(
                        value = maxIntText,
                        onValueChange = { maxIntText = it.filter { c -> c.isDigit() }.take(3) },
                        label = "Max Int",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    ValidatedTextField(
                        value = maxExtText,
                        onValueChange = { maxExtText = it.filter { c -> c.isDigit() }.take(3) },
                        label = "Max Ext",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = code.trim().uppercase()
                    val n = name.trim()
                    val f = facultyName.trim()
                    codeErr = when {
                        c.length < 3 -> "Min 3 characters"
                        existingCodes.contains(c) -> "Code already exists"
                        else -> null
                    }
                    nameErr = if (n.length < 3) "Title is required" else null
                    facErr = if (f.length < 2) "Faculty name required" else null

                    if (codeErr == null && nameErr == null && facErr == null) {
                        val cr = (creditsText.toIntOrNull() ?: 4).coerceIn(1, 6)
                        val mInt = (maxIntText.toIntOrNull() ?: 40).coerceIn(10, 100)
                        val mExt = (maxExtText.toIntOrNull() ?: 60).coerceIn(10, 100)
                        onSave(c, n, department, semester, cr, f, mInt, mExt)
                    }
                }
            ) {
                Text(if (existing == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
