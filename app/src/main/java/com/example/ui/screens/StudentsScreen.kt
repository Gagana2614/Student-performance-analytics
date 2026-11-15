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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AnalyticsEngine
import com.example.domain.model.Department
import com.example.domain.model.PerformanceTier
import com.example.domain.model.Student
import com.example.domain.model.StudentAnalytics
import com.example.domain.model.StudentStatus
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DemoBadge
import com.example.ui.components.EduBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterDropdown
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.PaginationBar
import com.example.ui.components.SearchField
import com.example.ui.components.TierBadge
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.viewmodel.AcademicUiState
import kotlin.math.ceil
import kotlin.math.max

private enum class StudentSort(val label: String) {
    NAME_ASC("Name (A–Z)"),
    SCORE_DESC("Highest Score"),
    SCORE_ASC("Lowest Score"),
    ATTENDANCE_ASC("Lowest Attendance"),
    SEMESTER_ASC("Semester")
}

@Composable
fun StudentsScreen(
    uiState: AcademicUiState,
    onSaveStudent: (
        existingId: String?,
        rollNumber: String,
        fullName: String,
        email: String,
        department: Department,
        semester: Int,
        enrollmentYear: Int,
        status: StudentStatus
    ) -> Unit,
    onDeleteStudent: (Student) -> Unit
) {
    var localSearch by remember { mutableStateOf("") }
    var deptFilter by remember { mutableStateOf<Department?>(null) }
    var semFilter by remember { mutableStateOf<Int?>(null) }
    var tierFilter by remember { mutableStateOf<PerformanceTier?>(null) }
    var sortBy by remember { mutableStateOf(StudentSort.NAME_ASC) }
    var currentPage by remember { mutableIntStateOf(1) }

    var showFormDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var viewingStudent by remember { mutableStateOf<StudentAnalytics?>(null) }
    var deletingStudent by remember { mutableStateOf<Student?>(null) }

    val query = (localSearch.ifBlank { uiState.globalSearch }).trim()

    val filtered = remember(uiState.studentAnalytics, query, deptFilter, semFilter, tierFilter, sortBy) {
        uiState.studentAnalytics
            .filter { item ->
                val s = item.student
                val matchQuery = query.isBlank() ||
                    s.fullName.contains(query, ignoreCase = true) ||
                    s.rollNumber.contains(query, ignoreCase = true) ||
                    s.email.contains(query, ignoreCase = true)
                val matchDept = deptFilter == null || s.department == deptFilter
                val matchSem = semFilter == null || s.currentSemester == semFilter
                val matchTier = tierFilter == null || item.tier == tierFilter
                matchQuery && matchDept && matchSem && matchTier
            }
            .let { list ->
                when (sortBy) {
                    StudentSort.NAME_ASC -> list.sortedBy { it.student.fullName }
                    StudentSort.SCORE_DESC -> list.sortedByDescending { it.averagePercentage }
                    StudentSort.SCORE_ASC -> list.sortedBy { it.averagePercentage }
                    StudentSort.ATTENDANCE_ASC -> list.sortedBy { it.attendancePercentage }
                    StudentSort.SEMESTER_ASC -> list.sortedBy { it.student.currentSemester }
                }
            }
    }

    val pageSize = 8
    val totalPages = max(1, ceil(filtered.size / pageSize.toDouble()).toInt())
    val safePage = currentPage.coerceIn(1, totalPages)
    val pageItems = remember(filtered, safePage) {
        filtered.drop((safePage - 1) * pageSize).take(pageSize)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeaderBlock(
                title = "Student Directory (${filtered.size})",
                subtitle = "Search, filter, inspect transcripts, and manage enrollments.",
                action = {
                    Button(
                        onClick = {
                            editingStudent = null
                            showFormDialog = true
                        },
                        modifier = Modifier.testTag("add_student_button")
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enroll")
                    }
                }
            )
        }

        item {
            SearchField(
                value = localSearch,
                onValueChange = {
                    localSearch = it
                    currentPage = 1
                },
                placeholder = "Search name, roll number, or email...",
                testTag = "students_search_field"
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
                    label = "Dept",
                    selectedLabel = deptFilter?.shortName ?: "All",
                    options = listOf(null to "All Departments") + Department.entries.map { it to it.displayName },
                    onSelect = {
                        deptFilter = it
                        currentPage = 1
                    }
                )
                FilterDropdown(
                    label = "Sem",
                    selectedLabel = semFilter?.let { "Sem $it" } ?: "All",
                    options = listOf(null to "All Semesters") + (1..8).map { it to "Semester $it" },
                    onSelect = {
                        semFilter = it
                        currentPage = 1
                    }
                )
                FilterDropdown(
                    label = "Tier",
                    selectedLabel = tierFilter?.label ?: "All",
                    options = listOf(null to "All Tiers") + PerformanceTier.entries.map { it to it.label },
                    onSelect = {
                        tierFilter = it
                        currentPage = 1
                    }
                )
                FilterDropdown(
                    label = "Sort",
                    selectedLabel = sortBy.label,
                    options = StudentSort.entries.map { it to it.label },
                    onSelect = { sortBy = it }
                )
            }
        }

        if (pageItems.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No matching students",
                    description = "Adjust your search/filters or enroll a new student record.",
                    actionLabel = "Enroll Student",
                    onAction = {
                        editingStudent = null
                        showFormDialog = true
                    }
                )
            }
        } else {
            items(pageItems, key = { it.student.id }) { item ->
                StudentListCard(
                    item = item,
                    attendanceThreshold = uiState.settings.attendanceThreshold,
                    onView = { viewingStudent = item },
                    onEdit = {
                        editingStudent = item.student
                        showFormDialog = true
                    },
                    onDelete = { deletingStudent = item.student }
                )
            }
        }

        item {
            PaginationBar(
                currentPage = safePage,
                totalPages = totalPages,
                totalItems = filtered.size,
                onPrevious = { currentPage = (safePage - 1).coerceAtLeast(1) },
                onNext = { currentPage = (safePage + 1).coerceAtMost(totalPages) }
            )
        }
    }

    if (showFormDialog) {
        StudentFormDialog(
            existing = editingStudent,
            existingRollNumbers = uiState.students
                .filter { it.id != editingStudent?.id }
                .map { it.rollNumber.uppercase() }
                .toSet(),
            onDismiss = { showFormDialog = false },
            onSave = { roll, name, email, dept, sem, year, status ->
                onSaveStudent(editingStudent?.id, roll, name, email, dept, sem, year, status)
                showFormDialog = false
            }
        )
    }

    viewingStudent?.let { profile ->
        StudentProfileDialog(
            profile = profile,
            uiState = uiState,
            onDismiss = { viewingStudent = null }
        )
    }

    deletingStudent?.let { stu ->
        ConfirmDeleteDialog(
            title = "Delete Student Record",
            message = "Remove ${stu.fullName} (${stu.rollNumber})? This permanently deletes their profile along with all linked marks and attendance logs.",
            onConfirm = {
                onDeleteStudent(stu)
                deletingStudent = null
            },
            onDismiss = { deletingStudent = null }
        )
    }
}

@Composable
private fun StudentListCard(
    item: StudentAnalytics,
    attendanceThreshold: Int,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val s = item.student
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
                            text = s.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        DemoBadge(isDemo = s.isDemo)
                    }
                    Text(
                        text = "${s.rollNumber} · ${s.department.displayName} · Sem ${s.currentSemester}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = s.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TierBadge(tier = item.tier)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score: ${item.averagePercentage}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GPA ${item.gpa}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AttendanceBadge(percentage = item.attendancePercentage, threshold = attendanceThreshold)
                }

                Row {
                    IconButton(onClick = onView, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Visibility, contentDescription = "View transcript")
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit student")
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete student",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentFormDialog(
    existing: Student?,
    existingRollNumbers: Set<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Department, Int, Int, StudentStatus) -> Unit
) {
    var rollNumber by remember { mutableStateOf(existing?.rollNumber ?: "") }
    var fullName by remember { mutableStateOf(existing?.fullName ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var department by remember { mutableStateOf(existing?.department ?: Department.COMPUTER_SCIENCE) }
    var semester by remember { mutableIntStateOf(existing?.currentSemester ?: 2) }
    var yearText by remember { mutableStateOf((existing?.enrollmentYear ?: 2026).toString()) }
    var status by remember { mutableStateOf(existing?.status ?: StudentStatus.ACTIVE) }

    var rollError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Enroll Student" else "Edit Student Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ValidatedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it; rollError = null },
                    label = "Roll Number (e.g. CS2610)",
                    error = rollError
                )
                ValidatedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; nameError = null },
                    label = "Full Name",
                    error = nameError
                )
                ValidatedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = "Academic Email",
                    error = emailError,
                    keyboardType = KeyboardType.Email
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ValidatedTextField(
                        value = yearText,
                        onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = "Year",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    FilterDropdown(
                        label = "Status",
                        selectedLabel = status.label,
                        options = StudentStatus.entries.map { it to it.label },
                        onSelect = { status = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rollNumber.trim().uppercase()
                    val n = fullName.trim()
                    val e = email.trim()
                    rollError = when {
                        r.length < 3 -> "Min 3 characters required"
                        existingRollNumbers.contains(r) -> "Roll number already exists"
                        else -> null
                    }
                    nameError = if (n.length < 2) "Enter student's full name" else null
                    emailError = if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(e)) "Enter valid email" else null

                    if (rollError == null && nameError == null && emailError == null) {
                        val yr = yearText.toIntOrNull() ?: 2026
                        onSave(r, n, e, department, semester, yr, status)
                    }
                },
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text(if (existing == null) "Enroll" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StudentProfileDialog(
    profile: StudentAnalytics,
    uiState: AcademicUiState,
    onDismiss: () -> Unit
) {
    val studentMarks = remember(uiState.marks, profile.student.id) {
        uiState.marks.filter { it.studentId == profile.student.id }
    }
    val subjectMap = remember(uiState.subjects) { uiState.subjects.associateBy { it.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(profile.student.fullName, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${profile.student.rollNumber} · ${profile.student.department.displayName} · Sem ${profile.student.currentSemester}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mean Score: ${profile.averagePercentage}%", fontWeight = FontWeight.Bold)
                    Text("GPA: ${profile.gpa}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Att: ${profile.attendancePercentage}%", fontWeight = FontWeight.Bold)
                }
                TierBadge(tier = profile.tier)
                HorizontalDivider()
                Text("Subject Transcript", style = MaterialTheme.typography.titleSmall)
                if (studentMarks.isEmpty()) {
                    Text(
                        text = "No assessments recorded for this student yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    studentMarks.forEach { m ->
                        val sub = subjectMap[m.subjectId]
                        val pct = AnalyticsEngine.computeMarkPercentage(
                            m.internalMarks,
                            m.externalMarks,
                            sub?.maxInternal ?: 40,
                            sub?.maxExternal ?: 60
                        )
                        val (grade, _) = AnalyticsEngine.getLetterGrade(pct)
                        val passed = pct >= uiState.settings.passPercentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${sub?.code ?: "SUB"} · ${sub?.name ?: "Subject"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Int ${m.internalMarks}/${sub?.maxInternal ?: 40} · Ext ${m.externalMarks}/${sub?.maxExternal ?: 60}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            EduBadge(
                                text = "$pct% ($grade)",
                                containerColor = if (passed) Emerald100 else Rose100,
                                contentColor = if (passed) Emerald600 else Rose600
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
