package com.example

import androidx.annotation.Keep
import java.util.UUID

@Keep
enum class CEOStage(val displayName: String, val minValuation: Double, val maxValuation: Double, val description: String) {
    SOLO_FOUNDER("Solo Founder", 0.0, 250_000.0, "Working from your bedroom. Building a prototype."),
    SEED_STAGE("Seed Stage Founder", 250_000.0, 2_000_000.0, "Venture backed! Working from a shared co-working space."),
    SERIES_A("Series A Founder", 2_000_000.0, 15_000_000.0, "Scaling the team. Standard Silicon Valley office."),
    SERIES_B("Series B Founder", 15_000_000.0, 80_000_000.0, "Expanding market presence. Multi-floor downtown office."),
    SERIES_C("Series C Founder", 80_000_000.0, 500_000_000.0, "Going global. Designing custom hardware and AI chips."),
    TECH_TITAN("Tech Titan", 500_000_000.0, 10_000_000_000.0, "Unicorn status. Preparing for massive Nasdaq IPO exit!")
}

@Keep
enum class Sector(val displayName: String, val revenuePerUser: Double, val baseGrowth: Double, val techDifficulty: Int) {
    SAAS("SaaS (Cloud Software)", 5.0, 0.15, 30),
    AI("Artificial Intelligence", 12.0, 0.35, 80),
    BIOTECH("BioTech & Health", 15.0, 0.20, 90),
    FINTECH("FinTech (Payments)", 8.0, 0.25, 60),
    WEB3("Web3 & Crypto", 2.0, 0.40, 50)
}

@Keep
data class Employee(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String, // "Engineer", "Marketer", "PM"
    val skillLevel: String, // "Junior", "Senior", "Lead"
    val skillScore: Int, // 1 to 100
    val salary: Double // Monthly Burn cost
)

@Keep
data class MarketRegion(
    val name: String,
    val isUnlocked: Boolean,
    val unlockCost: Double,
    val userCapacity: Long,
    val userAcquisitionMultiplier: Double
)

@Keep
data class NewsFeedItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val headline: String,
    val source: String, // "TechCrunch", "Board", "System", "Internal"
    val isAlert: Boolean = false
)

@Keep
data class EventDecision(
    val id: String,
    val title: String,
    val description: String,
    val optionAText: String,
    val optionBText: String,
    val optionAOutcome: String,
    val optionBOutcome: String,
    // Numerical effects for option A
    val cashEffectA: Double = 0.0,
    val valuationEffectA: Double = 0.0,
    val usersEffectA: Long = 0,
    val qualityEffectA: Double = 0.0,
    val burnEffectA: Double = 0.0,
    // Numerical effects for option B
    val cashEffectB: Double = 0.0,
    val valuationEffectB: Double = 0.0,
    val usersEffectB: Long = 0,
    val qualityEffectB: Double = 0.0,
    val burnEffectB: Double = 0.0,
    // Optional random success/fail check for B
    val riskFactorB: Float = 0f, // 0.0 to 1.0
    val riskEffectCashB: Double = 0.0,
    val riskEffectQualityB: Double = 0.0,
    val riskSuccessTextB: String = "",
    val riskFailTextB: String = ""
)

@Keep
data class CompetitorCompany(
    val id: String,
    val name: String,
    val description: String,
    val marketShare: Double, // 0 to 100
    val valuation: Double,
    val productQuality: Double,
    val products: List<String> = emptyList()
)

@Keep
data class MarketTrend(
    val id: String,
    val name: String,
    val description: String,
    val trendType: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val durationWeeks: Int,
    val sectorImpact: String // Sector.displayName or "All"
)

@Keep
enum class MarketEventType {
    ECONOMIC_RECESSION,
    GOVERNMENT_REGULATION,
    TECH_BREAKTHROUGH,
    CYBER_ATTACK,
    ACQUISITION,
    LAWSUIT,
    TALENT_SHORTAGE,
    MARKET_CRASH,
    CONSUMER_BEHAVIOR,
    BREAKING_NEWS
}

@Keep
data class MarketEvent(
    val id: String,
    val title: String,
    val description: String,
    val type: MarketEventType,
    val severity: String, // "INFO", "WARNING", "CRITICAL"
    val effectDurationWeeks: Int,
    val cashMultiplier: Double = 1.0,
    val userGrowthMultiplier: Double = 1.0,
    val valuationMultiplier: Double = 1.0,
    val salaryMultiplier: Double = 1.0,
    val headline: String
)

@Keep
data class GameAchievement(
    val id: String,
    val title: String,
    val description: String,
    val targetType: String, // "REVENUE", "LEVEL", "EMPLOYEES", "QUALITY", "DECISIONS"
    val targetValue: Double,
    val xpReward: Long,
    val tokenReward: Int,
    val cosmeticRewardId: String? = null
)

@Keep
data class CosmeticItem(
    val id: String,
    val name: String,
    val category: String, // "DESK", "CHAIR", "PLANT", "WALL_ART"
    val rarity: String, // "COMMON", "RARE", "LEGENDARY"
    val tokenCost: Int,
    val isPremiumOnly: Boolean = false
)

@Keep
data class FounderPassTier(
    val tier: Int,
    val xpRequired: Long = 100L,
    val freeRewardName: String,
    val freeRewardId: String?,
    val premiumRewardName: String,
    val premiumRewardId: String?
)


