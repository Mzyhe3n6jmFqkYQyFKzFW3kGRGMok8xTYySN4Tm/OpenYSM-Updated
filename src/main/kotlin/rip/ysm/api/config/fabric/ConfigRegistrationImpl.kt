package rip.ysm.api.config.fabric

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry
import net.minecraftforge.common.ForgeConfigSpec
import net.neoforged.fml.config.ModConfig

object ConfigRegistrationImpl {
    @JvmStatic
    fun register(modId: String, type: ModConfig.Type, spec: ForgeConfigSpec) {
        ConfigRegistry.INSTANCE.register(modId, type, spec)
    }
}
