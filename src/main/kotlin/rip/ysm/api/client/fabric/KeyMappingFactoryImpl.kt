package rip.ysm.api.client.fabric

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.input.KeyEvent

object KeyMappingFactoryImpl {
    fun createInGameAlt(
        name: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: KeyMapping.Category
    ): KeyMapping = KeyMapping(name, type, keyCode, category)

    fun createInGameNone(
        name: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: KeyMapping.Category
    ): KeyMapping = KeyMapping(name, type, keyCode, category)

    fun isActiveAndMatches(keyMapping: KeyMapping, event: KeyEvent): Boolean = keyMapping.matches(event)
}
