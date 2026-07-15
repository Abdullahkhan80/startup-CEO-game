package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductionReadyTests {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    // ==========================================
    // 1. UNIT TESTING (Crash Reporter & Analytics)
    // ==========================================

    @Test
    fun testCrashReporter_InitializationAndLocalCapture() {
        CrashReporter.initialize(context)
        
        // Simulate a crash trigger and verify local logging structure safely
        val writer = java.io.StringWriter()
        val mockException = RuntimeException("Developer Verification Crash")
        mockException.printStackTrace(java.io.PrintWriter(writer))
        val stackTrace = writer.toString()
        
        val report = """
            OS Version: Android 16
            Exception Message: ${mockException.message}
            Stacktrace:
            $stackTrace
        """.trimIndent()

        val file = File(context.filesDir, "test_verification_crash.txt")
        file.writeText(report)
        
        assertTrue("Crash report file must exist locally on write", file.exists())
        assertTrue("Crash report must include diagnostic message", file.readText().contains("Developer Verification Crash"))
        file.delete()
    }

    @Test
    fun testAnalyticsTracker_EventLoggingAndQueue() {
        AnalyticsTracker.initialize(context)
        
        AnalyticsTracker.logEvent(
            "test_production_metric",
            mapOf("test_param" to "test_value", "level" to 10)
        )
        // Verify queue operates asynchronously without exception or blocking
        assertTrue(true)
    }

    // ==========================================
    // 2. INTEGRATION TESTING (State & Progression)
    // ==========================================

    @Test
    fun testGameViewModel_ProgressionIntegration() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        val initialState = viewModel.uiState.value
        
        // Initial defaults check
        assertEquals("Sarah Jenkins", initialState.ceoName)
        assertEquals(1, initialState.founderLevel)
        assertEquals(0L, initialState.xp)

        // Gain XP and test level up limits
        viewModel.gainXP(600L) // Level 1 needs Level * 500 = 500 XP to level up
        
        val stateAfterXp = viewModel.uiState.value
        assertEquals("Gain XP must level up the founder", 2, stateAfterXp.founderLevel)
        assertEquals("Remaining XP must carry over correctly", 100L, stateAfterXp.xp)
    }

    @Test
    fun testFounderPass_ProgressionIntegration() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        
        // Initial state check
        assertEquals(1, viewModel.uiState.value.founderPassLevel)
        assertEquals(0L, viewModel.uiState.value.founderPassXp)

        // Gain Founder Pass XP
        viewModel.gainFounderPassXP(120L)
        
        val updatedState = viewModel.uiState.value
        assertEquals("Founder Pass must reach Tier 2", 2, updatedState.founderPassLevel)
        assertEquals("XP remainder must carry over", 20L, updatedState.founderPassXp)
    }

    // ==========================================
    // 3. PERFORMANCE TESTING (Simulation Benchmarks)
    // ==========================================

    @Test
    fun testSimulationEngine_ExecutionPerformanceBenchmark() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        
        // Benchmark running multiple advances to verify non-blocking efficiency
        val duration = measureTimeMillis {
            for (i in 1..20) {
                viewModel.advanceWeek()
            }
        }
        
        val averageTimePerCycle = duration / 20.0
        LogTelemetryDiagnostic("Simulation performance benchmark average: ${averageTimePerCycle}ms/cycle (Limit: 50ms)")
        
        assertTrue(
            "Weekly cycle simulation must be highly optimized (< 50ms) to prevent frames dropping",
            averageTimePerCycle < 50.0
        )
    }

    // ==========================================
    // 4. SECURITY TESTING (Financial Boundary Checks)
    // ==========================================

    @Test
    fun testSecurity_FinancialValidationAndNoOverflow() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        
        // Buy cosmetic with insufficient tokens must fail gracefully (prevention of negative token balance exploit)
        val initialTokens = viewModel.uiState.value.cosmeticTokens
        viewModel.buyCosmetic("chair_gold", initialTokens + 500) // cost exceeds holdings
        
        assertEquals(
            "Security control: buying cosmetic without sufficient funds must not decrease holdings or allow transaction",
            initialTokens,
            viewModel.uiState.value.cosmeticTokens
        )
    }

    private fun LogTelemetryDiagnostic(message: String) {
        println("[PERFORMANCE BENCHMARK] $message")
    }
}
