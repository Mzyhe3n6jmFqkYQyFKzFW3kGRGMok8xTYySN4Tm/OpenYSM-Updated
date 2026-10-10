@file:Suppress("unused")

package rip.ysm.api

import net.minecraft.server.MinecraftServer
import rip.ysm.api.fabric.PlatformAPIImpl
import java.nio.file.Path

object PlatformAPI {
    val isServer: Boolean
        get() = PlatformAPIImpl.isServer

    val platformName: String
        get() = PlatformAPIImpl.platformName

    val configFolder: Path
        get() = PlatformAPIImpl.configFolder

    val gameFolder: Path
        get() = PlatformAPIImpl.gameFolder

    fun isModLoaded(modId: String): Boolean = PlatformAPIImpl.isModLoaded(modId)

    fun getModVersion(modId: String): String = PlatformAPIImpl.getModVersion(modId)

    val isDevelopmentEnvironment: Boolean
        get() = PlatformAPIImpl.isDevelopmentEnvironment

    val server: MinecraftServer?
        get() = PlatformAPIImpl.server
}
