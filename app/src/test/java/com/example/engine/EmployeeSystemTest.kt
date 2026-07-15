package com.example.engine

import org.junit.Assert.*
import org.junit.Test

class EmployeeSystemTest {

    @Test
    fun testGenerateCandidate() {
        val candidate = EmployeeSystem.generateCandidate(
            role = "Developer",
            level = "Senior",
            week = 1
        )

        assertEquals("Developer", candidate.role)
        assertEquals("Senior", candidate.skillLevel)
        assertTrue(candidate.experienceYears in 4.0..9.0)
        assertTrue(candidate.skillScore in 60..80)
        assertTrue(candidate.technicalSkill >= 50.0)
        assertTrue(candidate.salary > 0.0)
        assertEquals("Remote", candidate.workMode) // Developers default to Remote
        assertFalse(candidate.onVacation)
    }

    @Test
    fun testGenerateRecruitmentPipeline() {
        val pipeline = EmployeeSystem.generateRecruitmentPipeline(week = 3, count = 8)
        
        assertEquals(8, pipeline.size)
        pipeline.forEach { candidate ->
            assertNotNull(candidate.id)
            assertNotNull(candidate.name)
            assertNotNull(candidate.role)
            assertNotNull(candidate.skillLevel)
            assertTrue(candidate.skillScore in 1..100)
            assertTrue(candidate.salary > 0.0)
        }
    }

    @Test
    fun testScreenCandidate() {
        val candidate = EmployeeSystem.generateCandidate("Designer", "Junior", 1)
        val screened = EmployeeSystem.screenCandidate(candidate)

        assertNotNull(screened)
        assertTrue(screened.skillScore in (candidate.skillScore - 4)..(candidate.skillScore + 4))
    }

    @Test
    fun testTrainEmployee() {
        val employee = EmployeeSystem.generateCandidate("Product Manager", "Mid", 1)
            .copy(technicalSkill = 40.0, softSkill = 50.0, skillScore = 45)

        val trainedTech = EmployeeSystem.trainEmployee(employee, "Technical")
        assertTrue(trainedTech.technicalSkill > employee.technicalSkill)
        assertEquals(employee.softSkill, trainedTech.softSkill, 0.01)

        val trainedSoft = EmployeeSystem.trainEmployee(employee, "SoftSkills")
        assertTrue(trainedSoft.softSkill > employee.softSkill)
        assertEquals(employee.technicalSkill, trainedSoft.technicalSkill, 0.01)
    }

    @Test
    fun testSendOnVacation() {
        var employee = EmployeeSystem.generateCandidate("Sales", "Senior", 1)
            .copy(vacationDaysAccumulated = 15.0)

        // Attempt vacation with insufficient days
        val failedVacation = EmployeeSystem.sendOnVacation(employee, weeks = 4) // needs 20 days
        assertFalse(failedVacation.onVacation)
        assertEquals(15.0, failedVacation.vacationDaysAccumulated, 0.01)

        // Successful vacation
        employee = EmployeeSystem.sendOnVacation(employee, weeks = 2) // needs 10 days
        assertTrue(employee.onVacation)
        assertEquals(2, employee.vacationWeeksRemaining)
        assertEquals(5.0, employee.vacationDaysAccumulated, 0.01)
    }

    @Test
    fun testPromoteEmployee() {
        val employee = EmployeeSystem.generateCandidate("Finance", "Junior", 1)
            .copy(salary = 4000.0, satisfaction = 50.0, loyalty = 60.0)

        val promoted = EmployeeSystem.promoteEmployee(employee, "Senior", salaryIncreasePercent = 25.0)
        assertEquals("Senior", promoted.skillLevel)
        assertEquals(5000.0, promoted.salary, 0.01)
        assertTrue(promoted.satisfaction > employee.satisfaction)
        assertTrue(promoted.loyalty > employee.loyalty)
    }

    @Test
    fun testSetWorkMode() {
        val employee = EmployeeSystem.generateCandidate("Developer", "Senior", 1)
            .copy(workMode = "Remote")

        val updated = EmployeeSystem.setWorkMode(employee, "Office")
        assertEquals("Office", updated.workMode)

        val invalid = EmployeeSystem.setWorkMode(updated, "Hybrid")
        assertEquals("Office", invalid.workMode) // Unchanged
    }

    @Test
    fun testConductPerformanceReview() {
        val employee = EmployeeSystem.generateCandidate("Legal", "Lead", 1)
            .copy(satisfaction = 50.0, stress = 40.0)

        val reviewed = EmployeeSystem.conductPerformanceReview(
            employee = employee,
            week = 4,
            rating = 4.5,
            feedback = "Outstanding support on Series A contract preparation."
        )

        assertEquals(1, reviewed.performanceReviews.size)
        assertEquals(4.5, reviewed.performanceReviews[0].rating, 0.01)
        assertEquals("Outstanding support on Series A contract preparation.", reviewed.performanceReviews[0].feedback)
        assertTrue(reviewed.satisfaction > employee.satisfaction)
        assertTrue(reviewed.stress < employee.stress)
    }

    @Test
    fun testSimulateEmployeeWeeklyBehaviorActive() {
        val employee = EmployeeSystem.generateCandidate("Developer", "Junior", 1).copy(
            weeklyWorkload = 45.0, // High workload
            workMode = "Office", // Developers hate Office work in our model
            tenureWeeks = 10,
            vacationDaysAccumulated = 10.0,
            satisfaction = 75.0,
            stress = 20.0,
            health = 90.0,
            loyalty = 80.0,
            salary = 3000.0 // Low salary to ensure no satisfaction bonus occurs
        )

        val simulated = EmployeeSystem.simulateEmployeeWeeklyBehavior(
            employee = employee,
            companyRunwayWeeks = 10.0,
            salaryMultiplier = 1.0
        )

        assertEquals(11, simulated.tenureWeeks)
        assertEquals(10.4, simulated.vacationDaysAccumulated, 0.01)
        assertTrue(simulated.stress > employee.stress) // Overwork + wrong workmode increases stress
        assertTrue(simulated.satisfaction < employee.satisfaction)
    }

    @Test
    fun testSimulateEmployeeWeeklyBehaviorVacation() {
        val employee = EmployeeSystem.generateCandidate("Designer", "Senior", 1).copy(
            onVacation = true,
            vacationWeeksRemaining = 2,
            vacationDaysAccumulated = 5.0,
            tenureWeeks = 15,
            satisfaction = 60.0,
            stress = 50.0,
            health = 80.0
        )

        val simulated = EmployeeSystem.simulateEmployeeWeeklyBehavior(
            employee = employee,
            companyRunwayWeeks = 10.0,
            salaryMultiplier = 1.0
        )

        assertEquals(16, simulated.tenureWeeks)
        assertEquals(5.4, simulated.vacationDaysAccumulated, 0.01)
        assertTrue(simulated.onVacation) // Still 1 week remaining
        assertEquals(1, simulated.vacationWeeksRemaining)
        assertTrue(simulated.stress < employee.stress) // stress decreases
        assertTrue(simulated.health > employee.health) // health improves
        assertTrue(simulated.satisfaction > employee.satisfaction) // morale improves
        assertEquals(0.0, simulated.productivity, 0.01) // No work produced on vacation
    }
}
