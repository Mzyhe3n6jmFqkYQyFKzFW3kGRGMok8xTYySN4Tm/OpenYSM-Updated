package rip.ysm.compat.curios

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.curios.fabric.CuriosCompatImpl

object CuriosCompat {
    @JvmStatic
    fun isLoaded(): Boolean = CuriosCompatImpl.isLoaded()

    @JvmStatic
    fun hasItemInSlot(livingEntity: LivingEntity, str: String, set: ReferenceOpenHashSet<Item>): Boolean =
        CuriosCompatImpl.hasItemInSlot(livingEntity, str, set)

    @JvmStatic
    fun hasTaggedItemInSlot(livingEntity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean =
        CuriosCompatImpl.hasTaggedItemInSlot(livingEntity, str, list)

    @JvmStatic
    fun hasNoTaggedItemInSlot(entity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean =
        CuriosCompatImpl.hasNoTaggedItemInSlot(entity, str, list)

    @JvmStatic
    fun registerCuriosItems(binding: ContextBinding) {
        CuriosCompatImpl.registerCuriosItems(binding)
    }
}
