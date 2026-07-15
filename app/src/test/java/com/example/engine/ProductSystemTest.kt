package com.example.engine

import org.junit.Assert.*
import org.junit.Test

class ProductSystemTest {

    @Test
    fun testCreateProductVariousCategories() {
        val saasProduct = ProductSystem.createProduct(
            name = "Zen CRM Workspace",
            category = ProductCategory.SAAS,
            launchWeek = 1
        )

        assertEquals("Zen CRM Workspace", saasProduct.name)
        assertEquals(ProductCategory.SAAS, saasProduct.category)
        assertTrue(saasProduct.features.isNotEmpty())
        assertEquals("v0.1.0", saasProduct.currentVersion)
        assertEquals(90.0, saasProduct.performance, 0.01)
        assertEquals(85.0, saasProduct.security, 0.01)
        assertEquals(5.0, saasProduct.popularity, 0.01)
        assertFalse(saasProduct.isReleased)
        assertTrue(saasProduct.roadmap.isNotEmpty())

        val aiProduct = ProductSystem.createProduct(
            name = "Cerebro Logic",
            category = ProductCategory.AI_PRODUCT,
            launchWeek = 5
        )
        assertEquals(ProductCategory.AI_PRODUCT, aiProduct.category)
        assertTrue(aiProduct.developmentCost > saasProduct.developmentCost) // AI is more complex
    }

    @Test
    fun testAddFeatureAndRoadmapItem() {
        var product = ProductSystem.createProduct("Pulse Mobile", ProductCategory.MOBILE_APP, 2)
        val initialFeatureCount = product.features.size

        product = ProductSystem.addFeature(product, "Dark Theme Mode", complexity = 20.0)
        assertEquals(initialFeatureCount + 1, product.features.size)
        assertEquals("Dark Theme Mode", product.features.last().name)
        assertEquals(20.0, product.features.last().complexity, 0.01)

        val initialRoadmapCount = product.roadmap.size
        product = ProductSystem.addRoadmapItem(product, "Expand to Tablets", targetWeek = 20)
        assertEquals(initialRoadmapCount + 1, product.roadmap.size)
        assertEquals("Expand to Tablets", product.roadmap.last().objective)
        assertEquals(20, product.roadmap.last().targetWeek)
    }

    @Test
    fun testReleaseVersionUpdate() {
        var product = ProductSystem.createProduct("Secure Chat", ProductCategory.MESSAGING_APP, 1)
            .copy(bugCount = 20, technicalDebt = 30.0, customerSatisfaction = 60.0)

        product = ProductSystem.releaseVersionUpdate(
            product = product,
            version = "v1.1.0",
            week = 4,
            notes = "Performance optimizations and encryption patches."
        )

        assertEquals("v1.1.0", product.currentVersion)
        assertTrue(product.bugCount < 20)
        assertTrue(product.technicalDebt < 30.0)
        assertTrue(product.customerSatisfaction > 60.0)
        assertEquals(1, product.versionUpdates.size)
        assertEquals("v1.1.0", product.versionUpdates[0].version)
        assertTrue(product.isReleased)
    }

    @Test
    fun testExecuteLaunchEvent() {
        var product = ProductSystem.createProduct("Battle Royale", ProductCategory.GAME, 1)
            .copy(popularity = 10.0, customerSatisfaction = 70.0)

        product = ProductSystem.executeLaunchEvent(
            product = product,
            week = 2,
            title = "E3 Reveal Trailer",
            description = "Unveiling state-of-the-art multiplayer systems.",
            marketingSpend = 1000.0
        )

        assertTrue(product.popularity > 10.0)
        assertEquals(1, product.launchEvents.size)
        assertEquals("E3 Reveal Trailer", product.launchEvents[0].title)
    }

    @Test
    fun testProcessDevelopmentWork() {
        var product = ProductSystem.createProduct("Cloud Storage", ProductCategory.CLOUD_SERVICE, 1)
        // All default features are uncompleted at 0%
        assertTrue(product.features.all { !it.isCompleted })

        // Apply engineering power to features
        product = ProductSystem.processDevelopmentWork(
            product = product,
            devPower = 80.0,
            pmMultiplier = 1.2,
            designerPower = 40.0
        )

        // At least some features should have progress
        assertTrue(product.features.any { it.progress > 0.0 })
    }

    @Test
    fun testProcessWeeklyProductDynamics() {
        val product = ProductSystem.createProduct("CodeEditor", ProductCategory.DEVELOPER_TOOL, 1)
            .copy(bugCount = 5, performance = 95.0, security = 90.0)

        // Simulate 1 week of operations
        val updated = ProductSystem.processWeeklyProductDynamics(
            product = product,
            devPower = 50.0,
            activeUsers = 5000L,
            week = 2
        )

        assertNotNull(updated)
        assertTrue(updated.bugCount >= 0)
        assertTrue(updated.performance > 0.0)
        assertTrue(updated.security > 0.0)
        assertTrue(updated.customerSatisfaction > 0.0)
    }
}
