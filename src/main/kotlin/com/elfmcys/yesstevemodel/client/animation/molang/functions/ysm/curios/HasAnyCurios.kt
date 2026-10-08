package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.curios

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import com.elfmcys.yesstevemodel.util.ThreadLocalItemTagSets
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.curios.CuriosCompat

class HasAnyCurios : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        val type: String = arguments.getAsString(context, 0) ?: return null
        if (type.isEmpty()) {
            return null
        }
        val referenceOpenHashSet: ReferenceOpenHashSet<Item> = ThreadLocalItemTagSets.ITEM_SET.get()
        referenceOpenHashSet.clear()
        for (i in 1 until arguments.size()) {
            val name: Identifier = arguments.getResourceLocation(context, i) ?: return null
            val item: Item? = BuiltInRegistries.ITEM.get(name).map(Holder<Item>::value).orElse(null)
            if (item != null) {
                referenceOpenHashSet.add(item)
            }
        }
        return CuriosCompat.hasItemInSlot(context.entity.entity, type, referenceOpenHashSet)
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size > 0
    }
}