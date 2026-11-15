package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val DASHBOARD = "dashboard"
    const val STUDENTS = "students"
    const val SUBJECTS = "subjects"
    const val MARKS = "marks"
    const val ATTENDANCE = "attendance"
    const val ANALYTICS = "analytics"
    const val AI_INSIGHTS = "ai_insights"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
}

data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val EduDestinations = listOf(
    NavDestination(Routes.DASHBOARD, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    NavDestination(Routes.STUDENTS, "Students", Icons.Filled.Group, Icons.Outlined.Group),
    NavDestination(Routes.SUBJECTS, "Subjects", Icons.Filled.Book, Icons.Outlined.Book),
    NavDestination(Routes.MARKS, "Marks", Icons.Filled.Grading, Icons.Outlined.Grading),
    NavDestination(Routes.ATTENDANCE, "Attendance", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable),
    NavDestination(Routes.ANALYTICS, "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    NavDestination(Routes.AI_INSIGHTS, "AI Insights", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    NavDestination(Routes.REPORTS, "Reports", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    NavDestination(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)
