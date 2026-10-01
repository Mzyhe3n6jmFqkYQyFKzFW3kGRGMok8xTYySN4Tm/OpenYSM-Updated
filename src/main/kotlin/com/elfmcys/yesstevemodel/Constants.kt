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
        ServerLifecycleEvents.SERVER_STARTING.register { getServer = it }
        ServerLifecycleEvents.SERVER_STOPPED.register { getServer = null }
    }

    const val MOD_NAME: String = "Open YSM"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MOD_NAME)

    @JvmStatic
    val ConfigDir: Path by lazy {
        val dir = FabricLoader.getInstance().configDir.resolve(NameSpaces.MOD())
        dir.createDirectories()
        dir
    }

    @JvmStatic
    val MainConfigDir: Path by lazy {
        val dir = FabricLoader.getInstance().configDir
        dir.createDirectories()
        dir
    }

    @JvmStatic
    val IsServer: Boolean
        get() = FabricLoader.getInstance().environmentType == EnvType.SERVER

    val ForceInitialize: Unit by lazy {
        doNothing()
    }

    @Volatile
    private var getServer: MinecraftServer? = null

    val Server: MinecraftServer
        get() {
            return getServer ?: error("Server is not initialized")
        }

    private fun doNothing(vararg objects: Any) {}
}