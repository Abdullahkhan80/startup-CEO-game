package com.example.engine

import androidx.annotation.Keep
import java.util.UUID

@Keep
object InvestmentSystem {

    /**
     * Spending money to upgrade the pitch deck quality.
     */
    fun upgradePitchDeck(state: CompanyState): CompanyState {
        val cost = 5000.0
        if (state.cash < cost) {
            return state.copy(newsLog = state.newsLog + "Insufficient funds to upgrade the Pitch Deck. Needs $5,000.")
        }
        val qualityBoost = (10..20).random().toDouble()
        val newQuality = (state.pitchDeckQuality + qualityBoost).coerceAtMost(100.0)
        
        return state.copy(
            cash = SimulationEngine.round(state.cash - cost),
            pitchDeckQuality = SimulationEngine.round(newQuality),
            newsLog = state.newsLog + "Upgraded Pitch Deck! Quality is now ${SimulationEngine.round(newQuality)}%. (Cost: $5,000)"
        )
    }

    /**
     * Generates a list of funding and debt financing offers tailored to the company's size,
     * valuation, registration state, and pitch deck quality.
     */
    fun generateFundingOffers(state: CompanyState): List<SimInvestmentOffer> {
        val currentWeek = state.currentWeek
        val valBonus = 1.0 + (state.pitchDeckQuality / 200.0) // Up to +50% better deals
        val baseValuation = state.valuation * valBonus

        val offers = mutableListOf<SimInvestmentOffer>()

        // 1. Debt Financing (Available if registered LLC or C-Corp and not bankrupt)
        if (state.registrationState != RegistrationState.UNREGISTERED) {
            val maxDebt = (state.valuation * 0.15).coerceIn(10000.0, 500000.0)
            offers.add(
                SimInvestmentOffer(
                    investorName = "Silicon Valley Bank (Debt)",
                    type = InvestmentType.DEBT_FINANCING,
                    amount = SimulationEngine.round(maxDebt),
                    interestRate = 0.08, // 8% annual
                    termWeeks = 52,
                    expirationWeek = currentWeek + 3
                )
            )
        }

        // 2. Early Stage: SAFE Agreements & Convertible Notes (Valuation < $1.5M)
        if (baseValuation < 1_500_000.0) {
            offers.add(
                SimInvestmentOffer(
                    investorName = "Paul Graham (Angel)",
                    type = InvestmentType.SAFE_AGREEMENT,
                    amount = 50_000.0,
                    valuationCap = SimulationEngine.round(baseValuation * 1.2),
                    discountRate = 0.20,
                    expirationWeek = currentWeek + 2
                )
            )
            offers.add(
                SimInvestmentOffer(
                    investorName = "Y Combinator",
                    type = InvestmentType.SAFE_AGREEMENT,
                    amount = 125_000.0,
                    valuationCap = SimulationEngine.round(baseValuation * 1.1),
                    discountRate = 0.10,
                    boardSeatRequired = false,
                    expirationWeek = currentWeek + 3
                )
            )
            offers.add(
                SimInvestmentOffer(
                    investorName = "Naval Ravikant (Convertible)",
                    type = InvestmentType.CONVERTIBLE_NOTE,
                    amount = 100_000.0,
                    valuationCap = SimulationEngine.round(baseValuation * 1.15),
                    discountRate = 0.15,
                    interestRate = 0.06, // 6% simple annual interest
                    expirationWeek = currentWeek + 2
                )
            )
        }

        // 3. Seed Rounds (Valuation $1.5M to $8M)
        if (baseValuation >= 800_000.0 && baseValuation < 8_000_000.0) {
            offers.add(
                SimInvestmentOffer(
                    investorName = "First Round Capital",
                    type = InvestmentType.SEED_ROUND,
                    amount = SimulationEngine.round(baseValuation * 0.15),
                    impliedValuation = SimulationEngine.round(baseValuation),
                    boardSeatRequired = true,
                    expirationWeek = currentWeek + 3
                )
            )
            offers.add(
                SimInvestmentOffer(
                    investorName = "Floodgate Fund",
                    type = InvestmentType.SEED_ROUND,
                    amount = SimulationEngine.round(baseValuation * 0.10),
                    impliedValuation = SimulationEngine.round(baseValuation * 1.1),
                    boardSeatRequired = false,
                    expirationWeek = currentWeek + 2
                )
            )
        }

        // 4. Series A (Valuation $8M to $40M)
        if (baseValuation >= 5_000_000.0 && baseValuation < 40_000_000.0) {
            offers.add(
                SimInvestmentOffer(
                    investorName = "Sequoia Capital",
                    type = InvestmentType.SERIES_A,
                    amount = SimulationEngine.round(baseValuation * 0.15),
                    impliedValuation = SimulationEngine.round(baseValuation),
                    boardSeatRequired = true,
                    expirationWeek = currentWeek + 4
                )
            )
            offers.add(
                SimInvestmentOffer(
                    investorName = "Andreessen Horowitz",
                    type = InvestmentType.SERIES_A,
                    amount = SimulationEngine.round(baseValuation * 0.18),
                    impliedValuation = SimulationEngine.round(baseValuation * 0.95),
                    boardSeatRequired = true,
                    expirationWeek = currentWeek + 3
                )
            )
        }

        // 5. Series B & C (Valuation $40M to $300M)
        if (baseValuation >= 30_000_000.0) {
            val tier = if (baseValuation < 120_000_000.0) InvestmentType.SERIES_B else InvestmentType.SERIES_C
            val name = if (tier == InvestmentType.SERIES_B) "Benchmark" else "Tiger Global"
            offers.add(
                SimInvestmentOffer(
                    investorName = "$name Capital",
                    type = tier,
                    amount = SimulationEngine.round(baseValuation * 0.12),
                    impliedValuation = SimulationEngine.round(baseValuation),
                    boardSeatRequired = true,
                    expirationWeek = currentWeek + 4
                )
            )
            offers.add(
                SimInvestmentOffer(
                    investorName = "SoftBank Vision Fund",
                    type = tier,
                    amount = SimulationEngine.round(baseValuation * 0.20),
                    impliedValuation = SimulationEngine.round(baseValuation * 0.9),
                    boardSeatRequired = true,
                    expirationWeek = currentWeek + 5
                )
            )
        }

        // 6. IPO & Acquisition (High Value opportunities)
        if (baseValuation >= 100_000_000.0) {
            offers.add(
                SimInvestmentOffer(
                    investorName = "Nasdaq Public Market",
                    type = InvestmentType.IPO,
                    amount = SimulationEngine.round(baseValuation * 0.25),
                    impliedValuation = SimulationEngine.round(baseValuation),
                    expirationWeek = currentWeek + 8
                )
            )
        }

        if (state.products.any { it.activeUsers > 10000 }) {
            offers.add(
                SimInvestmentOffer(
                    investorName = "Alphabet Inc. (Acquisition)",
                    type = InvestmentType.ACQUISITION,
                    amount = SimulationEngine.round(baseValuation * 1.3), // Premium buy out
                    expirationWeek = currentWeek + 3
                )
            )
        }

        return offers
    }

    /**
     * Conducts a negotiation on a selected investment offer.
     * Alters the offer terms or risks breaking the deal depending on strategy and Pitch Deck Quality.
     */
    fun negotiateOffer(
        state: CompanyState,
        offerId: String,
        strategy: String // "AGGRESSIVE_VALUATION", "FRIENDLY_PARTNERSHIP", "LOW_DILUTION"
    ): CompanyState {
        val offer = state.fundingOffers.find { it.id == offerId }
            ?: return state.copy(newsLog = state.newsLog + "Offer not found.")

        val roll = (1..100).random()
        val pitchFactor = state.pitchDeckQuality

        when (strategy) {
            "AGGRESSIVE_VALUATION" -> {
                // Try to boost valuation by 15-30%
                val successThreshold = 40 + (pitchFactor * 0.4) // max 80% success
                if (roll < successThreshold) {
                    val multiplier = 1.15 + (1..15).random() / 100.0
                    val updatedOffer = when {
                        offer.valuationCap != null -> offer.copy(valuationCap = SimulationEngine.round(offer.valuationCap * multiplier))
                        offer.impliedValuation != null -> offer.copy(impliedValuation = SimulationEngine.round(offer.impliedValuation * multiplier))
                        else -> offer
                    }
                    val updatedOffers = state.fundingOffers.map { if (it.id == offerId) updatedOffer else it }
                    return state.copy(
                        fundingOffers = updatedOffers,
                        newsLog = state.newsLog + "Negotiation Success! ${offer.investorName} increased their valuation proposal."
                    )
                } else {
                    // Fail: Investor gets insulted and walks away
                    val updatedOffers = state.fundingOffers.filter { it.id != offerId }
                    return state.copy(
                        fundingOffers = updatedOffers,
                        newsLog = state.newsLog + "Negotiation Failed! ${offer.investorName} felt your valuation was unrealistic and withdrew their offer."
                    )
                }
            }
            "FRIENDLY_PARTNERSHIP" -> {
                // Improves supportiveness, can waive board seat requirement if lucky
                val updatedOffer = offer.copy(
                    supportiveness = (offer.supportiveness + 15.0).coerceAtMost(100.0),
                    boardSeatRequired = if (roll > 60 && offer.type != InvestmentType.SERIES_A && offer.type != InvestmentType.SERIES_B) false else offer.boardSeatRequired
                )
                val updatedOffers = state.fundingOffers.map { if (it.id == offerId) updatedOffer else it }
                return state.copy(
                    fundingOffers = updatedOffers,
                    newsLog = state.newsLog + "Partnership Talk: Built a solid rapport with ${offer.investorName}. Future collaboration will be smoother."
                )
            }
            "LOW_DILUTION" -> {
                // Lowers the funding amount but keeps the valuation high, lowering overall dilution percentage
                val updatedOffer = offer.copy(
                    amount = SimulationEngine.round(offer.amount * 0.8)
                )
                val updatedOffers = state.fundingOffers.map { if (it.id == offerId) updatedOffer else it }
                return state.copy(
                    fundingOffers = updatedOffers,
                    newsLog = state.newsLog + "Dilution Control: Negotiated a smaller check size from ${offer.investorName} to preserve founder equity."
                )
            }
            else -> return state
        }
    }

    /**
     * Accepts a funding offer and updates the company state, financials, cap table, and governance.
     */
    fun acceptFundingOffer(state: CompanyState, offerId: String): CompanyState {
        val offer = state.fundingOffers.find { it.id == offerId }
            ?: return state.copy(newsLog = state.newsLog + "Funding offer not found.")

        // Filter out this accepted offer
        val remainingOffers = state.fundingOffers.filter { it.id != offerId }

        when (offer.type) {
            InvestmentType.DEBT_FINANCING -> {
                // Debt financing adds cash but records loan
                val annualRate = offer.interestRate ?: 0.08
                val term = offer.termWeeks ?: 52
                val interestFactor = 1.0 + (annualRate * (term / 52.0))
                val totalRepaymentAmount = offer.amount * interestFactor
                val weeklyRepay = totalRepaymentAmount / term

                val debtRecord = SimDebtRecord(
                    lenderName = offer.investorName,
                    principal = offer.amount,
                    remainingBalance = SimulationEngine.round(totalRepaymentAmount),
                    annualInterestRate = annualRate,
                    weeklyRepayment = SimulationEngine.round(weeklyRepay),
                    termWeeks = term,
                    weeksRemaining = term
                )

                return state.copy(
                    cash = SimulationEngine.round(state.cash + offer.amount),
                    debts = state.debts + debtRecord,
                    fundingOffers = remainingOffers,
                    newsLog = state.newsLog + "Debt Accepted: Received $${SimulationEngine.round(offer.amount)} loan from ${offer.investorName}. Weekly repayment of $${SimulationEngine.round(weeklyRepay)} begins next week."
                )
            }
            InvestmentType.SAFE_AGREEMENT, InvestmentType.CONVERTIBLE_NOTE -> {
                // No immediate dilution. Added to convertible instruments
                val safeOrNote = SimConvertibleInstrument(
                    investorName = offer.investorName,
                    type = offer.type,
                    principalAmount = offer.amount,
                    valuationCap = offer.valuationCap ?: (state.valuation * 1.5),
                    discountRate = offer.discountRate ?: 0.15,
                    interestRate = offer.interestRate ?: 0.0,
                    issuanceWeek = state.currentWeek
                )

                return state.copy(
                    cash = SimulationEngine.round(state.cash + offer.amount),
                    convertibleNotes = state.convertibleNotes + safeOrNote,
                    totalInvestmentRaised = SimulationEngine.round(state.totalInvestmentRaised + offer.amount),
                    fundingOffers = remainingOffers,
                    newsLog = state.newsLog + "Agreement Closed: Secured $${SimulationEngine.round(offer.amount)} via ${offer.type.name} from ${offer.investorName}. This will convert to shares during the next priced equity round."
                )
            }
            InvestmentType.ANGEL_ROUND, InvestmentType.SEED_ROUND, InvestmentType.SERIES_A, InvestmentType.SERIES_B, InvestmentType.SERIES_C -> {
                // priced Equity Round triggers CONVERSION of SAFEs and Convertible Notes!
                val postMoneyValuation = offer.impliedValuation ?: (state.valuation + offer.amount)
                val preMoneyValuation = postMoneyValuation - offer.amount

                // 1. Convert all pending convertible notes first
                var currentState = state
                val conversionEvents = mutableListOf<String>()
                var convertedSharesSum = 0L

                // We determine the priced round's share price based on total pre-conversion authorized shares and pre-money valuation
                var currentAuthorizedShares = currentState.totalAuthorizedShares
                val roundSharePrice = preMoneyValuation / currentAuthorizedShares

                val convertedShareholders = mutableListOf<Shareholder>()

                currentState.convertibleNotes.forEach { instrument ->
                    // Conversion share price is the minimum of (cap share price) or (discounted round share price)
                    val capSharePrice = instrument.valuationCap / currentAuthorizedShares
                    val discountedSharePrice = roundSharePrice * (1.0 - instrument.discountRate)
                    val conversionSharePrice = Math.min(capSharePrice, discountedSharePrice)

                    // Note: Convertible notes may have simple accrued interest
                    val simpleInterestAccrued = instrument.principalAmount * (instrument.interestRate * ((state.currentWeek - instrument.issuanceWeek) / 52.0))
                    val totalConversionValue = instrument.principalAmount + simpleInterestAccrued

                    val sharesToIssue = (totalConversionValue / conversionSharePrice).toLong()
                    convertedSharesSum += sharesToIssue

                    convertedShareholders.add(
                        Shareholder(
                            id = UUID.randomUUID().toString(),
                            name = instrument.investorName,
                            equityShares = sharesToIssue,
                            ownershipPercent = 0.0, // calculated later
                            isFounder = false
                        )
                    )

                    conversionEvents.add("CONVERTED: ${instrument.investorName}'s $${instrument.principalAmount} ${instrument.type.name} converted to $sharesToIssue shares at $${SimulationEngine.round(conversionSharePrice, 4)} per share.")
                }

                // Append converted shares to current shares
                currentAuthorizedShares += convertedSharesSum
                
                // Adjust cap table to reflect the converted shareholders (before adding the new priced investor)
                val postNoteCapTable = currentState.capTable.map { holder ->
                    holder.copy(ownershipPercent = SimulationEngine.round((holder.equityShares.toDouble() / currentAuthorizedShares) * 100.0, 4))
                } + convertedShareholders.map { holder ->
                    holder.copy(ownershipPercent = SimulationEngine.round((holder.equityShares.toDouble() / currentAuthorizedShares) * 100.0, 4))
                }

                // 2. Now process the priced equity investment itself
                val pricedEquityDilutionFraction = offer.amount / postMoneyValuation
                val newPricedSharesIssued = ((currentAuthorizedShares.toDouble() / (1.0 - pricedEquityDilutionFraction)) - currentAuthorizedShares).toLong()
                val finalTotalShares = currentAuthorizedShares + newPricedSharesIssued

                val pricedInvestor = Shareholder(
                    id = UUID.randomUUID().toString(),
                    name = offer.investorName,
                    equityShares = newPricedSharesIssued,
                    ownershipPercent = SimulationEngine.round((newPricedSharesIssued.toDouble() / finalTotalShares) * 100.0, 4),
                    isFounder = false
                )

                // Recalculate all cap table ownerships
                val finalCapTable = (postNoteCapTable + pricedInvestor).map { holder ->
                    val finalPct = SimulationEngine.round((holder.equityShares.toDouble() / finalTotalShares) * 100.0, 4)
                    holder.copy(ownershipPercent = finalPct)
                }

                // Add board member if required
                val boardMembers = if (offer.boardSeatRequired) {
                    val newBoardSeat = SimBoardMember(
                        name = "${offer.investorName} Representative",
                        representing = offer.type.name,
                        votingPower = SimulationEngine.round(pricedEquityDilutionFraction * 100.0),
                        supportiveness = offer.supportiveness
                    )
                    currentState.boardMembers + newBoardSeat
                } else {
                    currentState.boardMembers
                }

                return currentState.copy(
                    cash = SimulationEngine.round(currentState.cash + offer.amount),
                    valuation = postMoneyValuation,
                    capTable = finalCapTable,
                    totalAuthorizedShares = finalTotalShares,
                    convertibleNotes = emptyList(), // All converted!
                    boardMembers = boardMembers,
                    totalInvestmentRaised = SimulationEngine.round(currentState.totalInvestmentRaised + offer.amount),
                    fundingOffers = remainingOffers,
                    newsLog = currentState.newsLog + conversionEvents + "PRICED ROUND CLOSED: Raised $${SimulationEngine.round(offer.amount)} (${offer.type.displayName}) from ${offer.investorName} at $${SimulationEngine.round(postMoneyValuation)} post-money valuation."
                )
            }
            InvestmentType.IPO -> {
                // IPO is a massive exit! Translates cap table, adds huge cash
                val finalAuthorizedShares = state.totalAuthorizedShares + (state.totalAuthorizedShares * 0.25).toLong()
                val updatedCapTable = state.capTable.map { holder ->
                    val finalPct = SimulationEngine.round((holder.equityShares.toDouble() / finalAuthorizedShares) * 100.0, 4)
                    holder.copy(ownershipPercent = finalPct)
                }

                return state.copy(
                    cash = SimulationEngine.round(state.cash + offer.amount),
                    totalAuthorizedShares = finalAuthorizedShares,
                    capTable = updatedCapTable,
                    isIpoExited = true,
                    fundingOffers = emptyList(),
                    newsLog = state.newsLog + "INITIAL PUBLIC OFFERING (IPO): Congratulations! ${state.companyName} is now listed on Nasdaq. Raised $${SimulationEngine.round(offer.amount)} in public offering. Founders are now liquid tech titans!"
                )
            }
            InvestmentType.ACQUISITION -> {
                // Acquisition ends the game with a cash payout proportional to share ownerships
                val payouts = state.capTable.map { holder ->
                    val sharePayout = offer.amount * (holder.ownershipPercent / 100.0)
                    "${holder.name}: $${SimulationEngine.round(sharePayout)}"
                }

                return state.copy(
                    cash = SimulationEngine.round(offer.amount),
                    isAcquired = true,
                    fundingOffers = emptyList(),
                    newsLog = state.newsLog + "COMPANY ACQUIRED: ${state.companyName} has been acquired by ${offer.investorName} for $${SimulationEngine.round(offer.amount)}! Total cash payout completed: $payouts."
                )
            }
        }
    }

    /**
     * Submits a strategic proposal to the shareholders and Board of Directors.
     * Simulates voting based on shareholder alignment, interest, and state of company.
     */
    fun submitStrategicProposal(
        state: CompanyState,
        proposalType: String,
        title: String,
        description: String
    ): CompanyState {
        val proposedWeek = state.currentWeek
        
        // Simulating the shareholder and board member voting process
        // In modern startups, founder might hold voting control (if super-voting stock)
        // Here we do standard cap table proportional voting + board member approval influence
        var yesVotesWeight = 0.0
        var noVotesWeight = 0.0

        state.capTable.forEach { shareholder ->
            if (shareholder.isFounder) {
                // Founder always votes YES to their own proposal
                yesVotesWeight += shareholder.ownershipPercent
            } else {
                // VCs vote based on type of proposal and company conditions
                val isCompanyInCrisis = state.financials.runwayWeeks < 6.0
                val voteScore = when (proposalType) {
                    "ACQUISITION_OFFER" -> {
                        // VCs love acquisitions if valuation is high compared to raised capital
                        val totalRaised = state.totalInvestmentRaised
                        val ratio = state.valuation / (totalRaised + 1.0)
                        if (ratio > 3.0) 80 else if (isCompanyInCrisis) 90 else 40
                    }
                    "IPO_APPROVAL" -> {
                        if (state.valuation > 150_000_000.0) 85 else 20
                    }
                    "PIVOT_INDUSTRY" -> {
                        // Pivots are risky. VCs usually vote NO unless they are extremely supportive or company is failing
                        if (isCompanyInCrisis) 65 else 25
                    }
                    "REPLACE_CEO" -> {
                        // VCs vote to replace CEO if company is performing poorly
                        if (isCompanyInCrisis || state.averageEmployeeSatisfaction < 30.0) 90 else 10
                    }
                    else -> 50
                }

                // Adjust based on average board member supportiveness representing these classes
                val matchedBoard = state.boardMembers.find { it.name.contains(shareholder.name) }
                val boardBonus = ((matchedBoard?.supportiveness ?: 50.0) - 50.0) * 0.4
                val finalVoteScore = voteScore + boardBonus

                if ((1..100).random() < finalVoteScore) {
                    yesVotesWeight += shareholder.ownershipPercent
                } else {
                    noVotesWeight += shareholder.ownershipPercent
                }
            }
        }

        // Normalize weights to 100%
        val totalWeight = yesVotesWeight + noVotesWeight
        val yesPercent = if (totalWeight > 0.0) SimulationEngine.round((yesVotesWeight / totalWeight) * 100.0) else 100.0
        val noPercent = if (totalWeight > 0.0) SimulationEngine.round((noVotesWeight / totalWeight) * 100.0) else 0.0
        val isPassed = yesPercent > 50.0

        val resolutionNote = if (isPassed) {
            "Proposal Passed with ${yesPercent}% of shareholder approval."
        } else {
            "Proposal Rejected. Failed to secure majority vote (${yesPercent}% FOR, ${noPercent}% AGAINST)."
        }

        val completedProposal = SimShareholderProposal(
            title = title,
            description = description,
            proposalType = proposalType,
            proposedWeek = proposedWeek,
            votesForPercent = yesPercent,
            votesAgainstPercent = noPercent,
            isPassed = isPassed,
            isResolved = true,
            resolutionNote = resolutionNote
        )

        val updatedProposals = state.activeProposals + completedProposal
        val updatedLog = state.newsLog + "Governance Proposal [${title}]: $resolutionNote"

        // Execute consequences if proposal is passed
        var updatedState = state.copy(
            activeProposals = updatedProposals,
            newsLog = updatedLog
        )

        if (isPassed) {
            when (proposalType) {
                "PIVOT_INDUSTRY" -> {
                    // Restructuring cost of $25,000
                    val pivotCost = 25000.0
                    val currentSector = state.industry
                    val randomNewSector = SimulationIndustry.values().filter { it != currentSector }.random()
                    updatedState = updatedState.copy(
                        industry = randomNewSector,
                        cash = (updatedState.cash - pivotCost).coerceAtLeast(0.0),
                        newsLog = updatedState.newsLog + "VOTED PIVOT: Strategic restructuring approved. Company pivoted to ${randomNewSector.displayName} (Incurred $25,000 cost)."
                    )
                }
                "ACQUISITION_OFFER" -> {
                    updatedState = updatedState.copy(
                        isAcquired = true,
                        newsLog = updatedState.newsLog + "VOTED ACQUISITION: Board approved final buyout proposal. Payout distributions completed."
                    )
                }
                "REPLACE_CEO" -> {
                    // Founders are removed. Game over!
                    updatedState = updatedState.copy(
                        isBankruptcy = true, // marks game termination
                        newsLog = updatedState.newsLog + "VOTED OUT: Shareholder coup! You have been removed as CEO by the Board of Directors."
                    )
                }
            }
        }

        return updatedState
    }

    /**
     * Weekly updates for debts and convertible instruments.
     * Repays debts, accrues simple interest on convertible notes.
     */
    fun processWeeklyInvestmentDynamics(state: CompanyState): CompanyState {
        var currentCash = state.cash
        val updatedDebts = mutableListOf<SimDebtRecord>()
        val newsLogThisWeek = mutableListOf<String>()

        // 1. Process Debts
        state.debts.forEach { debt ->
            if (debt.weeksRemaining > 0) {
                val payment = debt.weeklyRepayment
                currentCash -= payment

                val updatedDebt = debt.copy(
                    remainingBalance = SimulationEngine.round((debt.remainingBalance - payment).coerceAtLeast(0.0)),
                    weeksRemaining = debt.weeksRemaining - 1
                )

                if (updatedDebt.weeksRemaining <= 0 || updatedDebt.remainingBalance <= 0.0) {
                    newsLogThisWeek.add("WEEKLY BILLS: Fully repaid loan from ${debt.lenderName}!")
                } else {
                    updatedDebts.add(updatedDebt)
                }
            }
        }

        // 2. Accrue Convertible Notes Interest
        val updatedNotes = state.convertibleNotes.map { note ->
            if (note.interestRate > 0.0) {
                val weeklyAccrued = note.principalAmount * (note.interestRate / 52.0)
                note.copy(accruedInterest = SimulationEngine.round(note.accruedInterest + weeklyAccrued))
            } else note
        }

        // 3. Keep Board supportiveness dynamic (decays slightly if low runway or cash, boosts if high satisfaction/revenue)
        val updatedBoardMembers = state.boardMembers.map { member ->
            val runway = state.financials.runwayWeeks
            var delta = if (runway < 6.0) -2.0 else 0.5
            if (state.lastWeekRevenue > 10000.0) delta += 1.0
            member.copy(supportiveness = (member.supportiveness + delta).coerceIn(10.0, 100.0))
        }

        return state.copy(
            cash = SimulationEngine.round(currentCash),
            debts = updatedDebts,
            convertibleNotes = updatedNotes,
            boardMembers = updatedBoardMembers,
            newsLog = state.newsLog + newsLogThisWeek
        )
    }
}
