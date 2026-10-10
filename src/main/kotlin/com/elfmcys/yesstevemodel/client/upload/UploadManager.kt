package com.elfmcys.yesstevemodel.client.upload

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.ResourceCleanupHelper
import com.elfmcys.yesstevemodel.client.texture.ITextureMap
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.google.common.collect.Queues
import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.Pair
import it.unimi.dsi.fastutil.objects.ReferenceIntMutablePair
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.Identifier
import org.apache.commons.lang3.time.StopWatch
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object UploadManager {
    private const val UPLOAD_TIME_LIMIT_MS: Long = 20L
    private var textureCounter: Long = 0L
    private val textureCache = IdentityHashMap<AbstractTexture, WeakReference<TextureLocatable>>()
    private val pendingUploads: Queue<Pair<TextureLocatable, AbstractTexture>> = Queues.newArrayDeque()
    private val expiredTextures = ConcurrentHashMap<AbstractTexture, ReferenceIntMutablePair<Identifier>>()
    private val pendingReleases: Queue<Identifier> = Queues.newArrayDeque()

    @JvmStatic
    fun getOrCreateLocatable(texture: AbstractTexture, register: Boolean): IResourceLocatable {
        return getOrCreateLocatableWithSize(texture, register)
    }

    @JvmStatic
    fun getOrCreateLocatableWithSize(
        texture: AbstractTexture,
        register: Boolean,
        sizeHint: Int = 200
    ): IResourceLocatable {
        RenderSystem.assertOnRenderThread()
        val weakReference = textureCache[texture]
        if (weakReference != null) {
            val locatable = weakReference.get()
            if (locatable != null) {
                if (register && !locatable.registered) {
                    registerTexture(texture, locatable)
                }
                return locatable
            }
            textureCache.remove(texture)
        }

        val removed = expiredTextures.remove(texture)
        val locatable = if (removed != null) {
            TextureLocatable(removed.first(), sizeHint)
        } else {
            TextureLocatable(sizeHint)
        }

        if (texture is ITextureMap) {
            for (suffixTexture in texture.suffixTextures.values) {
                val list =
                    locatable.suffixTextures ?: ArrayList<IResourceLocatable>(2).also { locatable.suffixTextures = it }
                list.add(getOrCreateLocatableWithSize(suffixTexture, register, sizeHint))
            }
        }

        textureCache[texture] = WeakReference(locatable)
        if (register) {
            registerTexture(texture, locatable)
        } else {
            pendingUploads.add(Pair.of(locatable, texture))
        }
        return locatable
    }

    @JvmStatic
    fun removeTexture(abstractTexture: AbstractTexture) {
        RenderSystem.assertOnRenderThread()
        textureCache.remove(abstractTexture)
    }

    @JvmStatic
    fun processPendingUploads() {
        RenderSystem.assertOnRenderThread()
        if (expiredTextures.isNotEmpty()) {
            val it = expiredTextures.entries.iterator()
            while (it.hasNext()) {
                val next = it.next()
                val iSecondInt = next.value.secondInt()
                if (iSecondInt <= 0) {
                    pendingReleases.add(next.value.first())
                    it.remove()
                } else {
                    next.value.second(iSecondInt - 1)
                }
            }
        }

        val stopWatchCreateStarted = StopWatch.createStarted()
        do {
            val pairPoll = pendingUploads.poll()
            if (pairPoll != null) {
                registerTexture(pairPoll.right(), pairPoll.left())
            } else {
                val textureManager: TextureManager = Minecraft.getInstance().textureManager
                do {
                    val resourceLocationPoll = pendingReleases.poll()
                    if (resourceLocationPoll != null) {
                        textureManager.release(resourceLocationPoll)
                    } else {
                        return
                    }
                } while (stopWatchCreateStarted.time < UPLOAD_TIME_LIMIT_MS)
                return
            }
        } while (stopWatchCreateStarted.time < UPLOAD_TIME_LIMIT_MS)
    }

    @JvmStatic
    private fun registerTexture(texture: AbstractTexture, locatable: TextureLocatable) {
        if (!locatable.registered) {
            Minecraft.getInstance().textureManager.register(locatable.identifier, texture)
            if (texture is OuterFileTexture) {
                texture.load()
            }
            ResourceCleanupHelper.registerBiCleanup(
                locatable,
                locatable.identifier,
                locatable.resolution
            ) { id, num ->
                expiredTextures[texture] = ReferenceIntMutablePair.of(id, num)
            }
            locatable.markRegistered()
        }
    }

    class TextureLocatable : IResourceLocatable {
        val identifier: Identifier
        val resolution: Int
        var suffixTextures: MutableList<IResourceLocatable>? = null

        @Volatile
        var registered: Boolean = false
            private set

        constructor(identifier: Identifier, resolution: Int) {
            this.identifier = identifier
            this.resolution = resolution
            this.registered = false
        }

        constructor(resolution: Int) : this(
            NameSpaces.MOD.path("textures/${++textureCounter}"),
            resolution
        )

        override fun getResourceLocation(): Identifier? {
            return if (registered) identifier else null
        }

        fun markRegistered() {
            registered = true
        }
    }
}