@file:Suppress("unused")

package rip.ysm.compat.sbackpack

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat
import rip.ysm.compat.sbackpack.fabric.SBackpackCompatImpl

object SBackpackCompat : ModCompat("sophisticatedbackpacks") {
    @JvmStatic
    fun setupRenderLayers() {
        if (!isModLoaded) return
        SBackpackCompatImpl.setupRenderLayers()
    }

    @JvmStatic
    val inCompatibleInfo: Pair<String, String>?
        get() {
            if (!isModLoaded) return null
            return SBackpackCompatImpl.inCompatibleInfo
        }

    @JvmStatic
    fun getBackpack(livingEntity: LivingEntity): ItemStack {
        if (!isModLoaded) return ItemStack.EMPTY
        return SBackpackCompatImpl.getBackpack(livingEntity)
    }

    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
        if (!isModLoaded) return
        SBackpackCompatImpl.registerControllerFunctions(binding)
    }
}
