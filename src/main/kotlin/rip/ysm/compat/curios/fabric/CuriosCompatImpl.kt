package rip.ysm.compat.curios.fabric

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.ModCompat

object CuriosCompatImpl : ModCompat("trinkets", "accessories") {
    fun hasItemInSlot(livingEntity: LivingEntity, str: String, set: ReferenceOpenHashSet<Item>): Boolean = false

    fun hasTaggedItemInSlot(livingEntity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean = false

    fun hasNoTaggedItemInSlot(entity: LivingEntity, str: String, list: List<TagKey<Item>>): Boolean = false

    fun registerCuriosItems(binding: ContextBinding) {
    }
}
