package com.example

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class GamePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("startup_ceo_simulator_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(GameState::class.java)

    data class GameState(
        val ceoName: String = "Sarah Jenkins",
        val companyName: String = "NeuraLinker",
        val ceoStage: CEOStage = CEOStage.SOLO_FOUNDER,
        val sector: Sector = Sector.AI,
        val valuation: Double = 150_000.0,
        val valuationGrowth: Double = 0.05,
        val cash: Double = 60_000.0,
        val users: Long = 0,
        val productQuality: Double = 15.0,
        val week: Int = 1,
        val employees: List<Employee> = emptyList(),
        val newsFeed: List<NewsFeedItem> = listOf(
            NewsFeedItem(
                timestamp = "Week 1",
                headline = "Founded NeuraLinker! Ready to build a tech empire from the ground up.",
                source = "System"
            )
        ),
        val activeDecisionId: String? = null,
        val unlockedRegions: List<String> = listOf("North America"),
        val isGameOver: Boolean = false,
        val isIPOExited: Boolean = false,
        val totalInvestmentRaised: Double = 0.0,
        val totalVCDilution: Double = 0.0,
        
        // New Investment properties
        val convertibleNotes: List<com.example.engine.SimConvertibleInstrument> = emptyList(),
        val debts: List<com.example.engine.SimDebtRecord> = emptyList(),
        val boardMembers: List<com.example.engine.SimBoardMember> = emptyList(),
        val activeProposals: List<com.example.engine.SimShareholderProposal> = emptyList(),
        val pitchDeckQuality: Double = 50.0,
        val isAcquired: Boolean = false,
        val capTable: List<com.example.engine.Shareholder> = emptyList(),
        val totalAuthorizedShares: Long = 10_000_000L,
        val simFundingOffers: List<com.example.engine.SimInvestmentOffer> = emptyList(),
        val competitors: List<CompetitorCompany> = emptyList(),
        val marketTrends: List<MarketTrend> = emptyList(),
        val marketEvents: List<MarketEvent> = emptyList(),
        
        // Progression and Monetization Properties
        val xp: Long = 0L,
        val founderLevel: Int = 1,
        val completedAchievements: List<String> = emptyList(),
        val lastClaimedDailyDay: Int = 0,
        val lastClaimedDailyTime: Long = 0L,
        val isPremiumCompanyEnabled: Boolean = false,
        val isExclusiveIndustryUnlocked: Boolean = false,
        val founderPassXp: Long = 0L,
        val founderPassLevel: Int = 1,
        val isFounderPassPremiumUnlocked: Boolean = false,
        val unlockedCosmetics: List<String> = listOf("desk_basic", "chair_basic", "plant_basic", "wall_basic"),
        val equippedDesk: String = "desk_basic",
        val equippedChair: String = "chair_basic",
        val equippedPlant: String = "plant_basic",
        val equippedWallArt: String = "wall_basic",
        val isPlayGamesSignedIn: Boolean = false,
        val cloudSaveTimestamp: Long = 0L,
        val isSubscribedToCEOClub: Boolean = false,
        val hasRemovedAds: Boolean = false,
        val cosmeticTokens: Int = 100,
        val ceoMood: String = "WORKING", // "WORKING", "CELEBRATING", "STRESSED", "DRINKING"
        val ceoEnergy: Int = 100
    )

    fun saveGameState(state: GameState) {
        try {
            val json = adapter.toJson(state)
            prefs.edit().putString("game_state", json).apply()
        } catch (e: Exception) {
            Log.e("GamePrefs", "Error saving game state", e)
        }
    }

    fun loadGameState(): GameState {
        val json = prefs.getString("game_state", null)
        return if (json != null) {
            try {
                adapter.fromJson(json) ?: GameState()
            } catch (e: Exception) {
                Log.e("GamePrefs", "Error loading game state, resetting", e)
                GameState()
            }
        } else {
            GameState()
        }
    }

    fun clearGameState() {
        prefs.edit().remove("game_state").apply()
    }
}
