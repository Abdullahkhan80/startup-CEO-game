package com.example.engine

import android.util.Log
import androidx.annotation.Keep
import com.example.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Keep
object AIPoweredMarketEngine {

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Generates initial competitor companies based on the selected sector.
     */
    fun generateInitialCompetitors(sector: Sector): List<CompetitorCompany> {
        return when (sector) {
            Sector.AI -> listOf(
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "OmniCognitive Labs",
                    description = "Aggressive enterprise AI platform backed by top-tier tech giants.",
                    marketShare = 32.0,
                    valuation = 1_500_000.0,
                    productQuality = 42.0,
                    products = listOf("OmniV1 Chat", "Cognitive API")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "AetherLabs AI",
                    description = "Agile, open-source AI community developing lightweight neural pipelines.",
                    marketShare = 22.0,
                    valuation = 600_000.0,
                    productQuality = 30.0,
                    products = listOf("Aether-7B Model")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "NeuralSynth",
                    description = "Niche workflow developer specializing in custom autonomous sales agents.",
                    marketShare = 16.0,
                    valuation = 450_000.0,
                    productQuality = 24.0,
                    products = listOf("SynthAgent Pro")
                )
            )
            Sector.SAAS -> listOf(
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "CloudGrid Inc.",
                    description = "The incumbent workspace giant with a full suite of cloud operations.",
                    marketShare = 35.0,
                    valuation = 2_000_000.0,
                    productQuality = 45.0,
                    products = listOf("Grid Suite v4")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "FlowState",
                    description = "Highly polished, beautiful project and time tracking app popular with designers.",
                    marketShare = 20.0,
                    valuation = 800_000.0,
                    productQuality = 32.0,
                    products = listOf("Flow Boards")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "DocuStream",
                    description = "Low-code collaborative document workflow and approval engine.",
                    marketShare = 15.0,
                    valuation = 500_000.0,
                    productQuality = 25.0,
                    products = listOf("DocuBuilder")
                )
            )
            Sector.BIOTECH -> listOf(
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "TheraGen Systems",
                    description = "Leading gene-editing pioneer researching cures for rare hereditary ailments.",
                    marketShare = 30.0,
                    valuation = 2_500_000.0,
                    productQuality = 48.0,
                    products = listOf("TG-101 Therapy")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "BioPulse Analytics",
                    description = "Healthtech wearable sensors integrated with algorithmic diagnostics.",
                    marketShare = 24.0,
                    valuation = 900_000.0,
                    productQuality = 34.0,
                    products = listOf("Pulse OS", "BioBand v1")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "NanoMed Corp",
                    description = "Targeted cancer therapeutics using bio-synthetic molecular capsules.",
                    marketShare = 16.0,
                    valuation = 600_000.0,
                    productQuality = 26.0,
                    products = listOf("NanoCarrier Alpha")
                )
            )
            Sector.FINTECH -> listOf(
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "ApexPay Networks",
                    description = "Universal payment ledger API offering friction-free micro-payments.",
                    marketShare = 34.0,
                    valuation = 1_800_000.0,
                    productQuality = 40.0,
                    products = listOf("Apex Gateway")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "LedgerFlow",
                    description = "Automated corporate expense and bank ledger reconciliation platform.",
                    marketShare = 22.0,
                    valuation = 750_000.0,
                    productQuality = 31.0,
                    products = listOf("Flow Ledger")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "MicroKobo",
                    description = "Emerging peer-to-peer microfinance platform scaling fast in Latin America.",
                    marketShare = 14.0,
                    valuation = 400_000.0,
                    productQuality = 20.0,
                    products = listOf("Kobo Lending App")
                )
            )
            Sector.WEB3 -> listOf(
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "EtherScale Core",
                    description = "Layer-2 scaling network offering high throughput and low gas fees.",
                    marketShare = 32.0,
                    valuation = 1_200_000.0,
                    productQuality = 38.0,
                    products = listOf("EtherScale Rollup")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "DafiYield Protocols",
                    description = "Smart contract vault automatically indexing decentralized finance yield farms.",
                    marketShare = 20.0,
                    valuation = 650_000.0,
                    productQuality = 28.0,
                    products = listOf("YieldVault Alpha")
                ),
                CompetitorCompany(
                    id = UUID.randomUUID().toString(),
                    name = "MintState Realty",
                    description = "Regulated portal tokenizing fractionalized retail and commercial real estate.",
                    marketShare = 18.0,
                    valuation = 500_000.0,
                    productQuality = 22.0,
                    products = listOf("MintState Portal")
                )
            )
        }
    }

    /**
     * Advances competitor companies, trends, and applies modifiers weekly.
     */
    fun advanceWeeklyMarket(state: GamePrefs.GameState): GamePrefs.GameState {
        val nextWeek = state.week + 1
        val newsFeedThisWeek = mutableListOf<NewsFeedItem>()

        // 1. Check if competitors are empty (first run fallback)
        var competitorsList = state.competitors.ifEmpty { generateInitialCompetitors(state.sector) }

        // 2. Weekly growth for competitor companies
        competitorsList = competitorsList.map { competitor ->
            val qualityGrowth = Random.nextDouble(0.15, 0.45)
            val updatedQuality = (competitor.productQuality + qualityGrowth).coerceAtMost(100.0)
            
            // Valuation tracks quality and market share
            val baseValVal = competitor.valuation * (1.0 + (qualityGrowth / 100.0) + (Random.nextDouble(-0.02, 0.05)))
            val updatedValuation = SimulationEngine.round(baseValVal.coerceAtLeast(100_000.0))

            // Dynamic Product Launch check (5% chance if quality is a multiple of ~15)
            val launchRoll = Random.nextFloat()
            val hasLaunchedNew = launchRoll < 0.05 && competitor.productQuality > 20.0
            
            val updatedProducts = if (hasLaunchedNew) {
                val productName = generateDynamicProductName(competitor.name, state.sector, competitor.products.size + 1)
                newsFeedThisWeek.add(
                    NewsFeedItem(
                        timestamp = "Week $nextWeek",
                        headline = "COMPETITOR LAUNCH: ${competitor.name} officially released '$productName' to the market! Dynamic competitive heat increases.",
                        source = "TechCrunch",
                        isAlert = true
                    )
                )
                competitor.products + productName
            } else {
                competitor.products
            }

            // Market share adjustments
            val shareChange = if (hasLaunchedNew) Random.nextDouble(2.0, 5.0) else Random.nextDouble(-0.5, 0.8)
            val updatedShare = (competitor.marketShare + shareChange).coerceIn(2.0, 60.0)

            competitor.copy(
                productQuality = updatedQuality,
                valuation = updatedValuation,
                products = updatedProducts,
                marketShare = updatedShare
            )
        }

        // Normalize competitor market shares so they don't exceed 90% aggregate
        val totalCompetitorShare = competitorsList.sumOf { it.marketShare }
        if (totalCompetitorShare > 90.0) {
            val scaleFactor = 90.0 / totalCompetitorShare
            competitorsList = competitorsList.map {
                it.copy(marketShare = SimulationEngine.round(it.marketShare * scaleFactor, 1))
            }
        }

        // 3. Tick active Trends and Events
        val activeTrends = state.marketTrends.map {
            it.copy(durationWeeks = it.durationWeeks - 1)
        }.filter { it.durationWeeks > 0 }

        val activeEvents = state.marketEvents.map {
            it.copy(effectDurationWeeks = it.effectDurationWeeks - 1)
        }.filter { it.effectDurationWeeks > 0 }

        // 4. Clean up notifications about expired events
        state.marketEvents.forEach { ev ->
            if (ev.effectDurationWeeks == 1) {
                newsFeedThisWeek.add(
                    NewsFeedItem(
                        timestamp = "Week $nextWeek",
                        headline = "MARKET UPDATE: The impact of '${ev.title}' has subsided. Market conditions returning to baseline.",
                        source = "System"
                    )
                )
            }
        }

        // 5. Randomly introduce a Trend if none are active (10% chance)
        var finalTrends = activeTrends.toMutableList()
        if (finalTrends.isEmpty() && Random.nextFloat() < 0.12) {
            val newTrend = generateBalancedProceduralTrend(state.sector)
            finalTrends.add(newTrend)
            newsFeedThisWeek.add(
                NewsFeedItem(
                    timestamp = "Week $nextWeek",
                    headline = "NEW TREND DETECTED: '${newTrend.name}' - ${newTrend.description}",
                    source = "TechCrunch",
                    isAlert = false
                )
            )
        }

        return state.copy(
            competitors = competitorsList,
            marketTrends = finalTrends,
            marketEvents = activeEvents,
            newsFeed = (state.newsFeed + newsFeedThisWeek).takeLast(40)
        )
    }

    /**
     * Generates a balanced, fully procedural market event when AI call fails or is not activated.
     */
    fun generateBalancedProceduralEvent(sector: Sector, week: Int): MarketEvent {
        val types = MarketEventType.values()
        val chosenType = types.random()
        val uuid = UUID.randomUUID().toString()

        return when (chosenType) {
            MarketEventType.ECONOMIC_RECESSION -> MarketEvent(
                id = uuid,
                title = "Macroeconomic Credit Squeeze",
                description = "Inflation spikes force central banks to raise interest rates, making funding scarce and tightening consumer budgets worldwide.",
                type = chosenType,
                severity = "CRITICAL",
                effectDurationWeeks = Random.nextInt(4, 9),
                cashMultiplier = 0.95,
                userGrowthMultiplier = 0.60,
                valuationMultiplier = 0.70,
                salaryMultiplier = 1.0,
                headline = "MARKET ALARM: Global Tech Stock Index tumbles as Federal Reserve signals persistent high interest rates."
            )
            MarketEventType.GOVERNMENT_REGULATION -> MarketEvent(
                id = uuid,
                title = "Strict Data Compliance Mandates",
                description = "New international privacy regulation frameworks impose heavy compliance auditing and system architecture reporting.",
                type = chosenType,
                severity = "WARNING",
                effectDurationWeeks = Random.nextInt(3, 7),
                cashMultiplier = 1.0,
                userGrowthMultiplier = 0.85,
                valuationMultiplier = 0.90,
                salaryMultiplier = 1.15, // Audit legal costs
                headline = "REGULATION WATCH: SEC and EU commission announce sweeping digital compliance checks on emerging platforms."
            )
            MarketEventType.TECH_BREAKTHROUGH -> MarketEvent(
                id = uuid,
                title = "Quantum Compiling Leap",
                description = "Researchers open-source a highly efficient algorithmic acceleration platform, dramatically lowering compilation and processing latency.",
                type = chosenType,
                severity = "INFO",
                effectDurationWeeks = Random.nextInt(3, 6),
                cashMultiplier = 1.0,
                userGrowthMultiplier = 1.40,
                valuationMultiplier = 1.20,
                salaryMultiplier = 1.0,
                headline = "TECH SENSATION: Breakthrough compilation architecture optimizes remote server latency by up to 300%."
            )
            MarketEventType.CYBER_ATTACK -> MarketEvent(
                id = uuid,
                title = "Distributed Router Poisoning",
                description = "Major DNS routers suffer coordinated poisoning campaigns, degrading network speed and consumer session security.",
                type = chosenType,
                severity = "CRITICAL",
                effectDurationWeeks = Random.nextInt(2, 5),
                cashMultiplier = 0.90, // Recovery costs
                userGrowthMultiplier = 0.70,
                valuationMultiplier = 0.80,
                salaryMultiplier = 1.0,
                headline = "SECURITY BREAK: Massive zero-day exploit targets core transit routing, affecting major digital systems."
            )
            MarketEventType.ACQUISITION -> MarketEvent(
                id = uuid,
                title = "Mega Merger Consolidated",
                description = "A massive legacy operator acquires a fast-growing challenger for a premium multiple, triggering interest in similar products.",
                type = chosenType,
                severity = "INFO",
                effectDurationWeeks = Random.nextInt(3, 6),
                cashMultiplier = 1.0,
                userGrowthMultiplier = 1.15,
                valuationMultiplier = 1.35, // Market valuation multiplier boost!
                salaryMultiplier = 1.0,
                headline = "CONSOLIDATION: Cisco acquires emerging cloud-native network startup for a record 18x revenue multiple."
            )
            MarketEventType.LAWSUIT -> MarketEvent(
                id = uuid,
                title = "Patent Routing Lawsuit",
                description = "A patent-troll conglomerate initiates claims against several key players in our industry, claiming infringement on API structures.",
                type = chosenType,
                severity = "WARNING",
                effectDurationWeeks = Random.nextInt(3, 6),
                cashMultiplier = 0.96,
                userGrowthMultiplier = 1.0,
                valuationMultiplier = 0.85,
                salaryMultiplier = 1.10, // legal consulting fees
                headline = "LEGAL RISK: Obscure patent holder launches multi-million dollar copyright lawsuits against tech operators."
            )
            MarketEventType.TALENT_SHORTAGE -> MarketEvent(
                id = uuid,
                title = "Specialized Developer Drought",
                description = "An intense bidding war for top tech engineering talent drives up salary expectations and slows onboarding timelines.",
                type = chosenType,
                severity = "WARNING",
                effectDurationWeeks = Random.nextInt(4, 8),
                cashMultiplier = 1.0,
                userGrowthMultiplier = 0.90,
                valuationMultiplier = 1.0,
                salaryMultiplier = 1.25, // Payroll inflation
                headline = "TALENT WAR: Silicon Valley talent recruiters warn of critical engineering and PM labor deficits."
            )
            MarketEventType.MARKET_CRASH -> MarketEvent(
                id = uuid,
                title = "Sudden Growth Index Correction",
                description = "Sustained high multiples trigger sudden liquidity exits, causing major tech stock valuations to plummet and boards to seek runway safety.",
                type = chosenType,
                severity = "CRITICAL",
                effectDurationWeeks = Random.nextInt(3, 7),
                cashMultiplier = 1.0,
                userGrowthMultiplier = 0.75,
                valuationMultiplier = 0.60, // Deep valuation cut
                salaryMultiplier = 0.90,
                headline = "BLACK MONDAY: Nasdaq Composite plunges 8.5% in record selloff as hyper-growth sentiment cooling spreads."
            )
            MarketEventType.CONSUMER_BEHAVIOR -> MarketEvent(
                id = uuid,
                title = "B2B Enterprise Integration Craze",
                description = "Enterprise customers heavily favor tightly integrated modular platforms over disparate specialized tools, accelerating platform consolidation.",
                type = chosenType,
                severity = "INFO",
                effectDurationWeeks = Random.nextInt(4, 7),
                cashMultiplier = 1.0,
                userGrowthMultiplier = if (sector == Sector.SAAS) 1.50 else 0.80,
                valuationMultiplier = 1.10,
                salaryMultiplier = 1.0,
                headline = "TRENDING: Corporate IT procurement surveys reveal strong preference for centralized enterprise licensing models."
            )
            MarketEventType.BREAKING_NEWS -> MarketEvent(
                id = uuid,
                title = "Pioneering Cloud Framework Launch",
                description = "A standard-setting developer coalition launches a game-changing reactive server framework that is completely free to deploy.",
                type = chosenType,
                severity = "INFO",
                effectDurationWeeks = Random.nextInt(2, 5),
                cashMultiplier = 1.0,
                userGrowthMultiplier = 1.20,
                valuationMultiplier = 1.10,
                salaryMultiplier = 1.0,
                headline = "DEV NEWS: Revolutionary micro-server architectural standard launched, accelerating web deployments."
            )
        }
    }

    /**
     * Generates initial or weekly competitor products dynamically.
     */
    private fun generateDynamicProductName(companyName: String, sector: Sector, index: Int): String {
        val root = companyName.split(" ").firstOrNull() ?: "Core"
        return when (sector) {
            Sector.AI -> listOf("Brain v$index", "Agent Engine $index.0", "Cognitive Mesh", "FlowMind Pro", "OmniNLP").random()
            Sector.SAAS -> listOf("Workspace Pro", "Sync Boards v$index", "DocuLink $index", "TeamGrid", "FlowSuite").random()
            Sector.BIOTECH -> listOf("BioSynth v$index", "CellMap $index.0", "GeneScan Max", "NanoCure", "PulseEngine").random()
            Sector.FINTECH -> listOf("PayGateway $index", "LedgerPro", "CryptoScale $index.0", "KoboPay", "VaultOS").random()
            Sector.WEB3 -> listOf("Rollup v$index", "YieldIndexer", "SmartWallet", "MintPortal $index.0", "ChainOS").random()
        }
    }

    /**
     * Generates a balanced procedural trend.
     */
    private fun generateBalancedProceduralTrend(sector: Sector): MarketTrend {
        val trends = listOf(
            Triple("AI Automation Rush", "Businesses are desperately automating support and logic pipelines.", Sector.AI),
            Triple("Privacy Defense Wave", "Consumers heavily prioritize end-to-end encryption and self-custody of metrics.", Sector.SAAS),
            Triple("DeFi Yield Renaissance", "Crypto liquidity pools experience massive inflows, lifting transaction counts.", Sector.WEB3),
            Triple("No-Code Platform Craze", "Non-technical teams bypass engineers to build SaaS products with drag-and-drop tools.", Sector.SAAS),
            Triple("Gene-Editing IPO Bubble", "A surge of clinical approvals triggers investor euphoria in genetic medicine.", Sector.BIOTECH),
            Triple("Instant Settlement Demands", "Consumers refuse traditional 3-day clearing, causing banks to adopt local FinTech integrations.", Sector.FINTECH)
        )
        val chosen = trends.random()
        return MarketTrend(
            id = UUID.randomUUID().toString(),
            name = chosen.first,
            description = chosen.second,
            trendType = if (chosen.third == sector) "BULLISH" else "NEUTRAL",
            durationWeeks = Random.nextInt(6, 12),
            sectorImpact = chosen.third.displayName
        )
    }

    /**
     * Call the Gemini AI API to generate a fully custom, balanced, and context-aware business event.
     * Keeps the simulation balanced by sanitizing numeric fields and falling back to procedural generation.
     */
    suspend fun generateAICustomEvent(state: GamePrefs.GameState): MarketEvent = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.i("AIPoweredMarketEngine", "Gemini API key is not configured, using balanced procedural generator.")
            return@withContext generateBalancedProceduralEvent(state.sector, state.week)
        }

        val prompt = buildString {
            append("You are the AI Market Simulation Engine for a highly realistic startup simulator game.\n")
            append("Please generate a realistic, context-aware, balanced business or market news event for week ${state.week} in the industry sector: ${state.sector.displayName}.\n")
            append("The player's startup is named '${state.companyName}' with cash reserves of ${state.cash} and total valuation of ${state.valuation}.\n")
            append("Select exactly one event type from the following: [ECONOMIC_RECESSION, GOVERNMENT_REGULATION, TECH_BREAKTHROUGH, CYBER_ATTACK, ACQUISITION, LAWSUIT, TALENT_SHORTAGE, MARKET_CRASH, CONSUMER_BEHAVIOR, BREAKING_NEWS].\n")
            append("You MUST return strictly valid JSON matching this exact structure and schema. No other text, formatting, or wrapping outside the JSON block:\n")
            append("{\n")
            append("  \"title\": \"A short, immersive event title (e.g., 'Cloud Infrastructure Fire' or 'AI Regulation Bill')\",\n")
            append("  \"description\": \"A professional, detailed description outlining the business consequences, user concerns, and operational adjustments (maximum 180 characters)\",\n")
            append("  \"type\": \"One of the specific types listed above\",\n")
            append("  \"severity\": \"One of: 'INFO', 'WARNING', 'CRITICAL'\",\n")
            append("  \"effectDurationWeeks\": An integer between 2 and 6,\n")
            append("  \"cashMultiplier\": A value between 0.90 and 1.0 (effects cash flows, 1.0 is neutral)\",\n")
            append("  \"userGrowthMultiplier\": A value between 0.65 and 1.45 (effects active user onboarding rate)\",\n")
            append("  \"valuationMultiplier\": A value between 0.60 and 1.35 (effects company stock value)\",\n")
            append("  \"salaryMultiplier\": A value between 0.90 and 1.25 (effects employee wages and hiring costs)\",\n")
            append("  \"headline\": \"A dynamic, dramatic, news-ticker style headline (maximum 100 characters)\"\n")
            append("}\n")
        }

        // Construct request following Option B (Direct REST API) from SKILL.md
        val requestJson = JSONObject()
        val contentsArray = org.json.JSONArray()
        val contentObject = JSONObject()
        val partsArray = org.json.JSONArray()
        val partObject = JSONObject()
        
        partObject.put("text", prompt)
        partsArray.put(partObject)
        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        requestJson.put("contents", contentsArray)

        // Force JSON response formatting in generationConfig
        val generationConfig = JSONObject()
        val responseFormat = JSONObject()
        responseFormat.put("mimeType", "application/json")
        generationConfig.put("responseFormat", responseFormat)
        generationConfig.put("temperature", 0.8)
        requestJson.put("generationConfig", generationConfig)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("AIPoweredMarketEngine", "Gemini API request failed with status: ${response.code}, falling back.")
                return@withContext generateBalancedProceduralEvent(state.sector, state.week)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val rawText = parts.getJSONObject(0).getString("text").trim()

            val responseData = JSONObject(rawText)
            
            // Validate and sanitize numerical values to keep the simulation strictly balanced and deterministic
            val title = responseData.optString("title", "Market Shakeup").take(60)
            val description = responseData.optString("description", "A shift in market forces alters industry dynamics.").take(180)
            val typeStr = responseData.optString("type", "BREAKING_NEWS")
            val type = try {
                MarketEventType.valueOf(typeStr)
            } catch (e: Exception) {
                MarketEventType.BREAKING_NEWS
            }
            val severity = responseData.optString("severity", "INFO")
            val duration = responseData.optInt("effectDurationWeeks", 3).coerceIn(2, 8)
            val cashMult = responseData.optDouble("cashMultiplier", 1.0).coerceIn(0.85, 1.0)
            val userGrowthMult = responseData.optDouble("userGrowthMultiplier", 1.0).coerceIn(0.50, 1.50)
            val valuationMult = responseData.optDouble("valuationMultiplier", 1.0).coerceIn(0.50, 1.40)
            val salaryMult = responseData.optDouble("salaryMultiplier", 1.0).coerceIn(0.80, 1.30)
            val headline = responseData.optString("headline", "BREAKING: Market shifts detected.").take(100)

            Log.i("AIPoweredMarketEngine", "Successfully generated AI Custom Event: $title")
            MarketEvent(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                type = type,
                severity = severity,
                effectDurationWeeks = duration,
                cashMultiplier = cashMult,
                userGrowthMultiplier = userGrowthMult,
                valuationMultiplier = valuationMult,
                salaryMultiplier = salaryMult,
                headline = headline
            )
        } catch (e: Exception) {
            Log.e("AIPoweredMarketEngine", "Exception during Gemini AI event generation: ${e.message}", e)
            generateBalancedProceduralEvent(state.sector, state.week)
        }
    }
}
