package rip.ysm.api.client

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.input.KeyEvent
import net.minecraft.resources.Identifier
import rip.ysm.api.client.fabric.KeyMappingFactoryImpl

object KeyMappingFactory {
    @JvmField
    val YSM_CATEGORY: KeyMapping.Category = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("yes_steve_model", "main")
    )

    @JvmStatic
    fun createInGameAlt(
        name: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: KeyMapping.Category
    ): KeyMapping {
        return KeyMappingFactoryImpl.createInGameAlt(name, type, keyCode, category)
    }

    @JvmStatic
    fun createInGameNone(
        name: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: KeyMapping.Category
    ): KeyMapping {
        return KeyMappingFactoryImpl.createInGameNone(name, type, keyCode, category)
    }

    @JvmStatic
    fun isActiveAndMatches(keyMapping: KeyMapping, event: KeyEvent): Boolean {
        return KeyMappingFactoryImpl.isActiveAndMatches(keyMapping, event)
    }
}
