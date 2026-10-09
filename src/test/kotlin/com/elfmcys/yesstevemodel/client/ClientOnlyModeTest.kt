package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingStruct
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientOnlyModeTest {

    @Test
    fun testClientOnlySelection() {
        ClientOnlySelection.save("custom/steve_special", "texture_01")
        assertTrue(ClientOnlySelection.hasSelection)
        assertEquals("custom/steve_special", ClientOnlySelection.modelId)
        assertEquals("texture_01", ClientOnlySelection.textureId)

        // Test roaming variables persistence
        ClientOnlySelection.updateRoamingVars("custom/steve_special", mapOf("tail" to 1.0f, "expression" to 2.5f))
        val vars1 = ClientOnlySelection.getRoamingVars("custom/steve_special")
        assertEquals(2, vars1.size)
        assertEquals(1.0f, vars1["tail"])
        assertEquals(2.5f, vars1["expression"])

        // Update another model without affecting the first model
        ClientOnlySelection.updateRoamingVars("wine_fox/01_taisho_maid", mapOf("hat" to 0.0f))
        val vars2 = ClientOnlySelection.getRoamingVars("wine_fox/01_taisho_maid")
        assertEquals(1, vars2.size)
        assertEquals(0.0f, vars2["hat"])

        // First model variables should still be preserved
        val vars1Again = ClientOnlySelection.getRoamingVars("custom/steve_special")
        assertEquals(2, vars1Again.size)
        assertEquals(1.0f, vars1Again["tail"])

        // Changing selection to another model preserves roaming variables
        ClientOnlySelection.save("wine_fox/01_taisho_maid", "default")
        assertEquals("wine_fox/01_taisho_maid", ClientOnlySelection.modelId)
        assertEquals("default", ClientOnlySelection.textureId)
        assertEquals(1.0f, ClientOnlySelection.getRoamingVars("custom/steve_special")["tail"])
        assertEquals(0.0f, ClientOnlySelection.getRoamingVars("wine_fox/01_taisho_maid")["hat"])
    }

    @Test
    fun testClientOnlyModeState() {
        ClientOnlyMode.reset()
        assertFalse(ClientOnlyMode.isActive)
        assertFalse(ClientOnlyMode.isForced)

        assertTrue(ClientOnlyMode.markCatalogLoaded())
        assertFalse(ClientOnlyMode.markCatalogLoaded())

        ClientOnlyMode.reset()
        assertTrue(ClientOnlyMode.markCatalogLoaded())
    }

    @Test
    fun testRoamingStructLocalHandling() {
        val vars = Int2FloatOpenHashMap()
        val roaming = RoamingStruct(12345, vars)

        assertFalse(roaming.hasPendingChanges())

        // Modify a Molang variable in roaming struct
        roaming[42] = 3.14f
        assertTrue(roaming.hasPendingChanges())
        assertEquals(3.14f, roaming[42])

        val batch = roaming.consumePendingBoneData()
        assertEquals(12345, batch.modelHashId())
        assertEquals(1, batch.changedVariables().size)
        assertEquals(3.14f, batch.changedVariables().get(42))

        assertFalse(roaming.hasPendingChanges())
    }
}
