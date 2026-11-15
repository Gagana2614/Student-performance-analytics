package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.layout.EduMetricsShell
import com.example.ui.navigation.Routes
import com.example.ui.screens.AIInsightsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarksScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.theme.EduMetricsTheme
import com.example.ui.viewmodel.AcademicViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduMetricsApp()
        }
    }
}

@Composable
fun EduMetricsApp(viewModel: AcademicViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.DASHBOARD
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.uiMessages.collect { msg ->
            snackbarHostState.showSnackbar(
                message = msg.text,
                duration = SnackbarDuration.Short
            )
        }
    }

    val navigateTo: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    EduMetricsTheme(darkTheme = uiState.settings.darkTheme) {
        EduMetricsShell(
            uiState = uiState,
            currentRoute = currentRoute,
            snackbarHostState = snackbarHostState,
            onNavigate = navigateTo,
            onSearchChange = viewModel::setGlobalSearch,
            onToggleTheme = viewModel::toggleTheme,
            onSelectRole = viewModel::updateRole,
            onMarkAllRead = viewModel::markAllNotificationsRead,
            onRetryLoad = viewModel::loadInitialData
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(uiState = uiState, onNavigate = navigateTo)
                }
                composable(Routes.STUDENTS) {
                    StudentsScreen(
                        uiState = uiState,
                        onSaveStudent = viewModel::saveStudent,
                        onDeleteStudent = viewModel::deleteStudent
                    )
                }
                composable(Routes.SUBJECTS) {
                    SubjectsScreen(
                        uiState = uiState,
                        onSaveSubject = viewModel::saveSubject,
                        onDeleteSubject = viewModel::deleteSubject
                    )
                }
                composable(Routes.MARKS) {
                    MarksScreen(
                        uiState = uiState,
                        onSaveMark = viewModel::saveMark,
                        onDeleteMark = viewModel::deleteMark
                    )
                }
                composable(Routes.ATTENDANCE) {
                    AttendanceScreen(
                        uiState = uiState,
                        onSaveAttendance = viewModel::saveAttendance,
                        onQuickIncrement = viewModel::quickIncrementAttendance,
                        onDeleteAttendance = viewModel::deleteAttendance
                    )
                }
                composable(Routes.ANALYTICS) {
                    AnalyticsScreen(uiState = uiState)
                }
                composable(Routes.AI_INSIGHTS) {
                    AIInsightsScreen(uiState = uiState, onNavigate = navigateTo)
                }
                composable(Routes.REPORTS) {
                    ReportsScreen(
                        uiState = uiState,
                        onBuildCsv = viewModel::buildReportCsv,
                        onShowMessage = { text ->
                            scope.launch { snackbarHostState.showSnackbar(text) }
                        }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        uiState = uiState,
                        onSaveSettings = viewModel::saveSettings,
                        onToggleTheme = viewModel::setTheme,
                        onRestoreDemo = viewModel::restoreDemoData,
                        onClearDemoOnly = viewModel::clearDemoRecordsOnly
                    )
                }
            }
        }
    }
}
