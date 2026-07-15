package com.example.engine

import androidx.annotation.Keep
import java.util.UUID

@Keep
enum class SimulationIndustry(
    val displayName: String,
    val revenuePerUser: Double,
    val baseGrowthRate: Double,
    val operatingExpenseMultiplier: Double,
    val complexity: Double // 0.0 (simple) to 1.0 (highly complex)
) {
    SAAS("SaaS (Cloud Software)", 6.5, 0.12, 1.0, 0.3),
    AI("Artificial Intelligence", 14.0, 0.30, 2.2, 0.8),
    BIOTECH("BioTech & Health", 18.0, 0.15, 3.0, 0.95),
    FINTECH("FinTech (Payments)", 9.0, 0.20, 1.8, 0.65),
    WEB3("Web3 & Crypto", 3.0, 0.35, 1.5, 0.5)
}

@Keep
enum class BusinessModel(
    val displayName: String,
    val arpuFactor: Double, // Revenue scaling factor
    val userChurnRate: Double, // Churn per week (e.g. 0.02 for 2%)
    val conversionRate: Double, // Premium user conversion rate
    val initialUserAcquisitionBoost: Double
) {
    SUBSCRIPTION("Subscription / SaaS", 1.0, 0.015, 0.10, 500.0),
    TRANSACTIONAL("Transactional Fee", 0.4, 0.05, 0.25, 2000.0),
    AD_SUPPORTED("Ad-Supported", 0.05, 0.08, 1.0, 15000.0),
    FREEMIUM("Freemium model", 0.25, 0.03, 0.05, 8000.0),
    ENTERPRISE("Enterprise Licensing", 25.0, 0.002, 0.01, 50.0)
}

@Keep
enum class Headquarters(
    val displayName: String,
    val rentCost: Double, // Weekly flat operating cost
    val employeeSalaryMultiplier: Double, // Wage adjustments
    val talentPoolQualityBonus: Double, // Added to employee contribution
    val networkingValuationBoost: Double // Multiplier on valuation
) {
    SILICON_VALLEY("Silicon Valley", 2000.0, 1.5, 15.0, 1.3),
    SEATTLE("Seattle Hub", 1400.0, 1.25, 10.0, 1.15),
    AUSTIN("Austin Tech District", 900.0, 1.0, 5.0, 1.05),
    LONDON("London Square Mile", 1500.0, 1.2, 8.0, 1.1),
    REMOTE("Fully Distributed / Remote", 150.0, 0.85, 0.0, 0.95)
}

@Keep
enum class RegistrationState(
    val displayName: String,
    val registrationFee: Double,
    val annualTaxRate: Double,
    val investorInterestMultiplier: Double
) {
    UNREGISTERED("Unregistered / Sole Proprietorship", 0.0, 0.35, 0.1),
    LLC("Limited Liability Company (LLC)", 500.0, 0.25, 0.6),
    DELAWARE_C_CORP("Delaware C-Corporation", 2000.0, 0.21, 1.5),
    S_CORP("S-Corporation", 1000.0, 0.20, 0.8)
}

@Keep
enum class ProductCategory(
    val displayName: String,
    val baseComplexity: Double, // 0.0 to 100.0
    val baseMaintenanceFactor: Double, // percentage of development cost weekly
    val infrastructureMultiplier: Double, // affects scaling costs
    val baseSecuritySensitivity: Double // higher means more critical
) {
    AI_PRODUCT("AI Assistant & Agents", 80.0, 0.02, 3.5, 0.8),
    SAAS("SaaS Workspace B2B", 50.0, 0.012, 1.2, 0.75),
    CRM("Enterprise CRM Suite", 65.0, 0.015, 1.5, 0.85),
    ERP("ERP Resource Planning", 85.0, 0.025, 2.0, 0.95),
    SOCIAL_NETWORK("Social Network", 55.0, 0.01, 2.5, 0.7),
    MESSAGING_APP("Secure Messaging App", 45.0, 0.008, 1.8, 0.9),
    SEARCH_ENGINE("Semantic Search Engine", 75.0, 0.018, 4.0, 0.75),
    CLOUD_SERVICE("Cloud Platform / PaaS", 90.0, 0.03, 5.0, 0.98),
    DEVELOPER_TOOL("DevTools & IDE Plugins", 40.0, 0.005, 0.5, 0.6),
    MOBILE_APP("Consumer Mobile App", 30.0, 0.006, 0.8, 0.5),
    GAME("Immersive Multiplayer Game", 60.0, 0.01, 2.2, 0.4)
}

@Keep
data class SimFeature(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val complexity: Double, // 0 to 100
    val progress: Double = 0.0, // 0 to 100
    val isCompleted: Boolean = false,
    val technicalDebtAccrued: Double = 0.0
)

@Keep
data class SimLaunchEvent(
    val week: Int,
    val title: String,
    val description: String,
    val customerSatisfactionShift: Double,
    val popularityBoost: Double
)

@Keep
data class SimRoadmapItem(
    val id: String = UUID.randomUUID().toString(),
    val objective: String,
    val targetWeek: Int,
    val isCompleted: Boolean = false
)

@Keep
data class SimVersionUpdate(
    val version: String,
    val week: Int,
    val notes: String,
    val stabilityShift: Double,
    val customerSatisfactionShift: Double
)

@Keep
data class SimProduct(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val designQuality: Double,      // 0.0 to 100.0
    val codeQuality: Double,        // 0.0 to 100.0
    val bugDensity: Double,         // 0.0 to 100.0 (lower is better)
    val featureCompleteness: Double, // 0.0 to 100.0
    val launchWeek: Int,
    val weeklyMarketingBudget: Double = 0.0,
    val activeUsers: Long = 0,
    val lifetimeRevenue: Double = 0.0,

    // Advanced attributes requested
    val category: ProductCategory = ProductCategory.SAAS,
    val features: List<SimFeature> = emptyList(),
    val developmentCost: Double = 0.0,
    val maintenanceCost: Double = 0.0,
    val bugCount: Int = 0,
    val performance: Double = 100.0, // 0.0 to 100.0
    val security: Double = 100.0,    // 0.0 to 100.0
    val popularity: Double = 10.0,   // 0.0 to 100.0
    val marketShare: Double = 0.0,   // percentage 0.0 to 100.0
    val customerSatisfaction: Double = 80.0, // 0.0 to 100.0
    val technicalDebt: Double = 0.0,  // 0.0 to 100.0
    val launchEvents: List<SimLaunchEvent> = emptyList(),
    val roadmap: List<SimRoadmapItem> = emptyList(),
    val versionUpdates: List<SimVersionUpdate> = emptyList(),
    val currentVersion: String = "v1.0.0",
    val isReleased: Boolean = false
) {
    // Computes overall product quality as a deterministic metric
    val productQualityScore: Double
        get() = ((designQuality * 0.2) + (codeQuality * 0.3) + (featureCompleteness * 0.3) + (performance * 0.1) + (security * 0.1) - (technicalDebt * 0.15) - (bugDensity * 0.15))
            .coerceIn(1.0, 100.0)
}

@Keep
data class SimPerformanceReview(
    val week: Int,
    val rating: Double, // 1.0 to 5.0
    val feedback: String
)

@Keep
data class SimEmployee(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String, // "Developer", "Designer", "Product Manager", "Marketing", "Sales", "HR", "Finance", "Legal", "Executive"
    val skillLevel: String, // "Junior", "Senior", "Lead", "Executive"
    val skillScore: Int, // 1 to 100 overall
    val salary: Double, // Monthly base salary
    val weeklyWorkload: Double = 40.0, // Hours worked per week
    val satisfaction: Double = 80.0, // Satisfaction/Morale (0.0 to 100.0)
    
    // Expanded advanced properties
    val technicalSkill: Double = 50.0, // 0.0 to 100.0
    val softSkill: Double = 50.0,      // 0.0 to 100.0
    val experienceYears: Double = 2.0,
    val productivity: Double = 80.0,   // 0.0 to 100.0
    val stress: Double = 20.0,         // 0.0 to 100.0
    val health: Double = 95.0,         // 0.0 to 100.0
    val loyalty: Double = 75.0,        // 0.0 to 100.0
    val performanceReviews: List<SimPerformanceReview> = emptyList(),
    val vacationDaysAccumulated: Double = 15.0,
    val onVacation: Boolean = false,
    val vacationWeeksRemaining: Int = 0,
    val workMode: String = "Office",   // "Office" or "Remote"
    val tenureWeeks: Int = 0,
    val isResigned: Boolean = false
)

@Keep
data class Shareholder(
    val id: String,
    val name: String,
    val equityShares: Long,
    val ownershipPercent: Double, // Percentage from 0.0 to 100.0
    val isFounder: Boolean
)

@Keep
enum class InvestmentType(val displayName: String) {
    SAFE_AGREEMENT("SAFE Agreement"),
    CONVERTIBLE_NOTE("Convertible Note"),
    ANGEL_ROUND("Angel Round"),
    SEED_ROUND("Seed Round"),
    SERIES_A("Series A"),
    SERIES_B("Series B"),
    SERIES_C("Series C"),
    DEBT_FINANCING("Debt Financing"),
    IPO("Initial Public Offering (IPO)"),
    ACQUISITION("Acquisition Buyout")
}

@Keep
data class SimInvestmentOffer(
    val id: String = UUID.randomUUID().toString(),
    val investorName: String,
    val type: InvestmentType,
    val amount: Double,
    val valuationCap: Double? = null, // For SAFE / Convertible Note
    val discountRate: Double? = null,  // For SAFE / Convertible Note, e.g. 0.20 for 20%
    val interestRate: Double? = null,  // For Convertible Note / Debt
    val impliedValuation: Double? = null, // Forpriced rounds (post-money)
    val termWeeks: Int? = null,         // For Debt Financing
    val boardSeatRequired: Boolean = false,
    val supportiveness: Double = 50.0, // 0.0 to 100.0
    val expirationWeek: Int
)

@Keep
data class SimConvertibleInstrument(
    val id: String = UUID.randomUUID().toString(),
    val investorName: String,
    val type: InvestmentType, // SAFE_AGREEMENT or CONVERTIBLE_NOTE
    val principalAmount: Double,
    val valuationCap: Double,
    val discountRate: Double,
    val interestRate: Double = 0.0,
    val accruedInterest: Double = 0.0,
    val issuanceWeek: Int
)

@Keep
data class SimDebtRecord(
    val id: String = UUID.randomUUID().toString(),
    val lenderName: String,
    val principal: Double,
    val remainingBalance: Double,
    val annualInterestRate: Double,
    val weeklyRepayment: Double,
    val termWeeks: Int,
    val weeksRemaining: Int
)

@Keep
data class SimBoardMember(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val representing: String, // "Founder", "Angel", "VC_SEED", "VC_A", "VC_B", "VC_C", "Independent"
    val votingPower: Double,   // percentage e.g. 20.0
    val supportiveness: Double, // 0.0 to 100.0 (affects strategic alignment and voting)
    val alignmentFactor: Double = 80.0 // 0.0 to 100.0
)

@Keep
data class SimShareholderProposal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val proposalType: String, // "ACQUISITION_OFFER", "IPO_APPROVAL", "PIVOT_INDUSTRY", "REPLACE_CEO"
    val proposedWeek: Int,
    val votesForPercent: Double = 0.0,
    val votesAgainstPercent: Double = 0.0,
    val isPassed: Boolean = false,
    val isResolved: Boolean = false,
    val resolutionNote: String = ""
)

@Keep
data class Financials(
    val cash: Double,
    val debt: Double,
    val weeklyRevenue: Double,
    val weeklyOperatingExpenses: Double,
    val weeklyMarketingExpenses: Double,
    val weeklyPayroll: Double,
    val weeklyRent: Double,
    val totalRevenueEarned: Double,
    val valuation: Double
) {
    val weeklyNetProfit: Double
        get() = weeklyRevenue - (weeklyOperatingExpenses + weeklyMarketingExpenses + weeklyPayroll + weeklyRent)

    val burnRate: Double
        get() = if (weeklyNetProfit < 0) -weeklyNetProfit else 0.0

    val runwayWeeks: Double
        get() = if (burnRate > 0) cash / burnRate else Double.POSITIVE_INFINITY
}
