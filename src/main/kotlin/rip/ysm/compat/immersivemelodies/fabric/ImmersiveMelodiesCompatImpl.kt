package rip.ysm.compat.immersivemelodies.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.immersivemelodies.ImmersiveMelodiesCompat.ImmersiveMelodiesData

object ImmersiveMelodiesCompatImpl : ModCompat("immersive_melodies") {
    fun updateMelodyProgress(livingEntity: LivingEntity, imData: ImmersiveMelodiesData) {
    }

    fun registerBindings(binding: CtrlBinding) {
    }
}
