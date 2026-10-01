package rip.ysm.compat.curios.fabric

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item

object CuriosCompatImpl {
    @JvmStatic
    fun hasItemInSlot(livingEntity: LivingEntity, str: String, set: ReferenceOpenHashSet<Item>): Boolean = false

    @JvmStatic
    fun hasTaggedItemInSlot(livingEntity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean = false

    @JvmStatic
    fun hasNoTaggedItemInSlot(entity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean = false

    @JvmStatic
    fun registerCuriosItems(binding: ContextBinding) {
    }
}
