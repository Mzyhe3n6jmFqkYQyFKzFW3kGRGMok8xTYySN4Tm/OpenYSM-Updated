package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.curios

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import com.elfmcys.yesstevemodel.util.ThreadLocalItemTagSets
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.curios.CuriosCompat

class HasAnyCuriosWithAllTags : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        val type: String = arguments.getAsString(context, 0) ?: return null
        if (type.isEmpty()) {
            return null
        }
        val referenceArrayList: ReferenceArrayList<TagKey<Item>> = ThreadLocalItemTagSets.TAG_KEY_LIST.get()
        referenceArrayList.size(arguments.size() - 1)
        for (i in 1 until arguments.size()) {
            val tag: Identifier = arguments.getResourceLocation(context, i) ?: return null
            referenceArrayList[i - 1] = TagKey.create(Registries.ITEM, tag)
        }
        return CuriosCompat.hasNoTaggedItemInSlot(context.entity().entity(), type, referenceArrayList)
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size > 1
    }
}