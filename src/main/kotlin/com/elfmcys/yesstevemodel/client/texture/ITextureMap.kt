package com.elfmcys.yesstevemodel.client.texture

import net.minecraft.client.renderer.texture.AbstractTexture
import rip.ysm.compat.oculus.ShadersTextureType

interface ITextureMap {
    val suffixTextures: Map<ShadersTextureType, AbstractTexture>
}