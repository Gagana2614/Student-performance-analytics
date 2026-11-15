package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AcademicSettings
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "edumetrics_settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val INSTITUTION_NAME = stringPreferencesKey("institution_name")
        val ACADEMIC_YEAR = stringPreferencesKey("academic_year")
        val ACTIVE_USER_NAME = stringPreferencesKey("active_user_name")
        val ACTIVE_USER_ROLE = stringPreferencesKey("active_user_role")
        val PASS_PERCENTAGE = intPreferencesKey("pass_percentage")
        val ATTENDANCE_THRESHOLD = intPreferencesKey("attendance_threshold")
        val EXCELLENCE_THRESHOLD = intPreferencesKey("excellence_threshold")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SEEDED_ONCE = booleanPreferencesKey("seeded_once")
    }

    val settingsFlow: Flow<AcademicSettings> = context.dataStore.data.map { prefs ->
        val roleName = prefs[Keys.ACTIVE_USER_ROLE]
        AcademicSettings(
            institutionName = prefs[Keys.INSTITUTION_NAME] ?: "EduMetrics Institute of Technology",
            academicYear = prefs[Keys.ACADEMIC_YEAR] ?: "2026–2027",
            activeUserName = prefs[Keys.ACTIVE_USER_NAME] ?: "Dr. Elena Rostova",
            activeUserRole = UserRole.entries.find { it.name == roleName } ?: UserRole.ACADEMIC_ADMIN,
            passPercentage = prefs[Keys.PASS_PERCENTAGE] ?: 40,
            attendanceThreshold = prefs[Keys.ATTENDANCE_THRESHOLD] ?: 75,
            excellenceThreshold = prefs[Keys.EXCELLENCE_THRESHOLD] ?: 85,
            darkTheme = prefs[Keys.DARK_THEME] ?: false
        )
    }

    val hasSeededFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SEEDED_ONCE] ?: false
    }

    suspend fun markSeeded() {
        context.dataStore.edit { it[Keys.SEEDED_ONCE] = true }
    }

    suspend fun updateSettings(settings: AcademicSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.INSTITUTION_NAME] = settings.institutionName
            prefs[Keys.ACADEMIC_YEAR] = settings.academicYear
            prefs[Keys.ACTIVE_USER_NAME] = settings.activeUserName
            prefs[Keys.ACTIVE_USER_ROLE] = settings.activeUserRole.name
            prefs[Keys.PASS_PERCENTAGE] = settings.passPercentage
            prefs[Keys.ATTENDANCE_THRESHOLD] = settings.attendanceThreshold
            prefs[Keys.EXCELLENCE_THRESHOLD] = settings.excellenceThreshold
            prefs[Keys.DARK_THEME] = settings.darkTheme
        }
    }

    suspend fun toggleTheme(dark: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = dark }
    }

    suspend fun updateRole(role: UserRole) {
        context.dataStore.edit { it[Keys.ACTIVE_USER_ROLE] = role.name }
    }
}
