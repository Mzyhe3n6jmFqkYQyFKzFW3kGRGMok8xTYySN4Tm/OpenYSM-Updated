package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import rip.ysm.api.PlatformAPI
import java.util.concurrent.TimeUnit

object EntityJoinCallbackEvent {
    private val callbackCache: Cache<Int, MutableList<(Entity) -> Unit>> =
        CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build()

    init {
        register()
    }

    private fun register() {
        if (PlatformAPI.isServer()) {
            return
        }
        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (!YesSteveModel.isAvailable()) {
                return@register
            }
            val list = callbackCache.getIfPresent(entity.id)
            if (list != null) {
                for (entityConsumer in list) {
                    entityConsumer(entity)
                }
            }
            callbackCache.invalidate(entity.id)
        }
    }

    @JvmStatic
    fun addCallback(i: Int, callback: (Entity) -> Unit) {
        Minecraft.getInstance().execute {
            val clientLevel = Minecraft.getInstance().level
            if (clientLevel != null) {
                val entity = clientLevel.getEntity(i)
                if (entity != null) {
                    callback(entity)
                } else {
                    addToCallbackList(i, callback)
                }
            }
        }
    }

    private fun addToCallbackList(i: Int, callback: (Entity) -> Unit) {
        var list = callbackCache.getIfPresent(i)
        if (list == null) {
            list = ArrayList(3)
            callbackCache.put(i, list)
        }
        list.add(callback)
    }
}
