package com.example.engine

import androidx.annotation.Keep

@Keep
data class CompanyState(
    val companyName: String,
    val ceoName: String,
    val currentWeek: Int = 1,
    val industry: SimulationIndustry,
    val businessModel: BusinessModel,
    val headquarters: Headquarters,
    val registrationState: RegistrationState,
    val products: List<SimProduct> = emptyList(),
    val employees: List<SimEmployee> = emptyList(),
    val capTable: List<Shareholder> = emptyList(),
    val totalAuthorizedShares: Long = 10_000_000L,
    val cash: Double = 100_000.0,
    val debt: Double = 0.0,
    val lastWeekRevenue: Double = 0.0,
    val totalRevenueEarned: Double = 0.0,
    val valuation: Double = 150_000.0,
    val averageEmployeeSatisfaction: Double = 80.0,
    val isBankruptcy: Boolean = false,
    val newsLog: List<String> = emptyList(),

    // Advanced investment system state
    val convertibleNotes: List<SimConvertibleInstrument> = emptyList(),
    val debts: List<SimDebtRecord> = emptyList(),
    val boardMembers: List<SimBoardMember> = emptyList(),
    val activeProposals: List<SimShareholderProposal> = emptyList(),
    val fundingOffers: List<SimInvestmentOffer> = emptyList(),
    val pitchDeckQuality: Double = 50.0, // 0 to 100
    val totalInvestmentRaised: Double = 0.0,
    val isIpoExited: Boolean = false,
    val isAcquired: Boolean = false
) {
    // Calculates core financial stats deterministically using the current state and parameters
    val financials: Financials
        get() {
            val rent = headquarters.rentCost
            // Weekly payroll is monthly payroll divided by 4
            val payroll = employees.sumOf { it.salary } / 4.0
            // Operating expense is proportional to industry complexity + server expenses of products
            val baseOpEx = (industry.operatingExpenseMultiplier * 200.0)
            val productOpEx = products.sumOf { it.activeUsers * 0.005 } // Scaling infrastructure cost
            val debtRepayments = debts.sumOf { it.weeklyRepayment }
            val operatingExpenses = baseOpEx + productOpEx + debtRepayments
            
            val marketingExpenses = products.sumOf { it.weeklyMarketingBudget }
            
            return Financials(
                cash = cash,
                debt = debts.sumOf { it.remainingBalance },
                weeklyRevenue = lastWeekRevenue,
                weeklyOperatingExpenses = operatingExpenses,
                weeklyMarketingExpenses = marketingExpenses,
                weeklyPayroll = payroll,
                weeklyRent = rent,
                totalRevenueEarned = totalRevenueEarned,
                valuation = valuation
            )
        }
}
