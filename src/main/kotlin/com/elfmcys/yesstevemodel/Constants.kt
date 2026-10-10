@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel

import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path
import kotlin.io.path.createDirectories

@Suppress("unused")
object Constants {
    init {
        runCatching {
            ServerLifecycleEvents.SERVER_STARTING.register { getServer = it }
            ServerLifecycleEvents.SERVER_STOPPED.register { getServer = null }
        }
    }

    const val MOD_NAME: String = "Open YSM"
    const val FORMAT_VERSION: Int = 40

    val LOGGER: Logger = LogManager.getLogger(MOD_NAME)

    val ConfigDir: Path by lazy {
        val dir = MainConfigDir.resolve(NameSpaces.MOD())
        runCatching { dir.createDirectories() }
        dir
    }

    val MainConfigDir: Path by lazy {
        val dir = runCatching {
            FabricLoader.getInstance().configDir
        }.getOrElse {
            Path.of(System.getProperty("fabric.config.dir") ?: ".temp/config")
        }
        runCatching { dir.createDirectories() }
        dir
    }

    val IsServer: Boolean
        get() = runCatching { FabricLoader.getInstance().environmentType == EnvType.SERVER }.getOrDefault(false)

    val ForceInitialize: Unit by lazy {
        doNothing()
    }

    @Volatile
    private var getServer: MinecraftServer? = null

    val Server: MinecraftServer
        get() = getServer ?: error("Server is not initialized")

    fun doNothing(vararg objects: Any) {}
}