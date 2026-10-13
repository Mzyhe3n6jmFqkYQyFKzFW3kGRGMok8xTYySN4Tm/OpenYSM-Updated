package com.elfmcys.yesstevemodel.model

import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.*
import kotlin.io.path.Path

/**
 * Regression tests for per-player / per-model persistence of model selection and roaming
 * variables. These guard against the file being lost or corrupted by the asynchronous save
 * path in [com.elfmcys.yesstevemodel.util.AbstractManager].
 */
class SelectionPersistenceTest {
    private val player1 = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
    private val file = Path(".temp/config/yes_steve_model/server_selection.dat")

    @AfterTest
    fun tearDown() {
        ServerModelSelection.clear()
    }

    private fun readBackFromDisk(): ServerSelectionData {
        // Wait for the asynchronous save coroutines to flush.
        Thread.sleep(1500)
        val tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap())
        return ServerSelectionData.load(tag)
    }

    @Test
    fun testRoamingSurvivesReloadFromDisk() {
        ServerModelSelection.clear()
        ServerModelSelection.savePlayerSelection(player1, "wine_fox/01_taisho_maid", "default")
        ServerModelSelection.updateRoamingVars(
            player1,
            "wine_fox/01_taisho_maid",
            mapOf("hat" to 1.0f, "expression" to 3.0f)
        )

        val onDisk = readBackFromDisk()
        val player = onDisk.players[player1.toString()]
        assertNotNull(player, "player record must be present on disk")
        assertEquals("wine_fox/01_taisho_maid", player.modelId)
        assertEquals(
            mapOf("hat" to 1.0f, "expression" to 3.0f),
            player.roamingStorage["wine_fox/01_taisho_maid"]?.toMap(),
            "roaming variables must survive a reload from disk"
        )
    }

    @Test
    fun testRoamingIsStoredPerModel() {
        ServerModelSelection.clear()
        ServerModelSelection.updateRoamingVars(player1, "model_a", mapOf("v" to 1.0f))
        ServerModelSelection.updateRoamingVars(player1, "model_b", mapOf("v" to 2.0f))

        val onDisk = readBackFromDisk()
        val player = onDisk.players[player1.toString()]
        assertNotNull(player)
        assertEquals(1.0f, player.roamingStorage["model_a"]?.get("v"))
        assertEquals(2.0f, player.roamingStorage["model_b"]?.get("v"))
    }

    @Test
    fun testRepeatedRapidSavesDoNotDropRoaming() {
        ServerModelSelection.clear()
        ServerModelSelection.savePlayerSelection(player1, "wine_fox/01_taisho_maid", "default")
        repeat(200) { i ->
            ServerModelSelection.updateRoamingVars(player1, "wine_fox/01_taisho_maid", mapOf("v$i" to i.toFloat()))
        }
        // Force an immediate synchronous flush, exactly like SERVER_STOPPING does.
        ServerModelSelection.saveNow()

        val onDisk = readBackFromDisk()
        val player = onDisk.players[player1.toString()]
        assertNotNull(player)
        val roaming = player.roamingStorage["wine_fox/01_taisho_maid"] ?: ConcurrentHashMap()
        assertEquals(200, roaming.size, "every roaming variable must be present")
    }
}
