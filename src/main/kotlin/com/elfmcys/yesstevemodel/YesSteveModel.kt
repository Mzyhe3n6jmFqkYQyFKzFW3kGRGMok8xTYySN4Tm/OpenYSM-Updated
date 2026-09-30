package com.elfmcys.yesstevemodel

import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.config.ModSoundEvents
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.event.YsmEventBootstrap
import com.elfmcys.yesstevemodel.util.obfuscate.Keep
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.neoforged.fml.config.ModConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import rip.ysm.api.PlatformAPI
import rip.ysm.api.config.ConfigRegistration
import java.io.File
import java.io.IOException

object YesSteveModel {
    const val MOD_ID = "yes_steve_model"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    @JvmField
    val GSON: Gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

    @JvmStatic
    fun init() {
        LOGGER.info("Initializing YesSteveModel, platform: {}", PlatformAPI.getPlatformName())
        try {
            NativeLibLoader.init()
        } catch (e: IOException) {
            LOGGER.error("Failed to initialize native lib", e)
        }
        if (!NativeLibLoader.isAvailable()) {
            LOGGER.error(getErrorMessage())
        } else {
            initConfig()
        }
        YsmEventBootstrap.register()
    }

    @Suppress("DEPRECATION", "removal")
    private fun initConfig() {
        val oldConfig: File = PlatformAPI.getConfigFolder().resolve("yes_steve_model-common.toml").toFile()
        if (oldConfig.isFile) {
            val file2: File = PlatformAPI.getConfigFolder().resolve("yes_steve_model-client.toml").toFile()
            if (!file2.isFile) {
                oldConfig.renameTo(file2)
            } else {
                oldConfig.delete()
            }
        }
        ConfigRegistration.register(MOD_ID, ModConfig.Type.CLIENT, GeneralConfig.buildSpec())
        ConfigRegistration.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.buildSpec())
        if (!PlatformAPI.isServer()) {
            ModSoundEvents.register()
        }
    }

    @Keep
    @JvmStatic
    fun isAvailable(): Boolean {
        return NativeLibLoader.isAvailable()
    }

    @JvmStatic
    fun isOnAndroid(): Boolean {
        return NativeLibLoader.isOnAndroid()
    }

    @Environment(EnvType.CLIENT)
    @JvmStatic
    fun sendUnavailableMessage() {
        val localPlayer: LocalPlayer? = Minecraft.getInstance().player
        if (localPlayer != null) {
            localPlayer.displayClientMessage(getUnavailableComponent(), false)
        }
    }

    @JvmStatic
    fun getUnavailableComponent(): Component {
        return NativeLibLoader.getErrorComponent()
    }

    @JvmStatic
    fun getErrorMessage(): String {
        return NativeLibLoader.getErrorMessage()
    }
}
