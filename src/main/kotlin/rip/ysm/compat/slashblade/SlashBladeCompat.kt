package rip.ysm.compat.slashblade

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.slashblade.fabric.SlashBladeCompatImpl

object SlashBladeCompat : ModCompat("slashblade") {
    fun isSlashBladeItem(stack: ItemStack): Boolean = isModLoaded && SlashBladeCompatImpl.isSlashBladeItem(stack)

    fun hasSlashBlade(livingEntity: LivingEntity): Boolean =
        isModLoaded && SlashBladeCompatImpl.hasSlashBlade(livingEntity)

    fun isCarry(livingEntity: LivingEntity): Boolean = isModLoaded && SlashBladeCompatImpl.isCarry(livingEntity)

    fun registerControllerFunctions(binding: CtrlBinding) {
        if (!isModLoaded) return
        SlashBladeCompatImpl.registerControllerFunctions(binding)
    }

    fun handleSlashBladeAnim(
        player: Player,
        event: AnimationEvent<CustomPlayerEntity>,
        str: String,
        loopType: ILoopType
    ): PlayState? {
        if (!isModLoaded) return null
        return SlashBladeCompatImpl.handleSlashBladeAnim(player, event, str, loopType)
    }

    fun getComboAnimName(event: AnimationEvent<LivingAnimatable<*>>): String {
        if (!isModLoaded) return ""
        return SlashBladeCompatImpl.getComboAnimName(event)
    }
}
