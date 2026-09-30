package rip.ysm.api.config

import net.minecraftforge.common.ForgeConfigSpec
import net.neoforged.fml.config.ModConfig
import rip.ysm.api.config.fabric.ConfigRegistrationImpl

object ConfigRegistration {
    @JvmStatic
    fun register(modId: String, type: ModConfig.Type, spec: ForgeConfigSpec) {
        ConfigRegistrationImpl.register(modId, type, spec)
    }
}
