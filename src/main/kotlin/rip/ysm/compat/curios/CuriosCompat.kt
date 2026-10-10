package rip.ysm.compat.curios

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.ModCompat
import rip.ysm.compat.curios.fabric.CuriosCompatImpl

object CuriosCompat : ModCompat("trinkets", "accessories") {
    fun hasItemInSlot(livingEntity: LivingEntity, str: String, set: ReferenceOpenHashSet<Item>): Boolean {
        return isModLoaded && CuriosCompatImpl.hasItemInSlot(livingEntity, str, set)
    }

    fun hasTaggedItemInSlot(livingEntity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean {
        return isModLoaded && CuriosCompatImpl.hasTaggedItemInSlot(livingEntity, str, list)
    }

    fun hasNoTaggedItemInSlot(entity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean {
        return isModLoaded && CuriosCompatImpl.hasNoTaggedItemInSlot(entity, str, list)
    }

    fun registerCuriosItems(binding: ContextBinding) {
        if (!isModLoaded) return
        CuriosCompatImpl.registerCuriosItems(binding)
    }
}
