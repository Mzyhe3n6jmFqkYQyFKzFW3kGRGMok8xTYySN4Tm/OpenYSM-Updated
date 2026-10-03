package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import java.util.Optional

class ModVersion : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any? {
        val modid: String = arguments.getAsString(context, 0) ?: return null
        val container: Optional<ModContainer> = FabricLoader.getInstance().getModContainer(modid)
        if (container.isEmpty) {
            return null
        }
        return container.get().metadata.version.friendlyString
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}