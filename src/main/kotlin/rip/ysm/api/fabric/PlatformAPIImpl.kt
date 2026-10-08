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
    val isServer: Boolean
        get() = FabricLoader.getInstance().environmentType == EnvType.SERVER

    @JvmStatic
    val platformName: String
        get() = "Fabric"

    @JvmStatic
    val configFolder: Path
        get() = FabricLoader.getInstance().configDir

    @JvmStatic
    val gameFolder: Path
        get() = FabricLoader.getInstance().gameDir

    @JvmStatic
    fun isModLoaded(modId: String): Boolean = FabricLoader.getInstance().isModLoaded(modId)

    @JvmStatic
    fun getModVersion(modId: String): String = FabricLoader.getInstance().getModContainer(modId)
        .orElse(null)?.metadata?.version?.friendlyString ?: "unknown"

    @JvmStatic
    val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    @JvmStatic
    val server: MinecraftServer?
        get() {
            if (currentServer != null) return currentServer
            if (!isServer)
                return runCatching { Minecraft.getInstance().singleplayerServer }.getOrNull()
            return null
        }
}
