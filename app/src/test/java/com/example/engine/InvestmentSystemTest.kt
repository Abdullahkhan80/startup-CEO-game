package com.example.engine

import org.junit.Assert.*
import org.junit.Test

class InvestmentSystemTest {

    private val engine = SimulationEngine()

    private fun createInitialState(): CompanyState {
        return engine.createCompany(
            companyName = "Apex Labs",
            ceoName = "Alice",
            industry = SimulationIndustry.AI,
            businessModel = BusinessModel.SUBSCRIPTION,
            headquarters = Headquarters.SILICON_VALLEY,
            registrationState = RegistrationState.DELAWARE_C_CORP
        )
    }

    @Test
    fun testGenerateOffers() {
        val state = createInitialState()
        val offers = InvestmentSystem.generateFundingOffers(state)

        // There should be offers for early stage
        assertTrue(offers.isNotEmpty())
        assertTrue(offers.any { it.type == InvestmentType.SAFE_AGREEMENT })
        assertTrue(offers.any { it.type == InvestmentType.CONVERTIBLE_NOTE })
        assertTrue(offers.any { it.type == InvestmentType.DEBT_FINANCING })
    }

    @Test
    fun testDebtFinancingAcceptanceAndWeeklyRepayment() {
        var state = createInitialState()
        val offers = InvestmentSystem.generateFundingOffers(state)
        val debtOffer = offers.first { it.type == InvestmentType.DEBT_FINANCING }

        // Store the original cash before accepting debt
        val initialCash = state.cash

        // Accept the debt
        state = state.copy(fundingOffers = offers)
        val acceptedState = InvestmentSystem.acceptFundingOffer(state, debtOffer.id)

        // Cash should increase by the debt amount
        assertEquals(initialCash + debtOffer.amount, acceptedState.cash, 0.01)
        assertEquals(1, acceptedState.debts.size)

        val debtRecord = acceptedState.debts[0]
        assertEquals(debtOffer.amount, debtRecord.principal, 0.01)
        assertTrue(debtRecord.weeklyRepayment > 0.0)

        // Run a weekly simulation cycle to verify repayment is processed
        val nextState = InvestmentSystem.processWeeklyInvestmentDynamics(acceptedState)
        assertEquals(acceptedState.cash - debtRecord.weeklyRepayment, nextState.cash, 0.01)
        assertEquals(debtRecord.weeksRemaining - 1, nextState.debts[0].weeksRemaining)
    }

    @Test
    fun testSAFEAndConvertibleNoteConversion() {
        var state = createInitialState()
        
        // Let's manually issue a SAFE agreement of $100,000 with a $1,000,000 Valuation Cap
        val safe = SimConvertibleInstrument(
            investorName = "Y Combinator",
            type = InvestmentType.SAFE_AGREEMENT,
            principalAmount = 100_000.0,
            valuationCap = 1_000_000.0,
            discountRate = 0.20,
            issuanceWeek = 1
        )

        state = state.copy(convertibleNotes = listOf(safe))

        // Ensure we have 10,000,000 authorized shares (as per CompanyState defaults)
        assertEquals(10_000_000L, state.totalAuthorizedShares)

        // Now, let's close a Seed priced equity round: raising $200,000 at a $2,000,000 post-money valuation
        // This implies pre-money is $1,800,000.
        val seedOffer = SimInvestmentOffer(
            investorName = "Sequoia",
            type = InvestmentType.SEED_ROUND,
            amount = 200_000.0,
            impliedValuation = 2_000_000.0,
            expirationWeek = 10
        )

        state = state.copy(fundingOffers = listOf(seedOffer))

        // Accept seed round, which triggers SAFE conversion
        val postSeedState = InvestmentSystem.acceptFundingOffer(state, seedOffer.id)

        // SAFE should be converted and removed from convertibleNotes list
        assertTrue(postSeedState.convertibleNotes.isEmpty())

        // Let's calculate mathematically:
        // Pre-money is $1.8M. Total shares before conversion = 10M.
        // Standard round share price = $1.8M / 10M = $0.18 per share.
        // Discounted share price = $0.18 * (1.0 - 0.20) = $0.144 per share.
        // Valuation cap share price = $1.0M / 10M = $0.10 per share.
        // Since $0.10 is lower than $0.144, the SAFE converts at $0.10 per share (the valuation cap!).
        // Shares issued to SAFE investor = $100,000 / $0.10 = 1,000,000 shares.
        // After SAFE conversion, total shares = 10M + 1M = 11M shares.
        // Now, Sequoia invests $200,000 at $2,000,000 post-money valuation.
        // Sequoia's dilution = $200k / $2M = 10% dilution of post-money.
        // Shares issued to Sequoia = (11M / (1.0 - 0.10)) - 11M = 1.222M shares.
        // Final total shares = 11M + 1.222M = 12,222,222 shares.

        val safeShareholder = postSeedState.capTable.find { it.name == "Y Combinator" }
        assertNotNull(safeShareholder)
        assertEquals(1_000_000L, safeShareholder!!.equityShares)

        // Ownership percent: 1,000,000 / 12,222,222 = ~8.18%
        assertEquals(8.18, safeShareholder.ownershipPercent, 0.1)

        val seedShareholder = postSeedState.capTable.find { it.name == "Sequoia" }
        assertNotNull(seedShareholder)
        assertEquals(10.0, seedShareholder!!.ownershipPercent, 0.1)
    }

    @Test
    fun testInvestorNegotiations() {
        var state = createInitialState()
        val offer = SimInvestmentOffer(
            investorName = "Peter Thiel",
            type = InvestmentType.ANGEL_ROUND,
            amount = 100_000.0,
            impliedValuation = 1_000_000.0,
            expirationWeek = 5
        )
        state = state.copy(fundingOffers = listOf(offer), pitchDeckQuality = 80.0)

        // Negotiate aggressive valuation
        val negotiatedState = InvestmentSystem.negotiateOffer(state, offer.id, "AGGRESSIVE_VALUATION")

        // Offer is either removed (insulted) or has increased valuation
        if (negotiatedState.fundingOffers.isEmpty()) {
            // Insulted walked away
            assertTrue(negotiatedState.newsLog.last().contains("withdrew"))
        } else {
            // Successfully increased
            val updatedOffer = negotiatedState.fundingOffers[0]
            assertTrue(updatedOffer.impliedValuation!! > 1_000_000.0)
        }
    }

    @Test
    fun testBoardVotingOnStrategicProposal() {
        var state = createInitialState()
        
        // Add a board member
        val boardMember = SimBoardMember(
            name = "John Doerr",
            representing = "VC_SEED",
            votingPower = 30.0,
            supportiveness = 80.0
        )
        state = state.copy(boardMembers = listOf(boardMember))

        // Create a shareholder proposal to pivot industry
        val proposalState = InvestmentSystem.submitStrategicProposal(
            state = state,
            proposalType = "PIVOT_INDUSTRY",
            title = "Pivot to Fintech",
            description = "Let's explore financial services."
        )

        val completed = proposalState.activeProposals.last()
        assertTrue(completed.isResolved)
        assertNotNull(completed.resolutionNote)
    }
}
