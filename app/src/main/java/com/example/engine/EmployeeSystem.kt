package com.example.engine

import androidx.annotation.Keep
import java.util.UUID
import kotlin.math.sqrt

@Keep
object EmployeeSystem {

    private val FIRST_NAMES = listOf(
        "Alex", "Sam", "Jordan", "Taylor", "Morgan", "Casey", "Robin", "Jamie", "Drew", "Cameron",
        "Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona", "George", "Hannah", "Ian", "Julia",
        "Kevin", "Laura", "Michael", "Natalie", "Oliver", "Penelope", "Quinn", "Rachel", "Steven", "Tiffany"
    )

    private val LAST_NAMES = listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Rodriguez", "Wilson",
        "Martinez", "Anderson", "Taylor", "Thomas", "Hernandez", "Moore", "Martin", "Jackson", "Thompson", "White",
        "Lopez", "Lee", "Gonzalez", "Harris", "Clark", "Lewis", "Robinson", "Walker", "Young", "Hall"
    )

    /**
     * Generates a random candidate with attributes appropriate for their role.
     */
    fun generateCandidate(
        role: String,
        level: String,
        week: Int
    ): SimEmployee {
        val firstName = FIRST_NAMES.random()
        val lastName = LAST_NAMES.random()
        val name = "$firstName $lastName"

        val experienceYears = when (level) {
            "Junior" -> (1..3).random().toDouble() + (0..9).random() / 10.0
            "Senior" -> (4..8).random().toDouble() + (0..9).random() / 10.0
            "Lead" -> (7..12).random().toDouble() + (0..9).random() / 10.0
            "Executive" -> (10..20).random().toDouble() + (0..9).random() / 10.0
            else -> 1.0
        }

        // Skill scores appropriate to level
        val baseSkill = when (level) {
            "Junior" -> (30..55).random()
            "Senior" -> (60..80).random()
            "Lead" -> (75..90).random()
            "Executive" -> (85..100).random()
            else -> 40
        }

        // Role-based skill weights
        val technicalSkill = when (role) {
            "Developer" -> baseSkill.toDouble() + (-5..5).random()
            "Designer" -> baseSkill.toDouble() * 0.8 + (-5..5).random()
            "Finance", "Legal" -> baseSkill.toDouble() * 0.9 + (-5..5).random()
            else -> baseSkill.toDouble() * 0.6 + (-5..5).random()
        }.coerceIn(10.0, 100.0)

        val softSkill = when (role) {
            "Product Manager", "HR", "Sales", "Executive" -> baseSkill.toDouble() + (-5..5).random()
            "Marketing" -> baseSkill.toDouble() * 0.9 + (-5..5).random()
            else -> baseSkill.toDouble() * 0.7 + (-5..5).random()
        }.coerceIn(10.0, 100.0)

        // Salary expectations based on role complexity and experience
        val roleBaseSalary = when (role) {
            "Developer" -> 6000.0
            "Designer" -> 5000.0
            "Product Manager" -> 5500.0
            "Marketing" -> 4500.0
            "Sales" -> 4000.0 // higher commission potential
            "HR" -> 4500.0
            "Finance" -> 5200.0
            "Legal" -> 7000.0
            "Executive" -> 9000.0
            else -> 4500.0
        }

        val levelMultiplier = when (level) {
            "Junior" -> 0.8
            "Senior" -> 1.4
            "Lead" -> 1.9
            "Executive" -> 2.6
            else -> 1.0
        }

        val expectedSalary = SimulationEngine.round(roleBaseSalary * levelMultiplier * (1.0 + (experienceYears * 0.02)))

        return SimEmployee(
            id = UUID.randomUUID().toString(),
            name = name,
            role = role,
            skillLevel = level,
            skillScore = baseSkill.coerceIn(1, 100),
            salary = expectedSalary,
            weeklyWorkload = 40.0,
            satisfaction = (70..90).random().toDouble(),
            technicalSkill = SimulationEngine.round(technicalSkill),
            softSkill = SimulationEngine.round(softSkill),
            experienceYears = SimulationEngine.round(experienceYears, 1),
            productivity = baseSkill.toDouble(),
            stress = (10..30).random().toDouble(),
            health = (85..100).random().toDouble(),
            loyalty = (60..85).random().toDouble(),
            performanceReviews = emptyList(),
            vacationDaysAccumulated = 10.0,
            onVacation = false,
            workMode = if (listOf("Developer", "Designer").contains(role)) "Remote" else "Office",
            tenureWeeks = 0,
            isResigned = false
        )
    }

    /**
     * Generates a list of random candidates representing a recruitment pipeline.
     */
    fun generateRecruitmentPipeline(week: Int, count: Int = 5): List<SimEmployee> {
        val roles = listOf(
            "Developer", "Designer", "Product Manager", "Marketing",
            "Sales", "HR", "Finance", "Legal", "Executive"
        )
        val levels = listOf("Junior", "Senior", "Lead", "Executive")
        
        return List(count) {
            val role = roles.random()
            // Executives are rare
            val level = if (role == "Executive") "Executive" else {
                val rand = (1..100).random()
                when {
                    rand < 50 -> "Junior"
                    rand < 85 -> "Senior"
                    else -> "Lead"
                }
            }
            generateCandidate(role, level, week)
        }
    }

    /**
     * Simulates screening a candidate. Reveals precise skills (within a range).
     */
    fun screenCandidate(candidate: SimEmployee): SimEmployee {
        // Simulates finding out extra details or refining their skill accuracy
        val accuracyAdjustment = (-3..3).random()
        val updatedSkillScore = (candidate.skillScore + accuracyAdjustment).coerceIn(1, 100)
        return candidate.copy(
            skillScore = updatedSkillScore,
            satisfaction = (candidate.satisfaction + (-5..5).random()).coerceIn(0.0, 100.0)
        )
    }

    /**
     * Train an employee to improve skills.
     * Costs cash and takes action.
     */
    fun trainEmployee(
        employee: SimEmployee,
        trainingFocus: String // "Technical", "SoftSkills", "General"
    ): SimEmployee {
        val skillIncrease = when (trainingFocus) {
            "Technical" -> (3..8).random().toDouble()
            "SoftSkills" -> (3..8).random().toDouble()
            else -> (2..5).random().toDouble()
        }

        val updatedTech = if (trainingFocus == "Technical" || trainingFocus == "General") {
            (employee.technicalSkill + skillIncrease).coerceAtMost(100.0)
        } else employee.technicalSkill

        val updatedSoft = if (trainingFocus == "SoftSkills" || trainingFocus == "General") {
            (employee.softSkill + skillIncrease).coerceAtMost(100.0)
        } else employee.softSkill

        val updatedOverall = ((updatedTech + updatedSoft) / 2.0).toInt().coerceIn(1, 100)

        // Training also improves morale/satisfaction but slightly increases stress of learning
        val updatedSatisfaction = (employee.satisfaction + 10.0).coerceAtMost(100.0)
        val updatedStress = (employee.stress + 5.0).coerceAtMost(100.0)

        return employee.copy(
            technicalSkill = SimulationEngine.round(updatedTech),
            softSkill = SimulationEngine.round(updatedSoft),
            skillScore = updatedOverall,
            satisfaction = SimulationEngine.round(updatedSatisfaction),
            stress = SimulationEngine.round(updatedStress)
        )
    }

    /**
     * Sends an employee on vacation.
     */
    fun sendOnVacation(employee: SimEmployee, weeks: Int): SimEmployee {
        val daysRequired = weeks * 5.0
        if (employee.vacationDaysAccumulated < daysRequired) {
            return employee // Insufficient vacation days
        }
        return employee.copy(
            onVacation = true,
            vacationWeeksRemaining = weeks,
            vacationDaysAccumulated = SimulationEngine.round(employee.vacationDaysAccumulated - daysRequired)
        )
    }

    /**
     * Promotes an employee. Boosts levels, morale, and increases loyalty and salary.
     */
    fun promoteEmployee(
        employee: SimEmployee,
        newLevel: String,
        salaryIncreasePercent: Double
    ): SimEmployee {
        val updatedSalary = SimulationEngine.round(employee.salary * (1.0 + salaryIncreasePercent / 100.0))
        val updatedSatisfaction = (employee.satisfaction + 20.0).coerceAtMost(100.0)
        val updatedLoyalty = (employee.loyalty + 15.0).coerceAtMost(100.0)
        val updatedStress = (employee.stress - 5.0).coerceAtLeast(0.0)

        return employee.copy(
            skillLevel = newLevel,
            salary = updatedSalary,
            satisfaction = SimulationEngine.round(updatedSatisfaction),
            loyalty = SimulationEngine.round(updatedLoyalty),
            stress = SimulationEngine.round(updatedStress)
        )
    }

    /**
     * Configures the employee work mode.
     */
    fun setWorkMode(employee: SimEmployee, mode: String): SimEmployee {
        if (mode != "Office" && mode != "Remote") return employee
        return employee.copy(workMode = mode)
    }

    /**
     * Records a new performance review for the employee.
     */
    fun conductPerformanceReview(
        employee: SimEmployee,
        week: Int,
        rating: Double,
        feedback: String
    ): SimEmployee {
        val review = SimPerformanceReview(week, rating.coerceIn(1.0, 5.0), feedback)
        val updatedReviews = employee.performanceReviews + review
        
        // Morale and loyalty shifts depending on rating
        val moraleDelta = when {
            rating >= 4.0 -> 8.0
            rating <= 2.0 -> -15.0
            else -> 2.0
        }
        val stressDelta = when {
            rating >= 4.0 -> -5.0
            rating <= 2.0 -> 15.0
            else -> 0.0
        }

        return employee.copy(
            performanceReviews = updatedReviews,
            satisfaction = (employee.satisfaction + moraleDelta).coerceIn(0.0, 100.0),
            stress = (employee.stress + stressDelta).coerceIn(0.0, 100.0)
        )
    }

    /**
     * Simulates the weekly autonomous behavior of an individual employee.
     */
    fun simulateEmployeeWeeklyBehavior(
        employee: SimEmployee,
        companyRunwayWeeks: Double,
        salaryMultiplier: Double
    ): SimEmployee {
        val tenureWeeks = employee.tenureWeeks + 1
        val vacationDaysAccumulated = (employee.vacationDaysAccumulated + 0.4).coerceAtMost(30.0)

        if (employee.onVacation) {
            val remainingWeeks = employee.vacationWeeksRemaining - 1
            val isVacationOver = remainingWeeks <= 0
            
            val updatedStress = (employee.stress - 20.0).coerceAtLeast(0.0)
            val updatedHealth = (employee.health + 10.0).coerceAtMost(100.0)
            val updatedSatisfaction = (employee.satisfaction + 10.0).coerceAtMost(100.0)

            return employee.copy(
                tenureWeeks = tenureWeeks,
                vacationDaysAccumulated = SimulationEngine.round(vacationDaysAccumulated),
                onVacation = !isVacationOver,
                vacationWeeksRemaining = remainingWeeks,
                stress = SimulationEngine.round(updatedStress),
                health = SimulationEngine.round(updatedHealth),
                satisfaction = SimulationEngine.round(updatedSatisfaction),
                productivity = 0.0
            )
        }

        // Active employee behavior
        var stress = employee.stress
        var health = employee.health
        var satisfaction = employee.satisfaction
        var loyalty = employee.loyalty

        // 1. Workload impact on stress
        val workloadStress = if (employee.weeklyWorkload > 40.0) {
            (employee.weeklyWorkload - 40.0) * 1.5
        } else {
            -0.5
        }
        stress = (stress + workloadStress).coerceIn(0.0, 100.0)

        // 2. Role-specific Work Mode Preference impact
        val developerOrDesigner = employee.role == "Developer" || employee.role == "Designer"
        val executiveOrSalesOrHR = employee.role == "Executive" || employee.role == "Sales" || employee.role == "HR"

        if (developerOrDesigner && employee.workMode == "Office") {
            stress = (stress + 1.2).coerceAtMost(100.0)
            satisfaction = (satisfaction - 0.8).coerceAtLeast(0.0)
        } else if (executiveOrSalesOrHR && employee.workMode == "Remote") {
            stress = (stress + 0.8).coerceAtMost(100.0)
            satisfaction = (satisfaction - 0.5).coerceAtLeast(0.0)
        }

        // 3. Stress impact on Health
        if (stress > 70.0) {
            health = (health - (stress - 70.0) * 0.15).coerceIn(0.0, 100.0)
        } else if (stress < 30.0) {
            health = (health + 0.5).coerceAtMost(100.0)
        }

        // 4. Financial runway impact on Satisfaction
        if (companyRunwayWeeks < 4.0) {
            satisfaction = (satisfaction - 4.5).coerceAtLeast(0.0)
            stress = (stress + 3.0).coerceAtMost(100.0)
        }

        // 5. Compensation fairness impact
        val baseLevelMultiplier = when (employee.skillLevel) {
            "Junior" -> 0.7
            "Senior" -> 1.3
            "Lead" -> 1.8
            "Executive" -> 2.5
            else -> 1.0
        }
        val benchmarkSalary = 5000.0 * salaryMultiplier * baseLevelMultiplier
        val salaryRatio = employee.salary / benchmarkSalary

        if (salaryRatio < 0.9) {
            satisfaction = (satisfaction - 3.0).coerceAtLeast(0.0)
            loyalty = (loyalty - 1.5).coerceAtLeast(0.0)
        } else if (salaryRatio > 1.2) {
            satisfaction = (satisfaction + 2.0).coerceAtMost(100.0)
            loyalty = (loyalty + 1.0).coerceAtMost(100.0)
        }

        // 6. Overall Satisfaction decay from high stress
        satisfaction = (satisfaction - (stress * 0.04)).coerceIn(0.0, 100.0)

        // 7. Loyalty alignment with Satisfaction
        if (satisfaction > 75.0) {
            loyalty = (loyalty + 0.6).coerceAtMost(100.0)
        } else if (satisfaction < 40.0) {
            loyalty = (loyalty - 1.2).coerceAtLeast(0.0)
        }

        // 8. Resignation evaluation
        // If morale/satisfaction is extremely low (< 15) or loyalty is extremely low (< 10), they resign.
        // Or if stress is high and satisfaction is low, there's a risk.
        val turnoverRisk = (100.0 - satisfaction) * 0.4 + stress * 0.3 + (100.0 - loyalty) * 0.3
        val isResigned = satisfaction < 15.0 || loyalty < 10.0 || (turnoverRisk > 75.0 && (1..100).random() < 8)

        // 9. Productivity calculation
        val roleBonus = if (employee.role == "Executive") 1.15 else 1.0
        val baseProductivity = (employee.technicalSkill * 0.5 + employee.softSkill * 0.5) * roleBonus
        val satisfactionFactor = satisfaction / 100.0
        val healthFactor = health / 100.0
        val stressFactor = 1.0 - (stress / 250.0)
        val productivity = (baseProductivity * satisfactionFactor * healthFactor * stressFactor).coerceIn(5.0, 100.0)

        return employee.copy(
            tenureWeeks = tenureWeeks,
            vacationDaysAccumulated = SimulationEngine.round(vacationDaysAccumulated),
            stress = SimulationEngine.round(stress),
            health = SimulationEngine.round(health),
            satisfaction = SimulationEngine.round(satisfaction),
            loyalty = SimulationEngine.round(loyalty),
            productivity = SimulationEngine.round(productivity),
            isResigned = isResigned
        )
    }
}
