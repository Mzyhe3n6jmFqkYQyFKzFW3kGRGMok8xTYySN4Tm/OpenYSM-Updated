package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.commands.arguments.ParticleArgument
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Environment(EnvType.CLIENT)
object ParticleEffectUtil {
    private val particleCache: Cache<String, ParticleOptions> =
        CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build()

    @JvmStatic
    @Throws(ExecutionException::class, CommandSyntaxException::class)
    fun handleParticle(
        context: ExecutionContext<IContext<Entity>>,
        arguments: Function.ArgumentCollection,
        isAbsolute: Boolean
    ): Boolean {
        val particleId = arguments.getAsString(context, 0)
        if (particleId.isNullOrBlank()) {
            return false
        }

        val offset = Vector3d(0.0, 0.0, 0.0)
        val delta = Vector3d(0.0, 0.0, 0.0)
        var speed = 0.0
        var count = 0
        var lifetime = 20
        val argCount = arguments.size()

        if (argCount > 1) offset.x = arguments.getAsDouble(context, 1)
        if (argCount > 2) offset.y = arguments.getAsDouble(context, 2)
        if (argCount > 3) offset.z = arguments.getAsDouble(context, 3)
        if (argCount > 4) delta.x = arguments.getAsDouble(context, 4)
        if (argCount > 5) delta.y = arguments.getAsDouble(context, 5)
        if (argCount > 6) delta.z = arguments.getAsDouble(context, 6)
        if (argCount > 7) speed = arguments.getAsDouble(context, 7)
        if (argCount > 8) count = max(arguments.getAsInt(context, 8), 0)
        if (argCount > 9) lifetime = max(arguments.getAsInt(context, 9), 1)

        context.entity().random()?.let {
            spawnParticles(
                context.entity().entity(),
                particleId,
                offset,
                delta,
                speed,
                count,
                lifetime,
                isAbsolute,
                it
            )
        }
        return true
    }

    private fun spawnParticles(
        entity: Entity,
        particleId: String,
        offset: Vector3d,
        delta: Vector3d,
        speed: Double,
        count: Int,
        lifetime: Int,
        isAbsolute: Boolean,
        random: RandomSource
    ) {
        val level = Minecraft.getInstance().level ?: return
        val particleOptions = runCatching {
            particleCache.get(particleId) {
                ParticleArgument.readParticle(StringReader(particleId), level.registryAccess())
            }
        }.getOrNull() ?: return

        val particleEngine: ParticleEngine = Minecraft.getInstance().particleEngine

        if (count == 0) {
            var spawnPos = Vec3(offset.x(), offset.y(), offset.z())
            if (!isAbsolute) {
                spawnPos = if (entity is Player) {
                    spawnPos.yRot((-entity.yBodyRot) * 0.017453292f)
                } else {
                    spawnPos.yRot((-entity.yRot) * 0.017453292f)
                }
            }

            val x = entity.x + spawnPos.x()
            val y = entity.y + spawnPos.y()
            val z = entity.z + spawnPos.z()
            val velocityX = speed * delta.x()
            val velocityY = speed * delta.y()
            val velocityZ = speed * delta.z()

            Minecraft.getInstance().execute {
                val particle = particleEngine.createParticle(particleOptions, x, y, z, velocityX, velocityY, velocityZ)
                particle?.setLifetime(lifetime)
            }
            return
        }

        repeat(count) {
            emitParticle(entity, offset, delta, speed, lifetime, particleEngine, particleOptions, isAbsolute, random)
        }
    }

    private fun emitParticle(
        entity: Entity,
        offset: Vector3d,
        delta: Vector3d,
        speed: Double,
        lifetime: Int,
        particleEngine: ParticleEngine,
        particleOptions: ParticleOptions,
        isAbsolute: Boolean,
        random: RandomSource
    ) {
        val spreadX = random.nextGaussian() * delta.x()
        val spreadY = random.nextGaussian() * delta.y()
        val spreadZ = random.nextGaussian() * delta.z()
        val velocityX = random.nextGaussian() * speed
        val velocityY = random.nextGaussian() * speed
        val velocityZ = random.nextGaussian() * speed

        var spawnPos = Vec3(offset.x() + spreadX, offset.y() + spreadY, offset.z() + spreadZ)
        if (!isAbsolute) {
            spawnPos = spawnPos.yRot((-entity.yRot) * 0.017453292f)
        }

        val x = entity.x + spawnPos.x()
        val y = entity.y + spawnPos.y()
        val z = entity.z + spawnPos.z()

        Minecraft.getInstance().execute {
            val particle = particleEngine.createParticle(particleOptions, x, y, z, velocityX, velocityY, velocityZ)
            particle?.setLifetime(lifetime)
        }
    }
}