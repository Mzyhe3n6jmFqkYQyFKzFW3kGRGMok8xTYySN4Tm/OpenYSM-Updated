package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import net.minecraft.network.chat.Component

object ComponentUtil {
    @JvmStatic
    fun getDisplayName(modelAssembly: ModelAssembly, str: String): Component {
        val selectedTexture = modelAssembly.textureRegistry.selectedTexture
        return if (selectedTexture.isBlank()) {
            Component.literal(str)
        } else {
            Component.literal(selectedTexture)
        }
    }
}