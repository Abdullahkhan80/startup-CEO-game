package com.example.engine

import androidx.annotation.Keep
import com.example.CosmeticItem
import com.example.GameAchievement
import com.example.FounderPassTier

@Keep
object ProgressionData {

    val COSMETICS = listOf(
        // Desks
        CosmeticItem("desk_basic", "Basic IKEA Desk", "DESK", "COMMON", 0),
        CosmeticItem("desk_neon", "Cyberpunk Neon Desk", "DESK", "RARE", 80),
        CosmeticItem("desk_executive", "Mahogany Executive Desk", "DESK", "RARE", 120),
        CosmeticItem("desk_hologram", "Quantum Hologram Console", "DESK", "LEGENDARY", 250, isPremiumOnly = true),

        // Chairs
        CosmeticItem("chair_basic", "Standard Office Swivel", "CHAIR", "COMMON", 0),
        CosmeticItem("chair_ergonomic", "Ergonomic Mesh Chair", "CHAIR", "COMMON", 40),
        CosmeticItem("chair_gaming", "Rivals Pro Gaming Chair", "CHAIR", "RARE", 100),
        CosmeticItem("chair_gold", "Golden Founder Throne", "CHAIR", "LEGENDARY", 300, isPremiumOnly = true),

        // Plants
        CosmeticItem("plant_basic", "Plastic Ficus", "PLANT", "COMMON", 0),
        CosmeticItem("plant_cactus", "Spiky Desk Cactus", "PLANT", "COMMON", 20),
        CosmeticItem("plant_bonsai", "Tranquil Juniper Bonsai", "PLANT", "RARE", 60),
        CosmeticItem("plant_bonsai_holo", "Neo-Kyoto Holographic Sakura", "PLANT", "LEGENDARY", 180, isPremiumOnly = true),

        // Wall Art
        CosmeticItem("wall_basic", "Motivation Cat Poster", "WALL_ART", "COMMON", 0),
        CosmeticItem("wall_blueprint", "System Architecture Blueprint", "WALL_ART", "RARE", 50),
        CosmeticItem("wall_neon_logo", "Golden Neon Logo Sign", "WALL_ART", "LEGENDARY", 200),
        CosmeticItem("wall_nft", "Rare Pixel Art Canvas", "WALL_ART", "LEGENDARY", 400, isPremiumOnly = true)
    )

    val ACHIEVEMENTS = listOf(
        GameAchievement(
            id = "ach_level_5",
            title = "Rising Star Founder",
            description = "Reach Founder Level 5 to build early credibility.",
            targetType = "LEVEL",
            targetValue = 5.0,
            xpReward = 150,
            tokenReward = 30
        ),
        GameAchievement(
            id = "ach_level_15",
            title = "Silicon Valley Veteran",
            description = "Reach Founder Level 15 to establish your leadership reputation.",
            targetType = "LEVEL",
            targetValue = 15.0,
            xpReward = 500,
            tokenReward = 80,
            cosmeticRewardId = "desk_neon"
        ),
        GameAchievement(
            id = "ach_valuation_1m",
            title = "Seven Figure Club",
            description = "Achieve a company valuation of $1,000,000.",
            targetType = "REVENUE",
            targetValue = 1_000_000.0,
            xpReward = 200,
            tokenReward = 50
        ),
        GameAchievement(
            id = "ach_valuation_100m",
            title = "The Centicorn Path",
            description = "Achieve a massive company valuation of $100,000,000.",
            targetType = "REVENUE",
            targetValue = 100_000_000.0,
            xpReward = 1000,
            tokenReward = 200,
            cosmeticRewardId = "chair_gold"
        ),
        GameAchievement(
            id = "ach_employees_10",
            title = "Power Coordinator",
            description = "Grow your active team size to 10 employees.",
            targetType = "EMPLOYEES",
            targetValue = 10.0,
            xpReward = 100,
            tokenReward = 25
        ),
        GameAchievement(
            id = "ach_quality_50",
            title = "Pristine Craftsmanship",
            description = "Improve your active product quality level to 50/100.",
            targetType = "QUALITY",
            targetValue = 50.0,
            xpReward = 150,
            tokenReward = 40,
            cosmeticRewardId = "wall_blueprint"
        )
    )

    val FOUNDER_PASS_TIERS = (1..20).map { tier ->
        val xpReq = 100L
        val freeName = when (tier) {
            1 -> "Starter Pack (20 Tokens)"
            2 -> "XP Booster"
            4 -> "Spiky Desk Cactus"
            7 -> "50 Cosmetic Tokens"
            10 -> "Rare System Blueprint Wall Art"
            13 -> "75 Cosmetic Tokens"
            16 -> "Tranquil Juniper Bonsai"
            20 -> "Elite Golden Neon Logo Sign"
            else -> "10 Cosmetic Tokens"
        }
        val freeId = when (tier) {
            1 -> "tokens_20"
            2 -> "xp_booster"
            4 -> "plant_cactus"
            7 -> "tokens_50"
            10 -> "wall_blueprint"
            13 -> "tokens_75"
            16 -> "plant_bonsai"
            20 -> "wall_neon_logo"
            else -> "tokens_10"
        }
        val premName = when (tier) {
            1 -> "Founder Pass Pro Badge"
            3 -> "Cyberpunk Neon Desk"
            5 -> "Rivals Pro Gaming Chair"
            8 -> "100 Cosmetic Tokens"
            12 -> "Neo-Kyoto Holographic Sakura"
            15 -> "150 Cosmetic Tokens"
            18 -> "Rare Pixel Art Canvas"
            20 -> "Golden Founder Throne"
            else -> "30 Cosmetic Tokens"
        }
        val premId = when (tier) {
            1 -> "badge_pro"
            3 -> "desk_neon"
            5 -> "chair_gaming"
            8 -> "tokens_100"
            12 -> "plant_bonsai_holo"
            15 -> "tokens_150"
            18 -> "wall_nft"
            20 -> "chair_gold"
            else -> "tokens_30"
        }
        FounderPassTier(tier, xpReq, freeName, freeId, premName, premId)
    }
}
