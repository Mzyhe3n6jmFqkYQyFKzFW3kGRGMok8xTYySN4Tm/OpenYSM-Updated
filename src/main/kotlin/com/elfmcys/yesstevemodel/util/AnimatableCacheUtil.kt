package com.elfmcys.yesstevemodel.util

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import java.time.Duration

@Environment(EnvType.CLIENT)
object AnimatableCacheUtil {
    val ENTITIES_CACHE: Cache<Identifier, Entity> =
        CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build()
}