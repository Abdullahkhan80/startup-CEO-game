package com.example.engine

import androidx.annotation.Keep
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Keep
class SimulationEngine {

    companion object {
        // Deterministic rounding helper for financial values to avoid floating point precision drifts
        fun round(value: Double, decimals: Int = 2): Double {
            if (value.isNaN() || value.isInfinite()) return 0.0
            return BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP).toDouble()
        }
    }

    /**
     * Initializes a brand new startup company from seed variables.
     */
    fun createCompany(
        companyName: String,
        ceoName: String,
        industry: SimulationIndustry,
        businessModel: BusinessModel,
        headquarters: Headquarters,
        registrationState: RegistrationState
    ): CompanyState {
        val initialFounderShares = 9_000_000L
        val initialOptionPoolShares = 1_000_000L
        val totalShares = initialFounderShares + initialOptionPoolShares

        val founder = Shareholder(
            id = UUID.randomUUID().toString(),
            name = ceoName.ifBlank { "Founder" },
            equityShares = initialFounderShares,
            ownershipPercent = round((initialFounderShares.toDouble() / totalShares) * 100.0, 4),
            isFounder = true
        )

        val optionPool = Shareholder(
            id = "employee_pool",
            name = "Employee Option Pool",
            equityShares = initialOptionPoolShares,
            ownershipPercent = round((initialOptionPoolShares.toDouble() / totalShares) * 100.0, 4),
            isFounder = false
        )

        val capTable = listOf(founder, optionPool)

        // Give initial product
        val initialProduct = SimProduct(
            name = "Beta Release",
            designQuality = 30.0,
            codeQuality = 30.0,
            bugDensity = 5.0,
            featureCompleteness = 20.0,
            launchWeek = 1,
            weeklyMarketingBudget = 50.0,
            activeUsers = businessModel.initialUserAcquisitionBoost.toLong(),
            features = listOf(
                SimFeature(name = "Core MVP Architecture", complexity = 50.0, progress = 10.0, isCompleted = false)
            )
        )

        return CompanyState(
            companyName = companyName.ifBlank { "Unicorn Inc" },
            ceoName = ceoName.ifBlank { "CEO" },
            currentWeek = 1,
            industry = industry,
            businessModel = businessModel,
            headquarters = headquarters,
            registrationState = registrationState,
            products = listOf(initialProduct),
            employees = emptyList(),
            capTable = capTable,
            totalAuthorizedShares = totalShares,
            cash = 100_000.0 - registrationState.registrationFee,
            valuation = 150_000.0,
            averageEmployeeSatisfaction = 80.0,
            newsLog = listOf("Founded $companyName in $headquarters! Registered as ${registrationState.displayName}.")
        )
    }

    /**
     * Updates company registration. Deducts filing fees and updates state.
     */
    fun registerCompany(state: CompanyState, newState: RegistrationState): CompanyState {
        if (state.cash < newState.registrationFee) {
            val failedLog = state.newsLog + "Unable to register as ${newState.displayName} due to insufficient funds."
            return state.copy(newsLog = failedLog)
        }
        val updatedCash = round(state.cash - newState.registrationFee)
        val updatedLog = state.newsLog + "Successfully registered as ${newState.displayName}. Filing fees of $${newState.registrationFee} deducted."
        return state.copy(
            registrationState = newState,
            cash = updatedCash,
            newsLog = updatedLog
        )
    }

    /**
     * Adds a newly designed product to the company's product roster.
     */
    fun createProduct(state: CompanyState, name: String, design: Double, code: Double): CompanyState {
        val newProduct = SimProduct(
            name = name,
            designQuality = design.coerceIn(1.0, 100.0),
            codeQuality = code.coerceIn(1.0, 100.0),
            bugDensity = 0.0,
            featureCompleteness = 10.0,
            launchWeek = state.currentWeek
        )
        val updatedProducts = state.products + newProduct
        val updatedLog = state.newsLog + "Launched a new product: '$name'!"
        return state.copy(products = updatedProducts, newsLog = updatedLog)
    }

    /**
     * Hires an employee to the staff roster.
     */
    fun hireEmployee(state: CompanyState, employee: SimEmployee): CompanyState {
        val updatedEmployees = state.employees + employee
        val updatedLog = state.newsLog + "Hired ${employee.name} as ${employee.skillLevel} ${employee.role} (Salary: $${employee.salary}/mo)."
        return state.copy(employees = updatedEmployees, newsLog = updatedLog)
    }

    /**
     * Fires an employee from the company staff.
     */
    fun fireEmployee(state: CompanyState, id: String): CompanyState {
        val target = state.employees.find { it.id == id } ?: return state
        val updatedEmployees = state.employees.filter { it.id != id }
        val updatedLog = state.newsLog + "Fired employee ${target.name}."
        return state.copy(employees = updatedEmployees, newsLog = updatedLog)
    }

    /**
     * Adjusts the weekly marketing spend on a specific product.
     */
    fun adjustMarketingBudget(state: CompanyState, productId: String, budget: Double): CompanyState {
        val updatedProducts = state.products.map {
            if (it.id == productId) {
                it.copy(weeklyMarketingBudget = budget.coerceAtLeast(0.0))
            } else it
        }
        return state.copy(products = updatedProducts)
    }

    /**
     * Adjusts weekly working hours of a specific employee (affects fatigue and satisfaction).
     */
    fun adjustEmployeeWorkload(state: CompanyState, employeeId: String, hours: Double): CompanyState {
        val updatedEmployees = state.employees.map {
            if (it.id == employeeId) {
                it.copy(weeklyWorkload = hours.coerceIn(10.0, 80.0))
            } else it
        }
        return state.copy(employees = updatedEmployees)
    }

    /**
     * Issues new shares representing equity funding round.
     * Cash is added to company balance sheet. Dilution occurs proportionally.
     */
    fun issueShares(state: CompanyState, investorName: String, investmentAmount: Double, impliedValuation: Double): CompanyState {
        if (investmentAmount <= 0.0 || impliedValuation <= investmentAmount) return state

        val totalShares = state.totalAuthorizedShares
        // New shares are calculated based on the investment fraction of post-money valuation
        val equityDilutionFraction = investmentAmount / impliedValuation
        val newSharesIssued = ((totalShares.toDouble() / (1.0 - equityDilutionFraction)) - totalShares).toLong()
        val updatedTotalShares = totalShares + newSharesIssued

        val existingCap = state.capTable.map { shareholder ->
            val updatedPercent = round((shareholder.equityShares.toDouble() / updatedTotalShares) * 100.0, 4)
            shareholder.copy(ownershipPercent = updatedPercent)
        }

        val investorShares = Shareholder(
            id = UUID.randomUUID().toString(),
            name = investorName,
            equityShares = newSharesIssued,
            ownershipPercent = round((newSharesIssued.toDouble() / updatedTotalShares) * 100.0, 4),
            isFounder = false
        )

        val updatedCapTable = existingCap + investorShares
        val updatedCash = round(state.cash + investmentAmount)
        val updatedLog = state.newsLog + "Closed funding round: Raised $${investmentAmount} at $${impliedValuation} valuation from $investorName."

        return state.copy(
            cash = updatedCash,
            valuation = impliedValuation,
            capTable = updatedCapTable,
            totalAuthorizedShares = updatedTotalShares,
            newsLog = updatedLog
        )
    }

    /**
     * Simulates exactly one week of core business progression.
     * All changes are calculated deterministically.
     */
    fun simulateWeek(state: CompanyState): CompanyState {
        if (state.isBankruptcy) return state

        val nextWeek = state.currentWeek + 1
        val newsLogThisWeek = mutableListOf<String>()

        // 1. Calculate Average Employee Satisfaction and updates weekly attributes
        var averageSatisfaction = 80.0
        val simulatedEmployees = mutableListOf<SimEmployee>()
        
        state.employees.forEach { emp ->
            val updatedEmp = EmployeeSystem.simulateEmployeeWeeklyBehavior(
                employee = emp,
                companyRunwayWeeks = state.financials.runwayWeeks,
                salaryMultiplier = state.headquarters.employeeSalaryMultiplier
            )
            if (updatedEmp.isResigned) {
                newsLogThisWeek.add("WEEK $nextWeek NOTICE: ${updatedEmp.name} (${updatedEmp.role}) has resigned due to low morale or high stress.")
            } else {
                simulatedEmployees.add(updatedEmp)
            }
        }

        // Apply HR boost to overall morale if HR team members are active
        val hrStaff = simulatedEmployees.filter { it.role == "HR" }
        val hrMoraleBoost = if (hrStaff.isNotEmpty()) {
            (hrStaff.sumOf { it.productivity } / 150.0).coerceAtMost(8.0)
        } else 0.0

        val finalEmployees = if (hrMoraleBoost > 0.0) {
            simulatedEmployees.map { emp ->
                if (emp.role != "HR") {
                    emp.copy(satisfaction = (emp.satisfaction + hrMoraleBoost).coerceAtMost(100.0))
                } else emp
            }
        } else {
            simulatedEmployees
        }

        if (finalEmployees.isNotEmpty()) {
            averageSatisfaction = round(finalEmployees.map { it.satisfaction }.average())
        }

        // 2. Perform Product Increments (Code Quality, Bugs, Feature completeness)
        val engineers = finalEmployees.filter { it.role == "Engineer" || it.role == "Developer" }
        val designers = finalEmployees.filter { it.role == "Designer" }
        val marketers = finalEmployees.filter { it.role == "Marketer" || it.role == "Marketing" }
        val productManagers = finalEmployees.filter { it.role == "PM" || it.role == "Product Manager" }
        val salesStaff = finalEmployees.filter { it.role == "Sales" }
        val financeStaff = finalEmployees.filter { it.role == "Finance" }
        val legalStaff = finalEmployees.filter { it.role == "Legal" }
        val executives = finalEmployees.filter { it.role == "Executive" }

        // Role productivity coefficients
        val execMultiplier = 1.0 + (executives.sumOf { it.productivity } / 400.0).coerceAtMost(0.20)
        val financeDiscount = 1.0 - (financeStaff.sumOf { it.productivity } / 400.0).coerceAtMost(0.25)
        val legalDiscount = 1.0 - (legalStaff.sumOf { it.productivity } / 500.0).coerceAtMost(0.20)
        val salesRevenueMultiplier = 1.0 + (salesStaff.sumOf { it.productivity } / 300.0).coerceAtMost(0.30)

        // Total dev and design power
        val totalEngineerPower = engineers.sumOf { it.productivity * (it.weeklyWorkload / 40.0) } * execMultiplier
        val totalDesignerPower = designers.sumOf { it.productivity * (it.weeklyWorkload / 40.0) } * execMultiplier
        val pmMultiplier = 1.0 + (productManagers.sumOf { it.productivity } / 300.0).coerceAtMost(0.5)

        val updatedProducts = state.products.map { product ->
            // Distribute engineering and design capacity evenly across active products
            val assignedDevPower = if (state.products.isNotEmpty()) totalEngineerPower / state.products.size else 0.0
            val assignedDesignerPower = if (state.products.isNotEmpty()) totalDesignerPower / state.products.size else 0.0
            
            // Process incremental feature development and technical debt accumulation
            var p = ProductSystem.processDevelopmentWork(
                product = product,
                devPower = assignedDevPower,
                pmMultiplier = pmMultiplier,
                designerPower = assignedDesignerPower
            )
            
            // User growth based on business model ARPU factors, product quality, marketing & marketing budget
            val qualityGrowthFactor = p.productQualityScore / 50.0 // 1.0 is standard at score 50
            
            // Marketing scaling: square root budget adds diminishing returns efficiency
            val marketingPower = marketers.sumOf { it.productivity } * execMultiplier
            val marketingSpendBonus = Math.sqrt(p.weeklyMarketingBudget) * (1.0 + marketingPower / 100.0) * state.businessModel.initialUserAcquisitionBoost / 1500.0

            val compoundGrowthMultiplier = state.industry.baseGrowthRate * qualityGrowthFactor
            val organicGrowth = p.activeUsers * compoundGrowthMultiplier
            val userLoss = p.activeUsers * state.businessModel.userChurnRate
            
            val netGrowth = organicGrowth - userLoss + marketingSpendBonus
            val finalUsers = (p.activeUsers + netGrowth).coerceAtLeast(0.0).toLong()

            // Product revenue: ARPU factors per business model, boosted by sales multiplier
            val weeklyArpu = (state.industry.revenuePerUser * state.businessModel.arpuFactor * salesRevenueMultiplier) / 4.0
            val revenueThisWeek = finalUsers * weeklyArpu
            val updatedLifetimeRev = p.lifetimeRevenue + revenueThisWeek

            // Process dynamic weekly software cycles (bugs, performance, security, satisfaction, roadmaps, marketShare)
            p = ProductSystem.processWeeklyProductDynamics(
                product = p,
                devPower = assignedDevPower,
                activeUsers = finalUsers,
                week = nextWeek
            )

            p.copy(
                activeUsers = finalUsers,
                lifetimeRevenue = round(updatedLifetimeRev)
            )
        }

        // 3. Financial updates (Revenue & Expenses flow)
        val rent = round(state.headquarters.rentCost * legalDiscount)
        val payroll = finalEmployees.sumOf { it.salary } / 4.0
        val baseOpEx = (state.industry.operatingExpenseMultiplier * 200.0) * financeDiscount
        val productOpEx = updatedProducts.sumOf { it.activeUsers * 0.005 } * financeDiscount // Cloud server costs
        val totalOpEx = baseOpEx + productOpEx
        
        val totalMarketingExpenses = updatedProducts.sumOf { it.weeklyMarketingBudget }
        val totalWeeklyRevenue = round(updatedProducts.sumOf { it.lifetimeRevenue - (state.products.find { p -> p.id == it.id }?.lifetimeRevenue ?: 0.0) })
        
        val totalExpenses = rent + payroll + totalOpEx + totalMarketingExpenses
        val updatedCash = round(state.cash + totalWeeklyRevenue - totalExpenses)

        // 4. Verify bankruptcy
        var isBankrupt = state.isBankruptcy
        if (updatedCash <= 0.0) {
            isBankrupt = true
            newsLogThisWeek.add("WEEK $nextWeek CRITICAL ALERT: Your company has run out of cash and is bankrupt!")
        } else {
            newsLogThisWeek.add("WEEK $nextWeek Financials: Revenue of $${round(totalWeeklyRevenue)}, Operating costs of $${round(totalExpenses)}. Cash reserves: $${updatedCash}.")
        }

        // 5. Valuation Updates (ARR run rate, multiple mechanics, headquarters networking multipliers)
        val annualizedRevenue = totalWeeklyRevenue * 52.0
        val baseIndustryMultiple = when (state.industry) {
            SimulationIndustry.SAAS -> 10.0
            SimulationIndustry.AI -> 22.0
            SimulationIndustry.BIOTECH -> 15.0
            SimulationIndustry.FINTECH -> 12.0
            SimulationIndustry.WEB3 -> 7.0
        }
        val qualityModifier = 0.5 + ((updatedProducts.map { it.productQualityScore }.average().takeIf { !it.isNaN() } ?: 15.0) / 100.0)
        val multipleWithRegistration = baseIndustryMultiple * (1.0 + state.registrationState.investorInterestMultiplier * 0.1)
        val rawValuation = (annualizedRevenue * multipleWithRegistration * qualityModifier * state.headquarters.networkingValuationBoost) + updatedProducts.sumOf { it.activeUsers * 3.0 }
        val finalValuation = round(rawValuation.coerceAtLeast(50_000.0))

        val nextState = state.copy(
            currentWeek = nextWeek,
            products = updatedProducts,
            employees = finalEmployees,
            cash = if (isBankrupt) 0.0 else updatedCash,
            lastWeekRevenue = totalWeeklyRevenue,
            totalRevenueEarned = round(state.totalRevenueEarned + totalWeeklyRevenue),
            valuation = finalValuation,
            averageEmployeeSatisfaction = averageSatisfaction,
            isBankruptcy = isBankrupt,
            newsLog = state.newsLog + newsLogThisWeek
        )

        return if (!isBankrupt) {
            InvestmentSystem.processWeeklyInvestmentDynamics(nextState)
        } else {
            nextState
        }
    }
}
