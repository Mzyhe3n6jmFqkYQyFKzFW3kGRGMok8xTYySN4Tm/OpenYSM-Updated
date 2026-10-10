package com.elfmcys.yesstevemodel.model

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.*

class ServerModelSelectionTest {
    private val player1 = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
    private val player2 = UUID.fromString("11111111-2222-3333-4444-555555555555")

    @BeforeTest
    fun setUp() {
        ServerModelSelection.clear()
    }

    @Test
    fun testPlayerSelectionAndPersistence() {
        assertFalse(ServerModelSelection.hasSelection(player1))

        ServerModelSelection.savePlayerSelection(player1, "wine_fox/01_taisho_maid", "default")
        assertTrue(ServerModelSelection.hasSelection(player1))
        assertEquals("wine_fox/01_taisho_maid", ServerModelSelection.getPlayerModel(player1))
        assertEquals("default", ServerModelSelection.getPlayerTexture(player1))

        // Roaming variables persistence per model
        ServerModelSelection.updateRoamingVars(
            player1,
            "wine_fox/01_taisho_maid",
            mapOf("hat" to 1.0f, "expression" to 3.0f)
        )
        val vars1 = ServerModelSelection.getRoamingVars(player1, "wine_fox/01_taisho_maid")
        assertEquals(2, vars1.size)
        assertEquals(1.0f, vars1["hat"])
        assertEquals(3.0f, vars1["expression"])

        // Switch to model 2 and store roaming variables for model 2
        ServerModelSelection.savePlayerSelection(player1, "wine_fox/08_sta", "tex2")
        assertEquals("wine_fox/08_sta", ServerModelSelection.getPlayerModel(player1))
        assertEquals("tex2", ServerModelSelection.getPlayerTexture(player1))

        ServerModelSelection.updateRoamingVars(player1, "wine_fox/08_sta", mapOf("bag" to 2.5f))
        val varsModel2 = ServerModelSelection.getRoamingVars(player1, "wine_fox/08_sta")
        assertEquals(1, varsModel2.size)
        assertEquals(2.5f, varsModel2["bag"])

        // Model 1 roaming variables must still be preserved
        val varsModel1Again = ServerModelSelection.getRoamingVars(player1, "wine_fox/01_taisho_maid")
        assertEquals(2, varsModel1Again.size)
        assertEquals(1.0f, varsModel1Again["hat"])
        assertEquals(3.0f, varsModel1Again["expression"])
    }

    @Test
    fun testMultiPlayerIsolation() {
        ServerModelSelection.savePlayerSelection(player1, "model_a", "tex_a")
        ServerModelSelection.updateRoamingVars(player1, "model_a", mapOf("val" to 10.0f))

        ServerModelSelection.savePlayerSelection(player2, "model_b", "tex_b")
        ServerModelSelection.updateRoamingVars(player2, "model_b", mapOf("val" to 20.0f))

        assertEquals("model_a", ServerModelSelection.getPlayerModel(player1))
        assertEquals("model_b", ServerModelSelection.getPlayerModel(player2))
        assertEquals(10.0f, ServerModelSelection.getRoamingVars(player1, "model_a")["val"])
        assertEquals(20.0f, ServerModelSelection.getRoamingVars(player2, "model_b")["val"])
        assertTrue(ServerModelSelection.getRoamingVars(player1, "model_b").isEmpty())
    }

    @Test
    fun testAuthModelsManagement() {
        assertTrue(ServerModelSelection.getAuthModels(player1).isEmpty())

        ServerModelSelection.addAuthModel(player1, "vip/exclusive_01")
        ServerModelSelection.addAllAuthModels(player1, listOf("vip/exclusive_02", "vip/exclusive_03"))

        val auths = ServerModelSelection.getAuthModels(player1)
        assertEquals(3, auths.size)
        assertTrue(auths.contains("vip/exclusive_01"))
        assertTrue(auths.contains("vip/exclusive_02"))
        assertTrue(auths.contains("vip/exclusive_03"))

        ServerModelSelection.removeAuthModel(player1, "vip/exclusive_02")
        val authsAfterRemove = ServerModelSelection.getAuthModels(player1)
        assertEquals(2, authsAfterRemove.size)
        assertFalse(authsAfterRemove.contains("vip/exclusive_02"))

        // Player 2 is isolated
        assertTrue(ServerModelSelection.getAuthModels(player2).isEmpty())

        ServerModelSelection.clearAuthModels(player1)
        assertTrue(ServerModelSelection.getAuthModels(player1).isEmpty())
    }

    @Test
    fun testNbtSerializationRoundTrip() {
        val player = PlayerServerData(
            modelId = "test_model",
            textureId = "test_tex"
        )
        player.roamingStorage["test_model"] = ConcurrentHashMap(mapOf("slider1" to 5.5f))
        player.authModels.addAll(listOf("auth_1", "auth_2"))

        val tag = player.save()
        val loaded = PlayerServerData.load(tag)

        assertEquals("test_model", loaded.modelId)
        assertEquals("test_tex", loaded.textureId)
        assertEquals(5.5f, loaded.roamingStorage["test_model"]?.get("slider1"))
        assertEquals(setOf("auth_1", "auth_2"), loaded.authModels)
    }
}
