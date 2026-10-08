@file:Suppress("unused")

package rip.ysm.api

import net.minecraft.server.MinecraftServer
import rip.ysm.api.fabric.PlatformAPIImpl
import java.nio.file.Path

object PlatformAPI {
    @JvmStatic
    val isServer: Boolean
        get() = PlatformAPIImpl.isServer

    @JvmStatic
    val platformName: String
        get() = PlatformAPIImpl.platformName

    @JvmStatic
    val configFolder: Path
        get() = PlatformAPIImpl.configFolder

    @JvmStatic
    val gameFolder: Path
        get() = PlatformAPIImpl.gameFolder

    @JvmStatic
    fun isModLoaded(modId: String): Boolean = PlatformAPIImpl.isModLoaded(modId)

    @JvmStatic
    fun getModVersion(modId: String): String = PlatformAPIImpl.getModVersion(modId)

    @JvmStatic
    val isDevelopmentEnvironment: Boolean
        get() = PlatformAPIImpl.isDevelopmentEnvironment

    @JvmStatic
    val server: MinecraftServer?
        get() = PlatformAPIImpl.server
}
