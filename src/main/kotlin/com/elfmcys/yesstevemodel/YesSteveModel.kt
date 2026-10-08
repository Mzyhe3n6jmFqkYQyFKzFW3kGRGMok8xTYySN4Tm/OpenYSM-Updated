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
import net.minecraft.network.chat.Component
import net.neoforged.fml.config.ModConfig
import rip.ysm.api.PlatformAPI
import rip.ysm.api.config.ConfigRegistration

object YesSteveModel {
    @JvmField
    val GSON: Gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

    init {
        Constants.LOGGER.info("Initializing YesSteveModel, platform: {}", PlatformAPI.platformName)
        runCatching { NativeLibLoader.init() }.onFailure {
            Constants.LOGGER.error(
                "Failed to initialize native lib",
                it
            )
        }
        if (!NativeLibLoader.isAvailable()) {
            Constants.LOGGER.error(getErrorMessage())
        } else {
            initConfig()
        }
        Constants.doNothing(YsmEventBootstrap)
    }

    private fun initConfig() {
        val oldConfig = Constants.MainConfigDir.resolve("yes_steve_model-common.toml").toFile()
        if (oldConfig.isFile) {
            val file2 = Constants.MainConfigDir.resolve("yes_steve_model-client.toml").toFile()
            if (!file2.isFile) {
                oldConfig.renameTo(file2)
            } else {
                oldConfig.delete()
            }
        }
        ConfigRegistration.register(NameSpaces.MOD(), ModConfig.Type.CLIENT, GeneralConfig.buildSpec())
        ConfigRegistration.register(NameSpaces.MOD(), ModConfig.Type.SERVER, ServerConfig.buildSpec())
        if (!PlatformAPI.isServer) ModSoundEvents.register()
    }

    @Keep
    @JvmStatic
    fun isAvailable(): Boolean = NativeLibLoader.isAvailable()

    @JvmStatic
    fun isOnAndroid(): Boolean = NativeLibLoader.isOnAndroid()

    @Environment(EnvType.CLIENT)
    @JvmStatic
    fun sendUnavailableMessage() {
        val localPlayer = Minecraft.getInstance().player
        getUnavailableComponent()?.let { localPlayer?.displayClientMessage(it, false) }
    }

    @JvmStatic
    fun getUnavailableComponent(): Component? = NativeLibLoader.getErrorComponent()

    @JvmStatic
    fun getErrorMessage(): String? = NativeLibLoader.getErrorMessage()
}
