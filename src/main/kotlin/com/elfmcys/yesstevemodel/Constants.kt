@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel

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

    const val MOD_ID: String = "yes_steve_model"
    const val MOD_NAME: String = "Open YSM"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MOD_NAME)

    @JvmStatic
    val ConfigDir: Path by lazy {
        val dir = FabricLoader.getInstance().configDir.resolve(NameSpaces.MOD())
        dir.createDirectories()
        dir
    }

    val ForceInitialize: Unit by lazy {
        doNothing()
    }

    private var getServer: MinecraftServer? = null

    val Server: MinecraftServer
        get() = getServer ?: error("Server is not initialized")

    private fun doNothing(vararg objects: Any) {}
}