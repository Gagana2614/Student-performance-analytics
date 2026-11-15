package com.example

import com.example.data.local.SeedData
import com.example.domain.model.AcademicSettings
import com.example.domain.model.AnalyticsEngine
import com.example.domain.model.PerformanceTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEngineTest {

    @Test
    fun seedBundleHasExpectedCountsAndConsistency() {
        val bundle = SeedData.createSeedBundle()
        assertTrue("Expected >= 20 demo students", bundle.students.size >= 20)
        assertTrue("Expected >= 5 demo subjects", bundle.subjects.size >= 5)
        assertTrue("Expected demo marks", bundle.marks.isNotEmpty())
        assertTrue("Expected demo attendance", bundle.attendance.isNotEmpty())

        val settings = AcademicSettings()
        val studentAnalytics = AnalyticsEngine.computeStudentAnalytics(
            students = bundle.students,
            subjects = bundle.subjects,
            marks = bundle.marks,
            attendance = bundle.attendance,
            settings = settings
        )

        assertTrue(studentAnalytics.any { it.tier == PerformanceTier.EXCELLENT })
        assertTrue(studentAnalytics.any { it.tier == PerformanceTier.AVERAGE })
        assertTrue(studentAnalytics.any { it.tier == PerformanceTier.AT_RISK })
    }

    @Test
    fun pearsonCorrelationReflectsPositiveRelationship() {
        val points = listOf(50.0 to 40.0, 70.0 to 65.0, 90.0 to 90.0)
        val r = AnalyticsEngine.computePearsonCorrelation(points)
        assertEquals(1.0, r, 0.05)
    }

    @Test
    fun letterGradesAndPercentagesMatchThresholds() {
        assertEquals(85.0, AnalyticsEngine.computeMarkPercentage(34, 51, 40, 60), 0.01)
        assertEquals("A+" to 10, AnalyticsEngine.getLetterGrade(92.0))
        assertEquals("F" to 0, AnalyticsEngine.getLetterGrade(32.0))
    }
}
