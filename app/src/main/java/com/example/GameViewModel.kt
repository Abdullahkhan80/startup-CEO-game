package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random
import com.example.engine.*

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = GamePrefs(application)
    private val _uiState = MutableStateFlow(prefs.loadGameState())
    val uiState: StateFlow<GamePrefs.GameState> = _uiState.asStateFlow()

    // Pools for hiring candidates (regenerated weekly)
    private val _candidates = MutableStateFlow<List<Employee>>(emptyList())
    val candidates: StateFlow<List<Employee>> = _candidates.asStateFlow()

    // Available VC rounds based on stage
    private val _fundingOffers = MutableStateFlow<List<FundingOffer>>(emptyList())
    val fundingOffers: StateFlow<List<FundingOffer>> = _fundingOffers.asStateFlow()

    data class FundingOffer(
        val roundName: String,
        val amount: Double,
        val equityDilution: Double, // e.g., 0.15 for 15%
        val impliedValuation: Double,
        val investorName: String
    )

    init {
        generateCandidates()
        generateFundingOffers()
    }

    fun resetGame(ceoName: String, companyName: String, sector: Sector) {
        val initialCompetitors = AIPoweredMarketEngine.generateInitialCompetitors(sector)
        val initialTrend = MarketTrend(
            id = java.util.UUID.randomUUID().toString(),
            name = "Sustained Capital Growth",
            description = "High venture capital interest keeps liquidity fluid across the board.",
            trendType = "BULLISH",
            durationWeeks = 8,
            sectorImpact = sector.displayName
        )
        val newState = GamePrefs.GameState(
            ceoName = ceoName.ifBlank { "Sarah Jenkins" },
            companyName = companyName.ifBlank { "NeuraLinker" },
            sector = sector,
            valuation = 150_000.0,
            cash = 60_000.0,
            productQuality = 15.0,
            newsFeed = listOf(
                NewsFeedItem(
                    timestamp = "Week 1",
                    headline = "Founded $companyName! Set off to build a Silicon Valley titan in ${sector.displayName}.",
                    source = "System"
                )
            ),
            competitors = initialCompetitors,
            marketTrends = listOf(initialTrend),
            marketEvents = emptyList()
        )
        val companyState = newState.toCompanyState()
        val initialOffers = com.example.engine.InvestmentSystem.generateFundingOffers(companyState)
        val finalNewState = newState.copy(simFundingOffers = initialOffers, capTable = companyState.capTable)
        _uiState.value = finalNewState
        prefs.saveGameState(finalNewState)
        generateCandidates()
        generateFundingOffers()
    }

    private fun generateCandidates() {
        val firstNames = listOf("Alex", "David", "Sophia", "Marcus", "Emily", "Ryan", "Elena", "Leo", "Sasha", "Nikola", "Ada", "Grace", "Liam", "Olivia")
        val lastNames = listOf("Rivera", "Chen", "Patel", "Vance", "Stone", "Kim", "Horowitz", "Tesla", "Lovelace", "Hopper", "Zuckerberg", "Musk", "Altman")
        val roles = listOf("Engineer", "Marketer", "PM")
        val skillLevels = listOf("Junior", "Senior", "Lead")

        val newCandidates = (1..3).map {
            val role = roles.random()
            val skill = skillLevels.random()
            val score = when (skill) {
                "Junior" -> Random.nextInt(30, 55)
                "Senior" -> Random.nextInt(60, 85)
                else -> Random.nextInt(85, 100)
            }
            val salary = when (role) {
                "Engineer" -> score * 180.0
                "Marketer" -> score * 120.0
                else -> score * 150.0 // PM
            }
            Employee(
                name = "${firstNames.random()} ${lastNames.random()}",
                role = role,
                skillLevel = skill,
                skillScore = score,
                salary = salary
            )
        }
        _candidates.value = newCandidates
    }

    fun generateFundingOffers() {
        val currentStage = _uiState.value.ceoStage
        val valuation = _uiState.value.valuation

        val offers = when (currentStage) {
            CEOStage.SOLO_FOUNDER -> listOf(
                FundingOffer("Friends & Family", 25_000.0, 0.05, 500_000.0, "Uncle Richard"),
                FundingOffer("Angel Round", 60_000.0, 0.10, 600_000.0, "Naval Ravikant (Angel)")
            )
            CEOStage.SEED_STAGE -> listOf(
                FundingOffer("Seed Round", 250_000.0, 0.15, 1_666_666.0, "Y Combinator"),
                FundingOffer("Pre-A Extension", 400_000.0, 0.18, 2_222_222.0, "First Round Capital")
            )
            CEOStage.SERIES_A -> listOf(
                FundingOffer("Series A VC", 2_000_000.0, 0.15, 13_333_333.0, "Sequoia Capital"),
                FundingOffer("Strategic A Round", 2_500_000.0, 0.18, 13_888_888.0, "Andreessen Horowitz")
            )
            CEOStage.SERIES_B -> listOf(
                FundingOffer("Series B Megaround", 10_000_000.0, 0.12, 83_333_333.0, "Benchmark Capital"),
                FundingOffer("Corporate Strategic", 12_000_000.0, 0.15, 80_000_000.0, "Google Ventures")
            )
            CEOStage.SERIES_C -> listOf(
                FundingOffer("Series C Growth", 40_000_000.0, 0.10, 400_000_000.0, "Tiger Global"),
                FundingOffer("Sovereign Round", 60_000_000.0, 0.12, 500_000_000.0, "SoftBank Vision Fund")
            )
            CEOStage.TECH_TITAN -> listOf(
                FundingOffer("Pre-IPO Mezzanine", 150_000_000.0, 0.08, 1_875_000_000.0, "Fidelity Growth"),
                FundingOffer("Private Equity", 200_000_000.0, 0.10, 2_000_000_000.0, "BlackRock")
            )
        }
        _fundingOffers.value = offers.filter { it.impliedValuation >= valuation * 0.7 }
    }

    fun hireEmployee(candidate: Employee) {
        _uiState.update { state ->
            val updatedEmployees = state.employees + candidate
            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "Hired ${candidate.name} as ${candidate.skillLevel} ${candidate.role} (Skill: ${candidate.skillScore})!",
                source = "Internal"
            )
            state.copy(
                employees = updatedEmployees,
                newsFeed = updatedNews.takeLast(40)
            )
        }
        _candidates.update { list -> list.filter { it.id != candidate.id } }
        prefs.saveGameState(_uiState.value)
    }

    fun fireEmployee(employee: Employee) {
        _uiState.update { state ->
            val updatedEmployees = state.employees.filter { it.id != employee.id }
            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "Terminated employment contract for ${employee.name}.",
                source = "Internal",
                isAlert = true
            )
            state.copy(
                employees = updatedEmployees,
                newsFeed = updatedNews.takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun acceptFundingOffer(offer: FundingOffer) {
        _uiState.update { state ->
            val updatedCash = state.cash + offer.amount
            val updatedDilution = state.totalVCDilution + (offer.equityDilution * 100)
            val updatedInvestment = state.totalInvestmentRaised + offer.amount
            val baseValuation = offer.impliedValuation
            
            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "Closed ${offer.roundName} funding round! Secured ${formatCurrency(offer.amount)} from ${offer.investorName} at ${formatCurrency(baseValuation)} valuation.",
                source = "Board"
            )
            
            state.copy(
                cash = updatedCash,
                valuation = baseValuation,
                totalVCDilution = updatedDilution,
                totalInvestmentRaised = updatedInvestment,
                newsFeed = updatedNews.takeLast(40)
            )
        }
        generateFundingOffers()
        prefs.saveGameState(_uiState.value)
    }

    fun pivotSector(newSector: Sector) {
        _uiState.update { state ->
            val cost = 15_000.0
            if (state.cash < cost) return@update state // Insufficient cash

            val updatedCash = state.cash - cost
            val updatedQuality = (state.productQuality * 0.75).coerceAtLeast(5.0) // Reset quality partially
            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "Pivoted main business product to ${newSector.displayName}. Incurred ${formatCurrency(cost)} restructuring cost.",
                source = "Board"
            )

            state.copy(
                sector = newSector,
                cash = updatedCash,
                productQuality = updatedQuality,
                newsFeed = updatedNews.takeLast(40)
            )
        }
        generateFundingOffers()
        prefs.saveGameState(_uiState.value)
    }

    fun unlockRegion(regionName: String, cost: Double) {
        _uiState.update { state ->
            if (state.cash < cost) return@update state
            val updatedRegions = state.unlockedRegions + regionName
            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "Expanded operations to $regionName! Unlocked local market access.",
                source = "System"
            )
            state.copy(
                cash = state.cash - cost,
                unlockedRegions = updatedRegions,
                newsFeed = updatedNews.takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun makeDecision(optionA: Boolean) {
        val activeEvent = activeEventDecision ?: return

        _uiState.update { state ->
            val cashChg = if (optionA) activeEvent.cashEffectA else activeEvent.cashEffectB
            val valChg = if (optionA) activeEvent.valuationEffectA else activeEvent.valuationEffectB
            val userChg = if (optionA) activeEvent.usersEffectA else activeEvent.usersEffectB
            val qualChg = if (optionA) activeEvent.qualityEffectA else activeEvent.qualityEffectB
            val burnChg = if (optionA) activeEvent.burnEffectA else activeEvent.burnEffectB

            var updatedCash = (state.cash + cashChg).coerceAtLeast(0.0)
            var updatedQuality = (state.productQuality + qualChg).coerceAtLeast(1.0)
            var updatedValuation = (state.valuation + valChg).coerceAtLeast(50_000.0)
            var updatedUsers = (state.users + userChg).coerceAtLeast(0)

            val summaryText = if (optionA) activeEvent.optionAOutcome else activeEvent.optionBOutcome
            var headlineText = "[Event Decided] $summaryText"

            // Handle risk calculations for Option B
            if (!optionA && activeEvent.riskFactorB > 0) {
                val roll = Random.nextFloat()
                if (roll < activeEvent.riskFactorB) {
                    // Risk failed!
                    updatedCash = (updatedCash + activeEvent.riskEffectCashB).coerceAtLeast(0.0)
                    updatedQuality = (updatedQuality + activeEvent.riskEffectQualityB).coerceAtLeast(1.0)
                    headlineText += " - " + activeEvent.riskFailTextB
                } else {
                    headlineText += " - " + activeEvent.riskSuccessTextB
                }
            }

            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = headlineText,
                source = "Board"
            )

            state.copy(
                cash = updatedCash,
                productQuality = updatedQuality,
                valuation = updatedValuation,
                users = updatedUsers,
                activeDecisionId = null,
                newsFeed = updatedNews.takeLast(40)
            )
        }

        activeEventDecision = null
        prefs.saveGameState(_uiState.value)
    }

    var activeEventDecision: EventDecision? = null
        private set

    fun advanceWeek() {
        val currentState = _uiState.value
        if (currentState.isGameOver || currentState.isIPOExited) return

        _uiState.update { state ->
            val nextWeek = state.week + 1

            // Apply active event & trend multipliers
            var userGrowthMult = 1.0
            var valuationMult = 1.0
            var salaryMult = 1.0
            var cashMult = 1.0

            state.marketEvents.forEach { ev ->
                userGrowthMult *= ev.userGrowthMultiplier
                valuationMult *= ev.valuationMultiplier
                salaryMult *= ev.salaryMultiplier
                cashMult *= ev.cashMultiplier
            }

            state.marketTrends.forEach { tr ->
                if (tr.trendType == "BULLISH") {
                    userGrowthMult *= 1.3
                    valuationMult *= 1.25
                } else if (tr.trendType == "BEARISH") {
                    userGrowthMult *= 0.65
                    valuationMult *= 0.75
                }
            }

            // Count team roles
            val engineers = state.employees.count { it.role == "Engineer" }
            val marketers = state.employees.count { it.role == "Marketer" }
            val pms = state.employees.count { it.role == "PM" }

            // 1. Calculate Product Quality
            var qualityDelta = 0.0
            if (engineers > 0) {
                val totalEngineerSkill = state.employees.filter { it.role == "Engineer" }.sumOf { it.skillScore }
                val pmMultiplier = 1.0 + (pms * 0.25).coerceAtMost(0.75)
                qualityDelta = (totalEngineerSkill / 250.0) * pmMultiplier
            } else {
                qualityDelta = -0.15 // Bit rot if no engineers!
            }
            val updatedQuality = (state.productQuality + qualityDelta).coerceIn(1.0, 100.0)

            // 2. User Growth Model
            val marketAccessMultiplier = state.unlockedRegions.size * 0.7
            val growthModifier = (updatedQuality / 20.0).coerceAtLeast(0.5)
            val marketingBoost = marketers * 250
            
            var userGrowthRate = state.sector.baseGrowth * growthModifier * marketAccessMultiplier * userGrowthMult
            if (userGrowthRate < 0.01) userGrowthRate = 0.01

            val baseOrganics = (updatedQuality * 5).toLong()
            val oldUsers = state.users
            val userGain = if (oldUsers == 0L) {
                (200 + marketingBoost).toLong()
            } else {
                (oldUsers * userGrowthRate).toLong() + marketingBoost + baseOrganics
            }
            val updatedUsers = oldUsers + userGain

            // 3. Weekly Revenue
            val weeklyRevenue = updatedUsers * (state.sector.revenuePerUser / 4.0)

            // 4. Weekly Burn rate
            val baseRent = when (state.ceoStage) {
                CEOStage.SOLO_FOUNDER -> 0.0
                CEOStage.SEED_STAGE -> 500.0
                CEOStage.SERIES_A -> 2000.0
                CEOStage.SERIES_B -> 6000.0
                CEOStage.SERIES_C -> 20000.0
                CEOStage.TECH_TITAN -> 60000.0
            }
            val totalSalaries = state.employees.sumOf { it.salary } * salaryMult
            val weeklySalaries = totalSalaries / 4.0
            val totalWeeklyBurn = baseRent + weeklySalaries

            // 5. Balance Sheet update
            val updatedCash = (state.cash + weeklyRevenue - totalWeeklyBurn) * cashMult

            // Check Bankruptcy (Game Over)
            if (updatedCash <= 0) {
                val bankruptcyNews = state.newsFeed + NewsFeedItem(
                    timestamp = "Week $nextWeek",
                    headline = "BANKRUPTCY: Your cash balance reached $0! ${state.companyName} was forced to liquidate.",
                    source = "System",
                    isAlert = true
                )
                return@update state.copy(
                    cash = 0.0,
                    isGameOver = true,
                    week = nextWeek,
                    newsFeed = bankruptcyNews.takeLast(40)
                )
            }

            // 6. Valuation Calculations (Silicon Valley Revenue multiples + product value)
            val annualizedRevenue = weeklyRevenue * 52.0
            val multiple = when (state.sector) {
                Sector.SAAS -> 12
                Sector.AI -> 25
                Sector.BIOTECH -> 18
                Sector.FINTECH -> 15
                Sector.WEB3 -> 8
            }
            val rawValuation = (annualizedRevenue * multiple) + (updatedQuality * 12_000.0) + (updatedUsers * 4.0)
            val updatedValuation = (rawValuation * valuationMult).coerceAtLeast(100_000.0)
            val valuationDelta = if (state.valuation > 0) (updatedValuation - state.valuation) / state.valuation else 0.0

            // 7. Advance CEO Level / Title
            var updatedStage = state.ceoStage
            val upcomingNews = mutableListOf<NewsFeedItem>()

            for (stage in CEOStage.values()) {
                if (updatedValuation >= stage.minValuation && stage.ordinal > updatedStage.ordinal) {
                    updatedStage = stage
                    upcomingNews.add(
                        NewsFeedItem(
                            timestamp = "Week $nextWeek",
                            headline = "PROMOTED: You are now a ${stage.displayName}! (${stage.description})",
                            source = "System"
                        )
                    )
                }
            }

            // Regular Weekly performance news item
            upcomingNews.add(
                NewsFeedItem(
                    timestamp = "Week $nextWeek",
                    headline = "Weekly Report: Product quality ${String.format("%.1f", updatedQuality)}. Added ${formatNumber(userGain)} users. Cash flow: +${formatCurrency(weeklyRevenue)} rev / -${formatCurrency(totalWeeklyBurn)} burn.",
                    source = "Internal"
                )
            )

            // Random events generator (30% chance of a decision, 15% of random news headline)
            var activeDecisionId: String? = null
            if (Random.nextFloat() < 0.35) {
                val chosenEvent = getRandomEventsList(state.sector).random()
                activeEventDecision = chosenEvent
                activeDecisionId = chosenEvent.id
                upcomingNews.add(
                    NewsFeedItem(
                        timestamp = "Week $nextWeek",
                        headline = "ALERT: Critical board room decision requires your attention: ${chosenEvent.title}",
                        source = "Board",
                        isAlert = true
                    )
                )
            } else if (Random.nextFloat() < 0.20) {
                // Post a random news story to keep world feeling alive
                val fillerStories = listOf(
                    "Silicon Valley Bank announces new founder credit lines.",
                    "Y Combinator demo day launches 200 new competitors in ${state.sector.displayName}.",
                    "Venture Capital sentiment reaches bullish high after successful NASDAQ exits.",
                    "AWS issues regional apology for high latency, guarantees credit refunds.",
                    "ChatGPT updates AI models with ultra-low latency inference, boosting industry workflows."
                )
                upcomingNews.add(
                    NewsFeedItem(
                        timestamp = "Week $nextWeek",
                        headline = fillerStories.random(),
                        source = "TechCrunch"
                    )
                )
            }

            // 8. TICK THE MARKET SIMULATION (Competitors grow/launch products, trends update)
            val advancedMarketState = AIPoweredMarketEngine.advanceWeeklyMarket(
                state.copy(
                    week = nextWeek,
                    cash = updatedCash,
                    productQuality = updatedQuality,
                    users = updatedUsers,
                    valuation = updatedValuation,
                    valuationGrowth = valuationDelta,
                    ceoStage = updatedStage,
                    activeDecisionId = activeDecisionId,
                    newsFeed = (state.newsFeed + upcomingNews).takeLast(40)
                )
            )

            val companyState = advancedMarketState.toCompanyState()
            val updatedCompany = com.example.engine.InvestmentSystem.processWeeklyInvestmentDynamics(companyState)
            val newOffers = com.example.engine.InvestmentSystem.generateFundingOffers(updatedCompany)
            val finalGameState = updatedCompany.toGameState(advancedMarketState).copy(simFundingOffers = newOffers)
            
            // Add Progression XP & founder pass weekly gains, then check Achievements
            val withWeeklyXp = internalGainXP(finalGameState, 150L) // +150 XP per week
            val withPassXp = internalGainFounderPassXP(withWeeklyXp, 25L) // +25 Pass XP per week
            
            // Deduct CEO energy weekly and handle stress
            val nextEnergy = (withPassXp.ceoEnergy - 15).coerceAtLeast(0)
            val nextMood = if (nextEnergy <= 15) "STRESSED" else "WORKING"
            val stateWithCEOLogic = withPassXp.copy(
                ceoEnergy = nextEnergy,
                ceoMood = nextMood
            )
            
            internalCheckAchievements(stateWithCEOLogic)
        }

        // Generate new hire pool weekly
        generateCandidates()
        generateFundingOffers()
        prefs.saveGameState(_uiState.value)
        
        // Telemetry logging for game progression
        AnalyticsTracker.logEvent(
            "advance_week",
            mapOf(
                "week" to _uiState.value.week,
                "valuation" to _uiState.value.valuation,
                "cash" to _uiState.value.cash,
                "users" to _uiState.value.users,
                "founder_level" to _uiState.value.founderLevel
            )
        )

        // TRIGGER BACKGROUND GEMINI AI EVENT GENERATION FOR THE INCOMING WEEKS (Asynchronous/Non-blocking!)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nextEvent = AIPoweredMarketEngine.generateAICustomEvent(_uiState.value)
                _uiState.update { s ->
                    if (s.marketEvents.any { it.title == nextEvent.title }) s else {
                        val updatedEvents = s.marketEvents + nextEvent
                        val alertNews = s.newsFeed + NewsFeedItem(
                            timestamp = "Week ${s.week}",
                            headline = "AI ENGINE ALERT: [${nextEvent.type.name}] ${nextEvent.headline}",
                            source = "Market AI",
                            isAlert = true
                        )
                        s.copy(
                            marketEvents = updatedEvents,
                            newsFeed = alertNews.takeLast(40)
                        )
                    }
                }
                prefs.saveGameState(_uiState.value)
            } catch (e: Exception) {
                Log.e("GameViewModel", "AI background event launch error", e)
            }
        }
    }


    fun triggerIPO() {
        val currentValuation = _uiState.value.valuation
        if (currentValuation < 100_000_000.0) return

        _uiState.update { state ->
            val finalPayout = currentValuation * (1.0 - (state.totalVCDilution / 100.0))
            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "IPO HISTORIC SUCCESS: ${_uiState.value.companyName} went public on NASDAQ! Valuation: ${formatCurrency(currentValuation)}. Founder payout: ${formatCurrency(finalPayout)} after venture dilution.",
                source = "TechCrunch"
            )
            state.copy(
                isIPOExited = true,
                newsFeed = updatedNews.takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun acceptAcquisition(offerValue: Double, buyer: String) {
        _uiState.update { state ->
            val finalPayout = offerValue * (1.0 - (state.totalVCDilution / 100.0))
            val updatedNews = state.newsFeed + NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "ACQUIRED: $buyer acquires ${state.companyName} for ${formatCurrency(offerValue)}! Cash distributed to shareholders. CEO payouts: ${formatCurrency(finalPayout)}.",
                source = "TechCrunch"
            )
            state.copy(
                isIPOExited = true,
                newsFeed = updatedNews.takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    // List of static event structures
    private fun getRandomEventsList(sector: Sector): List<EventDecision> {
        return listOf(
            EventDecision(
                id = "aws_crash",
                title = "AWS Server Outage",
                description = "Our primary cloud server suffers high latency in US-East. Users are complaining about downtime! We can pay $10,000 to trigger our automated multi-region backup cluster, or wait it out.",
                optionAText = "Enable backup ($10k)",
                optionBText = "Wait for AWS patch",
                optionAOutcome = "Activated Azure hot swap. Uptime restored within seconds!",
                optionBOutcome = "Refused to pay. Wait was 4 hours.",
                cashEffectA = -10000.0,
                qualityEffectA = 10.0,
                valuationEffectA = 100000.0,
                cashEffectB = 0.0,
                qualityEffectB = -15.0,
                usersEffectB = -8000,
                valuationEffectB = -150000.0
            ),
            EventDecision(
                id = "techcrunch_profile",
                title = "TechCrunch Profile Pitch",
                description = "A Senior Editor at TechCrunch wants to write a feature piece on our company, but we have to buy a 'Venture Builder' sponsorship bundle for $15,000.",
                optionAText = "Pay for sponsorship ($15k)",
                optionBText = "Decline sponsorship",
                optionAOutcome = "We made front-page TechCrunch! Millions of developers read about our technology.",
                optionBOutcome = "Declined the offer. We relied on organic Twitter PR instead.",
                cashEffectA = -15000.0,
                usersEffectA = 45000,
                valuationEffectA = 800000.0,
                cashEffectB = 0.0,
                usersEffectB = 4000,
                valuationEffectB = 50000.0
            ),
            EventDecision(
                id = "salary_dispute",
                title = "Talent Poaching Threat",
                description = "A major FAANG corporation is offering your principal software developer double his salary. He's asking for a $6,000 monthly raise to stay.",
                optionAText = "Approve $6k raise",
                optionBText = "Let him go to FAANG",
                optionAOutcome = "Superstar retained. Developer productivity is preserved, and team morale is stellar.",
                optionBOutcome = "Let him go. Engineering output slowed significantly.",
                burnEffectA = 6000.0,
                qualityEffectA = 12.0,
                valuationEffectA = 200000.0,
                burnEffectB = 0.0,
                qualityEffectB = -18.0,
                valuationEffectB = -300000.0
            ),
            EventDecision(
                id = "security_ransom",
                title = "Database Ransomware Ransom",
                description = "A mysterious hacker claims to have found a vulnerability in our database and demands $30,000 in Bitcoin. Paying might patch it, ignoring risks a leak.",
                optionAText = "Pay Ransom ($30k)",
                optionBText = "Ignore hacker & patch",
                optionAOutcome = "Bitcoin paid. Cyberthreat solved quietly, but board members are uneasy.",
                optionBOutcome = "Ignored hacker. Auditing codebase immediately. High risk of leak!",
                cashEffectA = -30000.0,
                valuationEffectA = -50000.0,
                cashEffectB = -5000.0, // Patching costs
                riskFactorB = 0.35f,
                riskEffectCashB = -100000.0, // Fine cost
                riskEffectQualityB = -25.0,
                riskSuccessTextB = "Patch was successful! Hacker defeated.",
                riskFailTextB = "DATABASE LEAK! Cybersec fines of $100k issued. Reputation tarnished!"
            ),
            EventDecision(
                id = "patent_troll",
                title = "Patent Troll Lawsuit",
                description = "An aggressive patent holding company has sued us for infringing an obsolete API routing patent. A legal firm wants $25,000 to defend or settle for $12,000.",
                optionAText = "Hire legal defense ($25k)",
                optionBText = "Settle for $12k",
                optionAOutcome = "Fought back in court! Troll backed off. Setup legal precedent.",
                optionBOutcome = "Settle quietly. Paid patent troll quickly.",
                cashEffectA = -25000.0,
                valuationEffectA = 300000.0,
                cashEffectB = -12000.0,
                valuationEffectB = -50000.0
            ),
            EventDecision(
                id = "tiktok_viral",
                title = "Viral Social Media Buzz",
                description = "A high-profile VC influencer posted a meme showcasing our platform. We can inject $10,000 into targeted social ads to amplify the trend.",
                optionAText = "Amplify meme ($10k)",
                optionBText = "Let it run organically",
                optionAOutcome = "Ad campaign succeeded. Traffic surged to an all-time high!",
                optionBOutcome = "Mild steady influx of organic tech workers.",
                cashEffectA = -10000.0,
                usersEffectA = 60000,
                valuationEffectA = 1200000.0,
                cashEffectB = 0.0,
                usersEffectB = 12000,
                valuationEffectB = 150000.0
            )
        )
    }

    // Helper formatting tools
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    fun formatNumber(num: Long): String {
        return NumberFormat.getNumberInstance(Locale.US).format(num)
    }

    // --- STATE CONVERTERS ---
    fun GamePrefs.GameState.toCompanyState(): com.example.engine.CompanyState {
        val industryVal = when (this.sector) {
            Sector.SAAS -> com.example.engine.SimulationIndustry.SAAS
            Sector.AI -> com.example.engine.SimulationIndustry.AI
            Sector.BIOTECH -> com.example.engine.SimulationIndustry.BIOTECH
            Sector.FINTECH -> com.example.engine.SimulationIndustry.FINTECH
            Sector.WEB3 -> com.example.engine.SimulationIndustry.WEB3
        }

        val employeesVal = this.employees.map { emp ->
            com.example.engine.SimEmployee(
                id = emp.name,
                name = emp.name,
                role = emp.role,
                skillLevel = emp.skillLevel,
                skillScore = emp.skillScore,
                salary = emp.salary,
                satisfaction = 80.0,
                stress = 10.0,
                productivity = emp.skillScore.toDouble(),
                weeklyWorkload = 40.0
            )
        }

        val capTableVal = if (this.capTable.isEmpty()) {
            listOf(
                com.example.engine.Shareholder(id = "ceo", name = this.ceoName, equityShares = 9_000_000L, ownershipPercent = 90.0, isFounder = true),
                com.example.engine.Shareholder(id = "employee_pool", name = "Employee Option Pool", equityShares = 1_000_000L, ownershipPercent = 10.0, isFounder = false)
            )
        } else {
            this.capTable
        }

        val productVal = com.example.engine.SimProduct(
            name = "Core Product",
            category = when (this.sector) {
                Sector.SAAS -> com.example.engine.ProductCategory.SAAS
                Sector.AI -> com.example.engine.ProductCategory.AI_PRODUCT
                Sector.BIOTECH -> com.example.engine.ProductCategory.MOBILE_APP
                Sector.FINTECH -> com.example.engine.ProductCategory.CRM
                Sector.WEB3 -> com.example.engine.ProductCategory.SOCIAL_NETWORK
            },
            designQuality = this.productQuality,
            codeQuality = this.productQuality,
            bugDensity = 5.0,
            bugCount = 5,
            featureCompleteness = 20.0,
            launchWeek = 1,
            weeklyMarketingBudget = 50.0,
            activeUsers = this.users,
            features = listOf(
                com.example.engine.SimFeature(name = "Core MVP Architecture", complexity = 50.0, progress = 10.0, isCompleted = false)
            )
        )

        return com.example.engine.CompanyState(
            companyName = this.companyName,
            ceoName = this.ceoName,
            currentWeek = this.week,
            industry = industryVal,
            businessModel = com.example.engine.BusinessModel.SUBSCRIPTION,
            headquarters = com.example.engine.Headquarters.SILICON_VALLEY,
            registrationState = com.example.engine.RegistrationState.DELAWARE_C_CORP,
            products = listOf(productVal),
            employees = employeesVal,
            capTable = capTableVal,
            totalAuthorizedShares = this.totalAuthorizedShares,
            cash = this.cash,
            valuation = this.valuation,
            averageEmployeeSatisfaction = 80.0,
            newsLog = emptyList(),
            convertibleNotes = this.convertibleNotes,
            debts = this.debts,
            boardMembers = this.boardMembers,
            activeProposals = this.activeProposals,
            pitchDeckQuality = this.pitchDeckQuality,
            totalInvestmentRaised = this.totalInvestmentRaised,
            fundingOffers = this.simFundingOffers
        )
    }

    fun com.example.engine.CompanyState.toGameState(oldState: GamePrefs.GameState): GamePrefs.GameState {
        val newNewsItems = this.newsLog.map { logText ->
            NewsFeedItem(
                timestamp = "Week ${this.currentWeek}",
                headline = logText,
                source = "System"
            )
        }

        return oldState.copy(
            week = this.currentWeek,
            cash = this.cash,
            valuation = this.valuation,
            productQuality = this.products.firstOrNull()?.designQuality ?: oldState.productQuality,
            convertibleNotes = this.convertibleNotes,
            debts = this.debts,
            boardMembers = this.boardMembers,
            activeProposals = this.activeProposals,
            pitchDeckQuality = this.pitchDeckQuality,
            totalInvestmentRaised = this.totalInvestmentRaised,
            capTable = this.capTable,
            totalAuthorizedShares = this.totalAuthorizedShares,
            newsFeed = (oldState.newsFeed + newNewsItems).takeLast(40)
        )
    }

    // --- ADVANCED INVESTMENT ACTIONS ---
    fun upgradePitchDeck() {
        _uiState.update { state ->
            val companyState = state.toCompanyState()
            val cost = 5000.0
            if (state.cash < cost) return@update state
            val upgraded = com.example.engine.InvestmentSystem.upgradePitchDeck(companyState)
            upgraded.copy(cash = upgraded.cash - cost).toGameState(state)
        }
        prefs.saveGameState(_uiState.value)
    }

    fun negotiateFundingOffer(offerId: String, strategy: String) {
        _uiState.update { state ->
            val companyState = state.toCompanyState()
            val updated = com.example.engine.InvestmentSystem.negotiateOffer(companyState, offerId, strategy)
            val updatedState = updated.toGameState(state)
            updatedState.copy(simFundingOffers = updated.fundingOffers)
        }
        prefs.saveGameState(_uiState.value)
    }

    fun acceptSimFundingOffer(offerId: String) {
        _uiState.update { state ->
            val companyState = state.toCompanyState()
            val updated = com.example.engine.InvestmentSystem.acceptFundingOffer(companyState, offerId)
            val updatedState = updated.toGameState(state)
            val nextOffers = com.example.engine.InvestmentSystem.generateFundingOffers(updated)
            updatedState.copy(
                simFundingOffers = nextOffers,
                totalInvestmentRaised = updated.totalInvestmentRaised,
                totalVCDilution = 100.0 - (updated.capTable.find { it.isFounder }?.ownershipPercent ?: 100.0)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun submitStrategicProposal(proposalType: String, title: String, description: String) {
        _uiState.update { state ->
            val companyState = state.toCompanyState()
            val updated = com.example.engine.InvestmentSystem.submitStrategicProposal(companyState, proposalType, title, description)
            updated.toGameState(state)
        }
        prefs.saveGameState(_uiState.value)
    }

    // --- PROGRESSION & MONETIZATION METHODS ---
    
    fun gainXP(amount: Long) {
        _uiState.update { state ->
            internalGainXP(state, amount)
        }
        prefs.saveGameState(_uiState.value)
    }

    private fun internalGainXP(state: GamePrefs.GameState, amount: Long): GamePrefs.GameState {
        var currentXp = state.xp + amount
        var currentLevel = state.founderLevel
        var requiredXp = currentLevel * 500L
        val news = mutableListOf<NewsFeedItem>()
        
        while (currentXp >= requiredXp) {
            currentXp -= requiredXp
            currentLevel++
            requiredXp = currentLevel * 500L
            
            // Level up reward
            val tokenReward = currentLevel * 15
            news.add(
                NewsFeedItem(
                    timestamp = "Week ${state.week}",
                    headline = "LEVEL UP! Reached Founder Level $currentLevel. Earned +$tokenReward Cosmetic Tokens! 🏆",
                    source = "System"
                )
            )
        }
        
        var updatedState = state.copy(
            xp = currentXp,
            founderLevel = currentLevel,
            newsFeed = (state.newsFeed + news).takeLast(40)
        )
        if (news.isNotEmpty()) {
            val totalTokens = news.sumOf { (it.headline.substringAfter("+$").substringBefore(" ").toIntOrNull() ?: 0) }
            updatedState = updatedState.copy(cosmeticTokens = updatedState.cosmeticTokens + totalTokens)
        }
        return updatedState
    }

    fun gainFounderPassXP(amount: Long) {
        _uiState.update { state ->
            internalGainFounderPassXP(state, amount)
        }
        prefs.saveGameState(_uiState.value)
    }

    private fun internalGainFounderPassXP(state: GamePrefs.GameState, amount: Long): GamePrefs.GameState {
        var passXp = state.founderPassXp + amount
        var passLevel = state.founderPassLevel
        val news = mutableListOf<NewsFeedItem>()
        
        while (passXp >= 100L && passLevel < 20) {
            passXp -= 100L
            passLevel++
            news.add(
                NewsFeedItem(
                    timestamp = "Week ${state.week}",
                    headline = "FOUNDER PASS: Unlocked Tier $passLevel! Check your rewards panel.",
                    source = "System"
                )
            )
        }
        
        return state.copy(
            founderPassXp = passXp,
            founderPassLevel = passLevel,
            newsFeed = (state.newsFeed + news).takeLast(40)
        )
    }

    fun checkAndRewardAchievements() {
        _uiState.update { state ->
            internalCheckAchievements(state)
        }
        prefs.saveGameState(_uiState.value)
    }

    private fun internalCheckAchievements(state: GamePrefs.GameState): GamePrefs.GameState {
        val completed = state.completedAchievements.toMutableList()
        val news = mutableListOf<NewsFeedItem>()
        var additionalXp = 0L
        var additionalTokens = 0
        val unlockedCosmetics = state.unlockedCosmetics.toMutableList()

        com.example.engine.ProgressionData.ACHIEVEMENTS.forEach { ach ->
            if (!completed.contains(ach.id)) {
                val valueToCheck = when (ach.targetType) {
                    "LEVEL" -> state.founderLevel.toDouble()
                    "REVENUE" -> state.valuation
                    "EMPLOYEES" -> state.employees.size.toDouble()
                    "QUALITY" -> state.productQuality
                    else -> 0.0
                }
                
                if (valueToCheck >= ach.targetValue) {
                    completed.add(ach.id)
                    additionalXp += ach.xpReward
                    additionalTokens += ach.tokenReward
                    ach.cosmeticRewardId?.let { cosmeticId ->
                        if (!unlockedCosmetics.contains(cosmeticId)) {
                            unlockedCosmetics.add(cosmeticId)
                        }
                    }
                    news.add(
                        NewsFeedItem(
                            timestamp = "Week ${state.week}",
                            headline = "ACHIEVEMENT UNLOCKED: '${ach.title}'! Earned +${ach.xpReward} XP, +${ach.tokenReward} Tokens! 🎖️",
                            source = "System"
                        )
                    )
                }
            }
        }

        if (news.isEmpty()) return state

        var updatedState = state.copy(
            completedAchievements = completed,
            unlockedCosmetics = unlockedCosmetics,
            cosmeticTokens = state.cosmeticTokens + additionalTokens,
            newsFeed = (state.newsFeed + news).takeLast(40)
        )
        if (additionalXp > 0L) {
            updatedState = internalGainXP(updatedState, additionalXp)
        }
        return updatedState
    }

    fun claimDailyReward(doubleWithAd: Boolean = false) {
        _uiState.update { state ->
            val nextDay = (state.lastClaimedDailyDay % 7) + 1
            var tokenReward = when (nextDay) {
                1 -> 50
                3 -> 100
                5 -> 150
                7 -> 250
                else -> 20
            }
            if (doubleWithAd) tokenReward *= 2
            
            val unlockedCosmetics = state.unlockedCosmetics.toMutableList()
            var cosmeticUnlockStr = ""
            val rewardCosmeticId = when (nextDay) {
                2 -> "desk_neon"
                4 -> "chair_gaming"
                6 -> "plant_bonsai_holo"
                7 -> "wall_neon_logo"
                else -> null
            }
            
            rewardCosmeticId?.let { cid ->
                if (!unlockedCosmetics.contains(cid)) {
                    unlockedCosmetics.add(cid)
                    cosmeticUnlockStr = " & unlocked cosmetic item!"
                }
            }

            val newsItem = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "DAILY REWARD: Claimed Day $nextDay login reward! Received +$tokenReward Tokens$cosmeticUnlockStr",
                source = "System"
            )

            val updatedState = state.copy(
                lastClaimedDailyDay = nextDay,
                lastClaimedDailyTime = System.currentTimeMillis(),
                cosmeticTokens = state.cosmeticTokens + tokenReward,
                unlockedCosmetics = unlockedCosmetics,
                newsFeed = (state.newsFeed + newsItem).takeLast(40)
            )
            
            internalGainXP(updatedState, 150L)
        }
        prefs.saveGameState(_uiState.value)
    }

    fun equipCosmetic(id: String, category: String) {
        _uiState.update { state ->
            if (!state.unlockedCosmetics.contains(id)) return@update state
            when (category) {
                "DESK" -> state.copy(equippedDesk = id)
                "CHAIR" -> state.copy(equippedChair = id)
                "PLANT" -> state.copy(equippedPlant = id)
                "WALL_ART" -> state.copy(equippedWallArt = id)
                else -> state
            }
        }
        prefs.saveGameState(_uiState.value)
    }

    fun buyCosmetic(id: String, cost: Int) {
        _uiState.update { state ->
            if (state.unlockedCosmetics.contains(id)) return@update state
            if (state.cosmeticTokens < cost) return@update state
            val updatedUnlocked = state.unlockedCosmetics + id
            state.copy(
                cosmeticTokens = state.cosmeticTokens - cost,
                unlockedCosmetics = updatedUnlocked
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun unlockPremiumCompany() {
        _uiState.update { state ->
            state.copy(
                isPremiumCompanyEnabled = true,
                cosmeticTokens = state.cosmeticTokens + 200,
                unlockedCosmetics = state.unlockedCosmetics + "desk_hologram"
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun unlockExclusiveIndustry() {
        _uiState.update { state ->
            state.copy(
                isExclusiveIndustryUnlocked = true,
                cosmeticTokens = state.cosmeticTokens + 300,
                unlockedCosmetics = state.unlockedCosmetics + "plant_bonsai_holo"
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun purchaseFounderPassPro() {
        _uiState.update { state ->
            state.copy(
                isFounderPassPremiumUnlocked = true,
                cosmeticTokens = state.cosmeticTokens + 500,
                unlockedCosmetics = state.unlockedCosmetics + "chair_gold"
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun subscribeToCEOClub() {
        _uiState.update { state ->
            state.copy(
                isSubscribedToCEOClub = true,
                hasRemovedAds = true,
                isFounderPassPremiumUnlocked = true,
                cosmeticTokens = state.cosmeticTokens + 1000
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun claimFounderPassReward(tier: Int, isPremium: Boolean) {
        _uiState.update { state ->
            val rewardTier = com.example.engine.ProgressionData.FOUNDER_PASS_TIERS.find { it.tier == tier } ?: return@update state
            val rewardId = if (isPremium) rewardTier.premiumRewardId else rewardTier.freeRewardId
            val rewardName = if (isPremium) rewardTier.premiumRewardName else rewardTier.freeRewardName
            if (rewardId == null) return@update state
            
            val unlockedCosmetics = state.unlockedCosmetics.toMutableList()
            var tokensToAdd = 0
            
            if (rewardId.startsWith("tokens_")) {
                tokensToAdd = rewardId.substringAfter("tokens_").toIntOrNull() ?: 0
            } else if (rewardId.startsWith("xp_booster")) {
                return@update internalGainXP(state, 500L)
            } else {
                if (!unlockedCosmetics.contains(rewardId)) {
                    unlockedCosmetics.add(rewardId)
                }
            }

            val newsItem = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "FOUNDER PASS: Claimed Tier $tier reward - $rewardName!",
                source = "System"
            )

            state.copy(
                unlockedCosmetics = unlockedCosmetics,
                cosmeticTokens = state.cosmeticTokens + tokensToAdd,
                newsFeed = (state.newsFeed + newsItem).takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun togglePlayGamesSignIn() {
        _uiState.update { state ->
            val nextState = !state.isPlayGamesSignedIn
            val news = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = if (nextState) "GPLAY: Signed in to Google Play Games. Achievements synced." else "GPLAY: Signed out of Google Play Games.",
                source = "System"
            )
            state.copy(
                isPlayGamesSignedIn = nextState,
                newsFeed = (state.newsFeed + news).takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun triggerCloudSave() {
        _uiState.update { state ->
            val news = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "CLOUD: Game state successfully backed up to cloud save storage.",
                source = "System"
            )
            state.copy(
                cloudSaveTimestamp = System.currentTimeMillis(),
                newsFeed = (state.newsFeed + news).takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun completeSimulatedRewardedAd() {
        _uiState.update { state ->
            val tokenBonus = 30
            val news = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "ADMOB: Watched Rewarded Video. Received +$tokenBonus Cosmetic Tokens!",
                source = "System"
            )
            state.copy(
                cosmeticTokens = state.cosmeticTokens + tokenBonus,
                newsFeed = (state.newsFeed + news).takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun executeCEODeepWork() {
        _uiState.update { state ->
            val news = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "ACTION: CEO ${state.ceoName} did deep-work, focusing on product refinement (+2.0 Quality, +30 XP)!",
                source = "Founder"
            )
            val updatedQuality = (state.productQuality + 2.0).coerceIn(1.0, 100.0)
            val intermediate = state.copy(
                ceoMood = "WORKING",
                productQuality = updatedQuality,
                newsFeed = (state.newsFeed + news).takeLast(40)
            )
            internalGainXP(intermediate, 30L)
        }
        prefs.saveGameState(_uiState.value)
    }

    fun executeCEOGiveBonus() {
        _uiState.update { state ->
            if (state.cash < 1000.0) {
                val failNews = NewsFeedItem(
                    timestamp = "Week ${state.week}",
                    headline = "FAILED ACTION: Insufficient cash to distribute team bonuses!",
                    source = "System",
                    isAlert = true
                )
                state.copy(
                    newsFeed = (state.newsFeed + failNews).takeLast(40)
                )
            } else {
                val news = NewsFeedItem(
                    timestamp = "Week ${state.week}",
                    headline = "ACTION: CEO ${state.ceoName} distributed custom team bonuses (-$1,000 cash)! Morale skyrocketed (+150 XP, +3.0 Quality)!",
                    source = "Founder"
                )
                val updatedQuality = (state.productQuality + 3.0).coerceIn(1.0, 100.0)
                val intermediate = state.copy(
                    cash = state.cash - 1000.0,
                    ceoMood = "CELEBRATING",
                    productQuality = updatedQuality,
                    newsFeed = (state.newsFeed + news).takeLast(40)
                )
                internalGainXP(intermediate, 150L)
            }
        }
        prefs.saveGameState(_uiState.value)
    }

    fun executeCEOOvertimeCrunch() {
        _uiState.update { state ->
            val news = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "ACTION: CEO ${state.ceoName} initiated a critical crunch phase (-25 Energy). Added new users and raw velocity (+450 Users, -$500 supplies)!",
                source = "Founder",
                isAlert = true
            )
            state.copy(
                ceoMood = "STRESSED",
                ceoEnergy = (state.ceoEnergy - 25).coerceAtLeast(0),
                users = state.users + 450L,
                cash = (state.cash - 500.0).coerceAtLeast(0.0),
                newsFeed = (state.newsFeed + news).takeLast(40)
            )
        }
        prefs.saveGameState(_uiState.value)
    }

    fun executeCEODrinkEspresso() {
        _uiState.update { state ->
            val news = NewsFeedItem(
                timestamp = "Week ${state.week}",
                headline = "ACTION: CEO ${state.ceoName} drank a triple-shot holographic espresso! Full Energy restored (+100 Energy, +20 XP)!",
                source = "Founder"
            )
            val intermediate = state.copy(
                ceoMood = "DRINKING",
                ceoEnergy = 100,
                newsFeed = (state.newsFeed + news).takeLast(40)
            )
            internalGainXP(intermediate, 20L)
        }
        prefs.saveGameState(_uiState.value)
    }
}
