package com.elfmcys.yesstevemodel.client.texture

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.GpuDevice
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.AddressMode
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.TextureFormat
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import net.minecraft.client.renderer.texture.AbstractTexture
import rip.ysm.compat.oculus.ShadersTextureType
import java.io.ByteArrayInputStream

class OuterFileTexture(private val data: ByteArray?) : AbstractTexture(), ITextureMap {
    var loadedImage: NativeImage? = null
        private set

    override var suffixTextures: Map<ShadersTextureType, OuterFileTexture> = Reference2ReferenceMaps.emptyMap()
        private set

    fun load() {
        if (RenderSystem.isOnRenderThread()) {
            doLoad()
        } else {
            RenderSystem.queueFencedTask(::doLoad)
        }
    }

    fun doLoad() {
        val rawData = data ?: return
        runCatching {
            val image = NativeImage.read(ByteArrayInputStream(rawData))
            loadedImage = image
            val gpuDevice: GpuDevice = RenderSystem.getDevice()
            val createdTexture =
                gpuDevice.createTexture("OutFileTexture", 5, TextureFormat.RGBA8, image.width, image.height, 1, 1)
            texture = createdTexture
            textureView = gpuDevice.createTextureView(createdTexture)
            gpuDevice.createCommandEncoder().writeToTexture(createdTexture, image)
            sampler = RenderSystem.getSamplerCache().getSampler(
                AddressMode.REPEAT,
                AddressMode.REPEAT,
                FilterMode.NEAREST,
                FilterMode.NEAREST,
                false
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    fun setSuffixTextures(map: Map<ShadersTextureType, OuterFileTexture>) {
        suffixTextures = Reference2ReferenceMaps.unmodifiable(Reference2ReferenceOpenHashMap(map))
    }
}