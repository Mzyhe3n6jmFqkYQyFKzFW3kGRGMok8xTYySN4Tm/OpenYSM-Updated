package rip.ysm.compat.immersivemelodies

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.immersivemelodies.fabric.ImmersiveMelodiesCompatImpl

object ImmersiveMelodiesCompat {
    class ImmersiveMelodiesData {
        @JvmField var pitch: Float = 0f
        @JvmField var volume: Float = 0f
        @JvmField var current: Float = 0f
        @JvmField var delta: Long = 0L
        @JvmField var time: Long = 0L
    }

    @JvmStatic
    fun isLoaded(): Boolean = ImmersiveMelodiesCompatImpl.isLoaded()

    @JvmStatic
    fun updateMelodyProgress(livingEntity: LivingEntity, imData: ImmersiveMelodiesData) {
        ImmersiveMelodiesCompatImpl.updateMelodyProgress(livingEntity, imData)
    }

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        ImmersiveMelodiesCompatImpl.registerBindings(binding)
    }
}
