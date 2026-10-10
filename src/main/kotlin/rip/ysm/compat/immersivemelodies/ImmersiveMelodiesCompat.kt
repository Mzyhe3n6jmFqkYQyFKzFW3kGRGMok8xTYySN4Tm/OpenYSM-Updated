package rip.ysm.compat.immersivemelodies

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.immersivemelodies.fabric.ImmersiveMelodiesCompatImpl

object ImmersiveMelodiesCompat : ModCompat("immersive_melodies") {
    class ImmersiveMelodiesData {
        var pitch: Float = 0f

        var volume: Float = 0f

        var current: Float = 0f

        var delta: Long = 0L

        var time: Long = 0L
    }

    fun updateMelodyProgress(livingEntity: LivingEntity, imData: ImmersiveMelodiesData) {
        if (!isModLoaded) return
        ImmersiveMelodiesCompatImpl.updateMelodyProgress(livingEntity, imData)
    }

    fun registerBindings(binding: CtrlBinding) {
        if (!isModLoaded) return
        ImmersiveMelodiesCompatImpl.registerBindings(binding)
    }
}
