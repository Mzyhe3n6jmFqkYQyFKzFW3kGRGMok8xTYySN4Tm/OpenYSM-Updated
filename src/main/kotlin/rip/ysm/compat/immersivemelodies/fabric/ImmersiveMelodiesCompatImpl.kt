package rip.ysm.compat.immersivemelodies.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.immersivemelodies.ImmersiveMelodiesCompat.ImmersiveMelodiesData

object ImmersiveMelodiesCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("immersive_melodies")

    @JvmStatic
    fun updateMelodyProgress(livingEntity: LivingEntity, imData: ImmersiveMelodiesData) {
    }

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
    }
}
