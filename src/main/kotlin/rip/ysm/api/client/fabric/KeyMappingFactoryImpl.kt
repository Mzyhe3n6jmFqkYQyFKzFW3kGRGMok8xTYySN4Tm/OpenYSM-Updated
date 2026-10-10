package rip.ysm.api.client.fabric

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.input.KeyEvent

object KeyMappingFactoryImpl {
    @JvmStatic
    fun createInGameAlt(
        name: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: KeyMapping.Category
    ): KeyMapping {
        return KeyMapping(name, type, keyCode, category)
    }

    @JvmStatic
    fun createInGameNone(
        name: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: KeyMapping.Category
    ): KeyMapping {
        return KeyMapping(name, type, keyCode, category)
    }

    @JvmStatic
    fun isActiveAndMatches(keyMapping: KeyMapping, event: KeyEvent): Boolean {
        return keyMapping.matches(event)
    }
}
