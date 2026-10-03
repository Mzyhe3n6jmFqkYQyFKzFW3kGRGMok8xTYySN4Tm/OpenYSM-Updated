package rip.ysm.api

import net.minecraft.server.MinecraftServer
import rip.ysm.api.fabric.PlatformAPIImpl
import java.nio.file.Path

object PlatformAPI {
    @JvmStatic
    fun isServer(): Boolean = PlatformAPIImpl.isServer()

    @JvmStatic
    fun getPlatformName(): String = PlatformAPIImpl.getPlatformName()

    @JvmStatic
    fun getConfigFolder(): Path = PlatformAPIImpl.getConfigFolder()

    @JvmStatic
    fun getGameFolder(): Path = PlatformAPIImpl.getGameFolder()

    @JvmStatic
    fun isModLoaded(modId: String): Boolean = PlatformAPIImpl.isModLoaded(modId)

    @JvmStatic
    fun getModVersion(modId: String): String = PlatformAPIImpl.getModVersion(modId)

    @JvmStatic
    fun isDevelopmentEnvironment(): Boolean = PlatformAPIImpl.isDevelopmentEnvironment()

    @JvmStatic
    fun getServer(): MinecraftServer? = PlatformAPIImpl.getServer()
}
