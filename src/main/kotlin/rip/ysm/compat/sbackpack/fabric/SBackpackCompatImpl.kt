package rip.ysm.compat.sbackpack.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat

object SBackpackCompatImpl : ModCompat("sophisticatedbackpacks") {
    fun setupRenderLayers() {
    }

    val inCompatibleInfo: Pair<String, String>?
        get() = null

    fun getBackpack(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY

    fun registerControllerFunctions(binding: CtrlBinding) {
    }
}
