package rip.ysm.compat.slashblade.fabric

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

object SlashBladeCompatImpl : ModCompat("slashblade") {
    fun isSlashBladeItem(stack: ItemStack): Boolean = false

    fun hasSlashBlade(livingEntity: LivingEntity): Boolean = false

    fun isCarry(livingEntity: LivingEntity): Boolean = false

    fun registerControllerFunctions(binding: CtrlBinding) {
    }

    fun handleSlashBladeAnim(
        player: Player,
        event: AnimationEvent<CustomPlayerEntity>,
        str: String,
        loopType: ILoopType
    ): PlayState? =
        null

    fun getComboAnimName(event: AnimationEvent<LivingAnimatable<*>>): String = ""
}
