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
        assertTrue(ClientOnlySelection.hasSelection())
        assertEquals("custom/steve_special", ClientOnlySelection.getModelId())
        assertEquals("texture_01", ClientOnlySelection.getTextureId())
    }

    @Test
    fun testClientOnlyModeState() {
        ClientOnlyMode.reset()
        assertFalse(ClientOnlyMode.isActive())
        assertFalse(ClientOnlyMode.isForced())

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
