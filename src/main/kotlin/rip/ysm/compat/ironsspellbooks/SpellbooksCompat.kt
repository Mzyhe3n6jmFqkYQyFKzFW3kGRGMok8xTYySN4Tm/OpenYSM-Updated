package rip.ysm.compat.ironsspellbooks

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.ironsspellbooks.fabric.SpellbooksCompatImpl

object SpellbooksCompat : ModCompat("irons_spellbooks") {
    fun registerBindings(binding: CtrlBinding) {
        if (!isModLoaded) return
        SpellbooksCompatImpl.registerBindings(binding)
    }

    fun resolvePlayState(event: AnimationEvent<LivingAnimatable<*>>, entity: LivingEntity): PlayState? {
        if (!isModLoaded) return null
        return SpellbooksCompatImpl.resolvePlayState(event, entity)
    }
}
