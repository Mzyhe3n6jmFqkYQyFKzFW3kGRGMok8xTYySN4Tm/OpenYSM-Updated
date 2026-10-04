package rip.ysm.compat.sbackpack.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat
import java.util.*

object SBackpackCompatImpl : ModCompat("sophisticatedbackpacks") {
    @JvmStatic
    fun setupRenderLayers() {
    }

    @JvmStatic
    fun getInCompatibleInfo(): Optional<Pair<String, String>> = Optional.empty()

    @JvmStatic
    fun getBackpack(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY

    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
    }
}
