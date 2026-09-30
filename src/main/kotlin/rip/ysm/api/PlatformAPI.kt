package rip.ysm.api

import net.minecraft.server.MinecraftServer
import rip.ysm.api.fabric.PlatformAPIImpl
import java.nio.file.Path

object PlatformAPI {
    @JvmStatic
    fun isServer(): Boolean {
        return PlatformAPIImpl.isServer()
    }

    @JvmStatic
    fun getPlatformName(): String {
        return PlatformAPIImpl.getPlatformName()
    }

    @JvmStatic
    fun getConfigFolder(): Path {
        return PlatformAPIImpl.getConfigFolder()
    }

    @JvmStatic
    fun getGameFolder(): Path {
        return PlatformAPIImpl.getGameFolder()
    }

    @JvmStatic
    fun isModLoaded(modId: String): Boolean {
        return PlatformAPIImpl.isModLoaded(modId)
    }

    @JvmStatic
    fun getModVersion(modId: String): String {
        return PlatformAPIImpl.getModVersion(modId)
    }

    @JvmStatic
    fun isDevelopmentEnvironment(): Boolean {
        return PlatformAPIImpl.isDevelopmentEnvironment()
    }

    @JvmStatic
    fun getServer(): MinecraftServer? {
        return PlatformAPIImpl.getServer()
    }
}
