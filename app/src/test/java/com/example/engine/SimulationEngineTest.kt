package com.example.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SimulationEngineTest {

    private lateinit var engine: SimulationEngine

    @Before
    fun setUp() {
        engine = SimulationEngine()
    }

    @Test
    fun testCompanyCreationAndInitialCapTable() {
        val companyState = engine.createCompany(
            companyName = "Quantum AI",
            ceoName = "Alice Dev",
            industry = SimulationIndustry.AI,
            businessModel = BusinessModel.SUBSCRIPTION,
            headquarters = Headquarters.SILICON_VALLEY,
            registrationState = RegistrationState.UNREGISTERED
        )

        assertEquals("Quantum AI", companyState.companyName)
        assertEquals("Alice Dev", companyState.ceoName)
        assertEquals(SimulationIndustry.AI, companyState.industry)
        assertEquals(BusinessModel.SUBSCRIPTION, companyState.businessModel)
        assertEquals(Headquarters.SILICON_VALLEY, companyState.headquarters)
        assertEquals(RegistrationState.UNREGISTERED, companyState.registrationState)

        // Validate Cap Table details
        assertEquals(2, companyState.capTable.size)
        val founder = companyState.capTable.find { it.name == "Alice Dev" }
        assertNotNull(founder)
        assertTrue(founder!!.isFounder)
        assertEquals(9_000_000L, founder.equityShares)
        assertEquals(90.0, founder.ownershipPercent, 0.01)

        val employeePool = companyState.capTable.find { it.name == "Employee Option Pool" }
        assertNotNull(employeePool)
        assertFalse(employeePool!!.isFounder)
        assertEquals(1_000_000L, employeePool.equityShares)
        assertEquals(10.0, employeePool.ownershipPercent, 0.01)

        // Cash flow is initialized minus registration fee
        assertEquals(100_000.0 - RegistrationState.UNREGISTERED.registrationFee, companyState.cash, 0.01)
    }

    @Test
    fun testRegisterCompany() {
        var companyState = engine.createCompany(
            companyName = "BioSensing",
            ceoName = "Bob Health",
            industry = SimulationIndustry.BIOTECH,
            businessModel = BusinessModel.ENTERPRISE,
            headquarters = Headquarters.LONDON,
            registrationState = RegistrationState.UNREGISTERED
        )

        // Attempting registration to Delaware C-Corp (fee: $2000)
        companyState = engine.registerCompany(companyState, RegistrationState.DELAWARE_C_CORP)

        assertEquals(RegistrationState.DELAWARE_C_CORP, companyState.registrationState)
        assertEquals(100_000.0 - 2000.0, companyState.cash, 0.01)
    }

    @Test
    fun testRegisterCompanyInsufficientFunds() {
        var companyState = engine.createCompany(
            companyName = "Empty Pocket",
            ceoName = "Charlie Poor",
            industry = SimulationIndustry.WEB3,
            businessModel = BusinessModel.AD_SUPPORTED,
            headquarters = Headquarters.AUSTIN,
            registrationState = RegistrationState.UNREGISTERED
        )

        // Force drain cash to $100
        companyState = companyState.copy(cash = 100.0)

        // Try registering to S-Corp ($1000 fee)
        companyState = engine.registerCompany(companyState, RegistrationState.S_CORP)

        // Registration state should remain Unregistered
        assertEquals(RegistrationState.UNREGISTERED, companyState.registrationState)
        assertEquals(100.0, companyState.cash, 0.01)
    }

    @Test
    fun testHiringAndFiringEmployees() {
        var companyState = engine.createCompany(
            companyName = "SaaSify",
            ceoName = "Dan Cloud",
            industry = SimulationIndustry.SAAS,
            businessModel = BusinessModel.SUBSCRIPTION,
            headquarters = Headquarters.AUSTIN,
            registrationState = RegistrationState.LLC
        )

        val programmer = SimEmployee(
            id = "prog-1",
            name = "Grace Hopper",
            role = "Engineer",
            skillLevel = "Senior",
            skillScore = 85,
            salary = 6000.0
        )

        companyState = engine.hireEmployee(companyState, programmer)
        assertEquals(1, companyState.employees.size)
        assertEquals("Grace Hopper", companyState.employees[0].name)

        // Fire Programmer
        companyState = engine.fireEmployee(companyState, "prog-1")
        assertEquals(0, companyState.employees.size)
    }

    @Test
    fun testEquityIssuanceAndVCInvestmentDilution() {
        val companyState = engine.createCompany(
            companyName = "Web3 Venture",
            ceoName = "Eve Token",
            industry = SimulationIndustry.WEB3,
            businessModel = BusinessModel.TRANSACTIONAL,
            headquarters = Headquarters.REMOTE,
            registrationState = RegistrationState.DELAWARE_C_CORP
        )

        // $250,000 raise at $2,500,000 post-money valuation (10% dilution)
        val postFundingState = engine.issueShares(
            state = companyState,
            investorName = "Andreesen",
            investmentAmount = 250_000.0,
            impliedValuation = 2_500_000.0
        )

        assertEquals(100_000.0 - 2000.0 + 250_000.0, postFundingState.cash, 0.01)
        assertEquals(2_500_000.0, postFundingState.valuation, 0.01)

        // Total authorized shares should have increased
        assertTrue(postFundingState.totalAuthorizedShares > companyState.totalAuthorizedShares)

        // Validate VC ownership is exactly 10%
        val vcShareholder = postFundingState.capTable.find { it.name == "Andreesen" }
        assertNotNull(vcShareholder)
        assertEquals(10.0, vcShareholder!!.ownershipPercent, 0.01)

        // Founder's ownership should have diluted from 90% to 81%
        val founderShareholder = postFundingState.capTable.find { it.name == "Eve Token" }
        assertNotNull(founderShareholder)
        assertEquals(81.0, founderShareholder!!.ownershipPercent, 0.01)
    }

    @Test
    fun testWeeklyDeterministicSimulationStep() {
        var companyState = engine.createCompany(
            companyName = "SaaS Corp",
            ceoName = "Steve Ballmer",
            industry = SimulationIndustry.SAAS,
            businessModel = BusinessModel.SUBSCRIPTION,
            headquarters = Headquarters.AUSTIN,
            registrationState = RegistrationState.DELAWARE_C_CORP
        )

        // Add 1 Engineer
        val dev = SimEmployee(
            id = "dev-1",
            name = "Ada Lovelace",
            role = "Engineer",
            skillLevel = "Senior",
            skillScore = 80,
            salary = 5000.0
        )
        companyState = engine.hireEmployee(companyState, dev)

        // Run simulation for 1 week
        val nextWeekState = engine.simulateWeek(companyState)

        assertEquals(2, nextWeekState.currentWeek)
        assertTrue(nextWeekState.cash > 0.0)

        // Verify feature progress was incremented by engineering power
        val initialProduct = companyState.products[0]
        val updatedProduct = nextWeekState.products[0]
        assertTrue(updatedProduct.features.any { it.progress > 0.0 })
    }
}
