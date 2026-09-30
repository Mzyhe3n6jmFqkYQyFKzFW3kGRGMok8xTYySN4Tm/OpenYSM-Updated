package rip.ysm.api.fabric

import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import java.nio.file.Path

object PlatformAPIImpl {
    @Volatile
    private var currentServer: MinecraftServer? = null

    init {
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            currentServer = server
        }
        ServerLifecycleEvents.SERVER_STOPPED.register {
            currentServer = null
        }
    }

    @JvmStatic
    fun isServer(): Boolean {
        return FabricLoader.getInstance().environmentType == EnvType.SERVER
    }

    @JvmStatic
    fun getPlatformName(): String {
        return "Fabric"
    }

    @JvmStatic
    fun getConfigFolder(): Path {
        return FabricLoader.getInstance().configDir
    }

    @JvmStatic
    fun getGameFolder(): Path {
        return FabricLoader.getInstance().gameDir
    }

    @JvmStatic
    fun isModLoaded(modId: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }

    @JvmStatic
    fun getModVersion(modId: String): String {
        return FabricLoader.getInstance().getModContainer(modId)
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }

    @JvmStatic
    fun isDevelopmentEnvironment(): Boolean {
        return FabricLoader.getInstance().isDevelopmentEnvironment
    }

    @JvmStatic
    fun getServer(): MinecraftServer? {
        if (currentServer != null) {
            return currentServer
        }
        if (!isServer()) {
            return try {
                Minecraft.getInstance().singleplayerServer
            } catch (e: Throwable) {
                null
            }
        }
        return null
    }
}
