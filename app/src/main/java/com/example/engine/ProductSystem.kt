package com.example.engine

import androidx.annotation.Keep
import java.util.UUID

@Keep
object ProductSystem {

    /**
     * Initializes a brand new product from a chosen category.
     */
    fun createProduct(
        name: String,
        category: ProductCategory,
        launchWeek: Int,
        budget: Double = 0.0
    ): SimProduct {
        // Base development costs and maintenance projections
        val devCost = category.baseComplexity * 400.0
        val maintenanceFactor = category.baseMaintenanceFactor
        val weeklyMaintenance = devCost * maintenanceFactor

        // Default initial features appropriate to the category
        val initialFeatures = when (category) {
            ProductCategory.AI_PRODUCT -> listOf(
                SimFeature(name = "Core LLM Integration", complexity = 50.0),
                SimFeature(name = "Contextual Memory Buffer", complexity = 35.0)
            )
            ProductCategory.SAAS -> listOf(
                SimFeature(name = "User Authentication", complexity = 20.0),
                SimFeature(name = "Data Workspace Grid", complexity = 30.0)
            )
            ProductCategory.CRM -> listOf(
                SimFeature(name = "Contact Database Engine", complexity = 40.0),
                SimFeature(name = "Pipeline Funnel Tracker", complexity = 35.0)
            )
            ProductCategory.ERP -> listOf(
                SimFeature(name = "Ledger Accounting Module", complexity = 60.0),
                SimFeature(name = "Inventory Control API", complexity = 50.0)
            )
            ProductCategory.SOCIAL_NETWORK -> listOf(
                SimFeature(name = "User Profile Feed", complexity = 25.0),
                SimFeature(name = "Graph Connection Engine", complexity = 45.0)
            )
            ProductCategory.MESSAGING_APP -> listOf(
                SimFeature(name = "P2P WebSockets Gateway", complexity = 30.0),
                SimFeature(name = "End-to-End Encryption Layer", complexity = 55.0)
            )
            ProductCategory.SEARCH_ENGINE -> listOf(
                SimFeature(name = "Web Crawler Daemon", complexity = 50.0),
                SimFeature(name = "Inverted Index Builder", complexity = 60.0)
            )
            ProductCategory.CLOUD_SERVICE -> listOf(
                SimFeature(name = "Virtual Machine Hypervisor", complexity = 80.0),
                SimFeature(name = "Object Storage API Gateway", complexity = 70.0)
            )
            ProductCategory.DEVELOPER_TOOL -> listOf(
                SimFeature(name = "Syntax Parser / AST", complexity = 30.0),
                SimFeature(name = "Local Hot-Reload Engine", complexity = 25.0)
            )
            ProductCategory.MOBILE_APP -> listOf(
                SimFeature(name = "Responsive Canvas UI", complexity = 15.0),
                SimFeature(name = "Local Key-Value Cache", complexity = 15.0)
            )
            ProductCategory.GAME -> listOf(
                SimFeature(name = "Physics Collision Matrix", complexity = 40.0),
                SimFeature(name = "UDP Client-Server Router", complexity = 50.0)
            )
        }

        // Default initial roadmap items
        val initialRoadmap = listOf(
            SimRoadmapItem(objective = "Complete MVP Core Features", targetWeek = launchWeek + 4),
            SimRoadmapItem(objective = "Reach 50% Market Awareness", targetWeek = launchWeek + 10),
            SimRoadmapItem(objective = "Execute Major Launch Event", targetWeek = launchWeek + 16)
        )

        return SimProduct(
            name = name.ifBlank { "New Project" },
            category = category,
            designQuality = 35.0,
            codeQuality = 35.0,
            bugDensity = 8.0,
            bugCount = 10,
            featureCompleteness = 10.0,
            launchWeek = launchWeek,
            weeklyMarketingBudget = budget,
            developmentCost = devCost,
            maintenanceCost = weeklyMaintenance,
            features = initialFeatures,
            roadmap = initialRoadmap,
            currentVersion = "v0.1.0",
            performance = 90.0,
            security = 85.0,
            popularity = 5.0,
            marketShare = 0.0,
            customerSatisfaction = 75.0,
            technicalDebt = 5.0,
            isReleased = false
        )
    }

    /**
     * Adds a custom new feature to the product's design.
     */
    fun addFeature(product: SimProduct, name: String, complexity: Double): SimProduct {
        val newFeature = SimFeature(
            name = name.ifBlank { "Feature ${product.features.size + 1}" },
            complexity = complexity.coerceIn(10.0, 100.0)
        )
        return product.copy(
            features = product.features + newFeature,
            isReleased = false // Changes require re-release verification to reflect properly
        )
    }

    /**
     * Set a new target on the product's roadmap.
     */
    fun addRoadmapItem(product: SimProduct, objective: String, targetWeek: Int): SimProduct {
        val newItem = SimRoadmapItem(objective = objective, targetWeek = targetWeek)
        return product.copy(roadmap = product.roadmap + newItem)
    }

    /**
     * Deploys a new version update (e.g. v1.1.0) which fixes bugs and resolves technical debt.
     */
    fun releaseVersionUpdate(
        product: SimProduct,
        version: String,
        week: Int,
        notes: String
    ): SimProduct {
        // Evaluate bug fixes and debt reduction
        val resolvedBugsCount = (product.bugCount * 0.6).toInt()
        val updatedBugCount = (product.bugCount - resolvedBugsCount).coerceAtLeast(0)
        val updatedBugDensity = (product.bugDensity * 0.4).coerceAtLeast(1.0)
        
        // Debt cleanup
        val resolvedDebt = product.technicalDebt * 0.35
        val updatedDebt = (product.technicalDebt - resolvedDebt).coerceAtLeast(2.0)

        // Customer satisfaction boost from deployment
        val satisfactionBoost = if (resolvedBugsCount > 5) 12.0 else 5.0
        val updatedSatisfaction = (product.customerSatisfaction + satisfactionBoost).coerceAtMost(100.0)

        val updateRecord = SimVersionUpdate(
            version = version,
            week = week,
            notes = notes,
            stabilityShift = resolvedBugsCount.toDouble(),
            customerSatisfactionShift = satisfactionBoost
        )

        return product.copy(
            currentVersion = version,
            bugCount = updatedBugCount,
            bugDensity = updatedBugDensity,
            technicalDebt = updatedDebt,
            customerSatisfaction = updatedSatisfaction,
            versionUpdates = product.versionUpdates + updateRecord,
            isReleased = true
        )
    }

    /**
     * Executes a product launch/PR event to boost popularity and user awareness.
     */
    fun executeLaunchEvent(
        product: SimProduct,
        week: Int,
        title: String,
        description: String,
        marketingSpend: Double
    ): SimProduct {
        val qualityMultiplier = product.productQualityScore / 50.0
        val popularityBoost = (5.0 + Math.sqrt(marketingSpend) * 0.5) * qualityMultiplier
        val satisfactionShift = if (product.bugCount > 15) -10.0 else 8.0

        val event = SimLaunchEvent(
            week = week,
            title = title,
            description = description,
            customerSatisfactionShift = satisfactionShift,
            popularityBoost = popularityBoost
        )

        val updatedPopularity = (product.popularity + popularityBoost).coerceAtMost(100.0)
        val updatedSatisfaction = (product.customerSatisfaction + satisfactionShift).coerceIn(0.0, 100.0)

        return product.copy(
            popularity = updatedPopularity,
            customerSatisfaction = updatedSatisfaction,
            launchEvents = product.launchEvents + event
        )
    }

    /**
     * Simulates the incremental software development progress of a product's features.
     */
    fun processDevelopmentWork(
        product: SimProduct,
        devPower: Double,
        pmMultiplier: Double,
        designerPower: Double
    ): SimProduct {
        if (product.features.isEmpty()) return product

        // Distribute devPower, designPower across uncompleted features
        val uncompletedFeatures = product.features.filter { !it.isCompleted }
        if (uncompletedFeatures.isEmpty()) {
            // If all features completed, power builds up product design/code quality instead
            val updatedDesign = (product.designQuality + designerPower * 0.1).coerceAtMost(100.0)
            val updatedCode = (product.codeQuality + devPower * 0.15).coerceAtMost(100.0)
            return product.copy(
                designQuality = SimulationEngine.round(updatedDesign),
                codeQuality = SimulationEngine.round(updatedCode)
            )
        }

        // Distribute power evenly
        val distributedDevPower = devPower / uncompletedFeatures.size
        val distributedDesignPower = designerPower / uncompletedFeatures.size

        val updatedFeatures = product.features.map { feature ->
            if (feature.isCompleted) {
                feature
            } else {
                val speedCoefficient = pmMultiplier
                val progressAdded = (distributedDevPower * 0.6 + distributedDesignPower * 0.4) * speedCoefficient
                val newProgress = (feature.progress + progressAdded).coerceAtMost(100.0)
                val completed = newProgress >= 100.0

                // When feature completes, high complexity features add more tech debt if designer/dev ratio is low
                val debtAccrued = if (completed) {
                    val developerRatio = distributedDevPower / (distributedDesignPower + 0.1)
                    val baseDebt = feature.complexity * 0.08
                    val penalty = if (developerRatio > 2.0) baseDebt * 0.5 else 0.0
                    baseDebt + penalty
                } else 0.0

                feature.copy(
                    progress = SimulationEngine.round(newProgress),
                    isCompleted = completed,
                    technicalDebtAccrued = SimulationEngine.round(debtAccrued)
                )
            }
        }

        // Recalculate feature completeness
        val completedCount = updatedFeatures.count { it.isCompleted }
        val featureCompleteness = (completedCount.toDouble() / updatedFeatures.size) * 100.0

        // Accrue technical debt from completed features
        val newDebtAdded = updatedFeatures.sumOf { it.technicalDebtAccrued }
        val totalTechDebt = (product.technicalDebt + newDebtAdded).coerceIn(0.0, 100.0)

        // Clear accrued tech debt records from the features so they don't compound next week
        val cleanFeatures = updatedFeatures.map { it.copy(technicalDebtAccrued = 0.0) }

        return product.copy(
            features = cleanFeatures,
            featureCompleteness = SimulationEngine.round(featureCompleteness),
            technicalDebt = SimulationEngine.round(totalTechDebt)
        )
    }

    /**
     * Evaluates and progresses product traits, security, bugs, performance and market dynamics.
     */
    fun processWeeklyProductDynamics(
        product: SimProduct,
        devPower: Double,
        activeUsers: Long,
        week: Int
    ): SimProduct {
        var bugCount = product.bugCount
        var bugDensity = product.bugDensity
        var performance = product.performance
        var security = product.security
        var customerSatisfaction = product.customerSatisfaction

        // 1. Bug Accumulation from Technical Debt and feature scale
        // High dev power can mitigate bugs if focused, but features introduce bugs
        val baseNewBugs = (product.features.size * 0.5) + (product.technicalDebt * 0.15)
        val devMitigation = devPower * 0.25
        val netBugsCreated = (baseNewBugs - devMitigation).toInt().coerceAtLeast(0)
        
        bugCount += netBugsCreated
        bugDensity = (bugCount.toDouble() / (product.features.size + 1.0) * 5.0).coerceIn(0.0, 100.0)

        // 2. Performance Degradation due to scaling active users vs tech debt
        val loadMultiplier = (activeUsers.toDouble() / 10000.0).coerceIn(0.0, 25.0)
        val performanceDrain = (loadMultiplier * 0.3) + (product.technicalDebt * 0.08)
        val performanceBoost = if (devPower > 0) (devPower * 0.15) else 0.0
        
        performance = (performance - performanceDrain + performanceBoost).coerceIn(10.0, 100.0)

        // 3. Security vulnerabilities exposure (critical if tech debt or category sensitive is high)
        val securitySensitivity = product.category.baseSecuritySensitivity
        val securityDecay = (product.technicalDebt * 0.1 * securitySensitivity) + (if (bugDensity > 40.0) 3.0 else 0.0)
        val securityHardening = if (devPower > 0) (devPower * 0.2) else 0.0

        security = (security - securityDecay + securityHardening).coerceIn(10.0, 100.0)

        // 4. Customer satisfaction mapping
        // Driven by bugs, performance, security, and design quality
        val qualityScore = product.productQualityScore
        val targetSatisfaction = (qualityScore * 0.7 + product.designQuality * 0.3).coerceIn(0.0, 100.0)
        
        // Interpolate satisfaction weekly
        customerSatisfaction = (customerSatisfaction * 0.8 + targetSatisfaction * 0.2)
        if (performance < 40.0) customerSatisfaction -= 8.0 // Severe penalty for lags
        if (security < 30.0) customerSatisfaction -= 15.0  // Severe penalty for data breaches
        customerSatisfaction = customerSatisfaction.coerceIn(0.0, 100.0)

        // 5. Evaluate roadmap milestones
        val updatedRoadmap = product.roadmap.map { goal ->
            if (goal.isCompleted) {
                goal
            } else {
                val achieved = when {
                    goal.objective.contains("MVP") && product.featureCompleteness >= 80.0 -> true
                    goal.objective.contains("Market") && product.marketShare >= 5.0 -> true
                    goal.objective.contains("Launch") && product.launchEvents.isNotEmpty() -> true
                    else -> false
                }
                goal.copy(isCompleted = achieved)
            }
        }

        // 6. Calculate Dynamic Market Share based on category complexity, popularity, and quality
        val rawShare = (product.popularity * 0.6 + qualityScore * 0.4) / (product.category.baseComplexity + 10.0)
        val finalMarketShare = rawShare.coerceIn(0.0, 100.0)

        return product.copy(
            bugCount = bugCount,
            bugDensity = SimulationEngine.round(bugDensity),
            performance = SimulationEngine.round(performance),
            security = SimulationEngine.round(security),
            customerSatisfaction = SimulationEngine.round(customerSatisfaction),
            roadmap = updatedRoadmap,
            marketShare = SimulationEngine.round(finalMarketShare, 3)
        )
    }
}
