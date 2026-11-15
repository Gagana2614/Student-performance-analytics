package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.AcademicSettings
import com.example.domain.model.UserRole
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.FilterDropdown
import com.example.ui.components.PageHeaderBlock
import com.example.ui.components.SectionCard
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.Rose600
import com.example.ui.viewmodel.AcademicUiState

@Composable
fun SettingsScreen(
    uiState: AcademicUiState,
    onSaveSettings: (AcademicSettings) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onRestoreDemo: () -> Unit,
    onClearDemoOnly: () -> Unit
) {
    var institution by remember { mutableStateOf(uiState.settings.institutionName) }
    var academicYear by remember { mutableStateOf(uiState.settings.academicYear) }
    var userName by remember { mutableStateOf(uiState.settings.activeUserName) }
    var role by remember { mutableStateOf(uiState.settings.activeUserRole) }
    var passPct by remember { mutableStateOf(uiState.settings.passPercentage.toString()) }
    var attPct by remember { mutableStateOf(uiState.settings.attendanceThreshold.toString()) }
    var excPct by remember { mutableStateOf(uiState.settings.excellenceThreshold.toString()) }
    var thresholdError by remember { mutableStateOf<String?>(null) }

    var confirmAction by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.settings) {
        institution = uiState.settings.institutionName
        academicYear = uiState.settings.academicYear
        userName = uiState.settings.activeUserName
        role = uiState.settings.activeUserRole
        passPct = uiState.settings.passPercentage.toString()
        attPct = uiState.settings.attendanceThreshold.toString()
        excPct = uiState.settings.excellenceThreshold.toString()
    }

    val demoStudents = uiState.students.count { it.isDemo }
    val customStudents = uiState.students.size - demoStudents

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeaderBlock(
                title = "Platform Settings",
                subtitle = "Configure evaluation thresholds, appearance, and demonstration data controls."
            )
        }

        item {
            SectionCard(
                title = "Academic Policies & Profile",
                subtitle = "Thresholds dynamically update grades, tiers, and risk warnings across the app"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ValidatedTextField(
                        value = institution,
                        onValueChange = { institution = it },
                        label = "Institution Name"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ValidatedTextField(
                            value = academicYear,
                            onValueChange = { academicYear = it },
                            label = "Academic Year",
                            modifier = Modifier.weight(1f)
                        )
                        ValidatedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = "Administrator Name",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FilterDropdown(
                        label = "Role",
                        selectedLabel = role.label,
                        options = UserRole.entries.map { it to it.label },
                        onSelect = { role = it }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ValidatedTextField(
                            value = passPct,
                            onValueChange = { passPct = it.filter { c -> c.isDigit() }.take(2); thresholdError = null },
                            label = "Pass % Min",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        ValidatedTextField(
                            value = attPct,
                            onValueChange = { attPct = it.filter { c -> c.isDigit() }.take(2); thresholdError = null },
                            label = "Att % Min",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        ValidatedTextField(
                            value = excPct,
                            onValueChange = { excPct = it.filter { c -> c.isDigit() }.take(2); thresholdError = null },
                            label = "Excel % Min",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (thresholdError != null) {
                        Text(thresholdError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Theme", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Switch between Lavender Light & Slate Dark",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.settings.darkTheme,
                            onCheckedChange = onToggleTheme,
                            modifier = Modifier.testTag("settings_theme_switch")
                        )
                    }

                    Button(
                        onClick = {
                            val p = passPct.toIntOrNull()
                            val a = attPct.toIntOrNull()
                            val e = excPct.toIntOrNull()
                            if (p == null || a == null || e == null || p !in 1..90 || a !in 1..100 || e !in (p + 1)..100) {
                                thresholdError = "Ensure Pass (1–90), Attendance (1–100), and Excellence (> Pass) are valid."
                            } else {
                                onSaveSettings(
                                    uiState.settings.copy(
                                        institutionName = institution.trim().ifBlank { "EduMetrics Institute" },
                                        academicYear = academicYear.trim().ifBlank { "2026–2027" },
                                        activeUserName = userName.trim().ifBlank { "Dr. Elena Rostova" },
                                        activeUserRole = role,
                                        passPercentage = p,
                                        attendanceThreshold = a,
                                        excellenceThreshold = e
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_settings_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Configuration")
                    }
                }
            }
        }

        item {
            SectionCard(
                title = "Data Persistence & Demo Controls",
                subtitle = "Room SQLite + DataStore persistent storage"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Active Records: ${uiState.students.size} students ($demoStudents demo, $customStudents custom) · ${uiState.subjects.size} subjects · ${uiState.marks.size} marks · ${uiState.attendance.size} attendance logs.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { confirmAction = "clear-demo" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Demo Only")
                        }
                        Button(
                            onClick = { confirmAction = "restore" },
                            colors = ButtonDefaults.buttonColors(containerColor = Rose600, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore Demo")
                        }
                    }
                }
            }
        }
    }

    if (confirmAction != null) {
        val isRestore = confirmAction == "restore"
        ConfirmDeleteDialog(
            title = if (isRestore) "Restore Demonstration Dataset" else "Clear Demo Records Only",
            message = if (isRestore) {
                "This replaces the current database with the original 22-student demonstration dataset."
            } else {
                "This removes all pre-loaded demonstration records while keeping every student, subject, mark, and attendance entry you added."
            },
            confirmLabel = if (isRestore) "Restore Demo" else "Clear Demo",
            onConfirm = {
                if (isRestore) onRestoreDemo() else onClearDemoOnly()
                confirmAction = null
            },
            onDismiss = { confirmAction = null }
        )
    }
}
