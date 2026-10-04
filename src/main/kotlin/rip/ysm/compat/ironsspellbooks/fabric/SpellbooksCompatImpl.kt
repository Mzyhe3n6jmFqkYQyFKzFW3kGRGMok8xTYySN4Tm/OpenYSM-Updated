package rip.ysm.compat.ironsspellbooks.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat

object SpellbooksCompatImpl : ModCompat("irons_spellbooks") {
    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
    }

    @JvmStatic
    fun resolvePlayState(event: AnimationEvent<LivingAnimatable<*>>, entity: LivingEntity): PlayState? = null
}
