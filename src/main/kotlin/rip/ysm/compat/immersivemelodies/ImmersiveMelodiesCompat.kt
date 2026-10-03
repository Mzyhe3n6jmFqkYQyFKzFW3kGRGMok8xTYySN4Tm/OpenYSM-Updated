package rip.ysm.compat.immersivemelodies

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.immersivemelodies.fabric.ImmersiveMelodiesCompatImpl

object ImmersiveMelodiesCompat : ModCompat("immersive_melodies") {
    class ImmersiveMelodiesData {
        @JvmField
        var pitch: Float = 0f

        @JvmField
        var volume: Float = 0f

        @JvmField
        var current: Float = 0f

        @JvmField
        var delta: Long = 0L

        @JvmField
        var time: Long = 0L
    }

    @JvmStatic
    fun updateMelodyProgress(livingEntity: LivingEntity, imData: ImmersiveMelodiesData) {
        if (!isModLoaded) return
        ImmersiveMelodiesCompatImpl.updateMelodyProgress(livingEntity, imData)
    }

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        if (!isModLoaded) return
        ImmersiveMelodiesCompatImpl.registerBindings(binding)
    }
}
