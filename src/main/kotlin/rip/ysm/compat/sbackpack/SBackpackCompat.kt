package rip.ysm.compat.sbackpack

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat
import rip.ysm.compat.sbackpack.fabric.SBackpackCompatImpl
import java.util.Optional

object SBackpackCompat : ModCompat("sophisticatedbackpacks") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun setupRenderLayers() {
        SBackpackCompatImpl.setupRenderLayers()
    }

    @JvmStatic
    fun getInCompatibleInfo(): Optional<Pair<String, String>> = SBackpackCompatImpl.getInCompatibleInfo()

    @JvmStatic
    fun getBackpack(livingEntity: LivingEntity): ItemStack = SBackpackCompatImpl.getBackpack(livingEntity)

    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
        SBackpackCompatImpl.registerControllerFunctions(binding)
    }
}
