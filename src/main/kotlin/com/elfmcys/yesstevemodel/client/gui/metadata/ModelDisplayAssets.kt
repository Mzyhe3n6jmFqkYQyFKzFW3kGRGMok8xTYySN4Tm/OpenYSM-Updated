package com.elfmcys.yesstevemodel.client.gui.metadata

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import net.minecraft.client.renderer.texture.AbstractTexture

data class ModelDisplayAssets(
    val selectedTexture: String,
    var isAuthModel: Boolean,
    val authorAvatars: Map<String, OuterFileTexture>,
    private val guiTextures: Map<String, AbstractTexture>
) {
    val guiForeground: AbstractTexture?
        get() = guiTextures["gui_foreground"]
    val guiBackground: AbstractTexture?
        get() = guiTextures["gui_background"]
}