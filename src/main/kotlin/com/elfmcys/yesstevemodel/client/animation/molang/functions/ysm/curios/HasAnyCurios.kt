package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.curios

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import com.elfmcys.yesstevemodel.util.ThreadLocalItemTagSets
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.curios.CuriosCompat
import kotlin.jvm.optionals.getOrNull

class HasAnyCurios : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        val type: String = arguments.getAsString(context, 0) ?: return null
        if (type.isEmpty()) {
            return null
        }
        val referenceOpenHashSet: ReferenceOpenHashSet<Item> = ThreadLocalItemTagSets.ITEM_SET.get()
        referenceOpenHashSet.clear()
        for (i in 1 until arguments.size()) {
            val name = arguments.getResourceLocation(context, i) ?: return null
            val item = BuiltInRegistries.ITEM.get(name).map(Holder<Item>::value).getOrNull()
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