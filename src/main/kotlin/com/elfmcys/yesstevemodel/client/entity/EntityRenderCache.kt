package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.config.GeneralConfig
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.projectile.Projectile
import java.lang.ref.WeakReference

object EntityRenderCache {
    private val weakRefs: ReferenceArrayList<WeakReference<GeoEntity<*>>> = ReferenceArrayList(64)
    private val strongRefs: ReferenceArrayList<GeoEntity<*>> = ReferenceArrayList(16)

    @JvmStatic
    fun register(entity: GeoEntity<*>) {
        weakRefs.add(WeakReference(entity))
    }

    @JvmStatic
    fun tick(partialTick: Float) {
        if (Minecraft.getInstance().player == null) {
            return
        }
        val it = weakRefs.iterator()
        while (it.hasNext()) {
            val geoEntity = it.next().get()
            when {
                geoEntity == null -> {
                    it.remove()
                }

                !geoEntity.isDebugMode -> {
                    it.remove()
                }

                else -> {
                    geoEntity.tickModel()
                    if (geoEntity.supportsAsync() && geoEntity.isModelInitialized && geoEntity.isModelReady()) {
                        val entity = geoEntity.entity
                        when {
                            entity is AbstractClientPlayer -> {
                                when {
                                    entity is LocalPlayer -> {
                                        if (!GeneralConfig.DISABLE_SELF_MODEL.get()) {
                                            geoEntity.submitAsyncUpdate(partialTick)
                                            strongRefs.add(geoEntity)
                                        }
                                    }

                                    !GeneralConfig.DISABLE_OTHER_MODEL.get() -> {
                                        geoEntity.submitAsyncUpdate(partialTick)
                                        strongRefs.add(geoEntity)
                                    }
                                }
                            }

                            entity is Projectile -> {
                                if (!GeneralConfig.DISABLE_PROJECTILE_MODEL.get()) {
                                    geoEntity.submitAsyncUpdate(partialTick)
                                    strongRefs.add(geoEntity)
                                }
                            }

                            !GeneralConfig.DISABLE_VEHICLE_MODEL.get() -> {
                                geoEntity.submitAsyncUpdate(partialTick)
                                strongRefs.add(geoEntity)
                            }
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun clear() {
        val it = strongRefs.iterator()
        while (it.hasNext()) {
            runCatching {
                it.next().awaitAsyncResult()
            }.onFailure { th ->
                th.printStackTrace()
            }
        }
        strongRefs.clear()
    }
}