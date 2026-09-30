package rip.ysm.compat.ironsspellbooks

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.ironsspellbooks.fabric.SpellbooksCompatImpl

object SpellbooksCompat : ModCompat("irons_spellbooks") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        SpellbooksCompatImpl.registerBindings(binding)
    }

    @JvmStatic
    fun resolvePlayState(event: AnimationEvent<LivingAnimatable<*>>, entity: LivingEntity): PlayState? =
        SpellbooksCompatImpl.resolvePlayState(event, entity)
}
