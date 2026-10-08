package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.world.entity.Entity

class DumpRelativeBlock : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any? {
        if (!context.entity.isDebugMode) return null
        val blockState = MolangUtils.getRelativeBlockState(context, arguments) ?: return null
        val key = BuiltInRegistries.BLOCK.getKey(blockState.block)
        context.entity.logWarningComponent(
            Component.literal("Display ").append(ComponentUtils.copyOnClickText(blockState.block.name.getString(99)))
        )
        context.entity
            .logWarningComponent(Component.literal("Name ").append(ComponentUtils.copyOnClickText(key.toString())))
        blockState.tags.forEach { tagKey ->
            context.entity.logWarningComponent(
                Component.literal("Tag ").append(ComponentUtils.copyOnClickText(tagKey.location().toString()))
            )
        }
        return null
    }

    override fun validateArgumentSize(size: Int): Boolean = size == 3
}