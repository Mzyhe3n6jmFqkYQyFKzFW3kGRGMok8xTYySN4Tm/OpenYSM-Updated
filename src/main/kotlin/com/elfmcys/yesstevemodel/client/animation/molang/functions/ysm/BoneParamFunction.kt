package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.world.entity.Entity

abstract class BoneParamFunction : EntityFunction() {
    abstract fun getParam(bone: IBone): Vec3fStruct

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }

    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any? {
        val name: Int = arguments.getStringId(context, 0)
        if (name == StringPool.EMPTY_ID) {
            return null
        }
        val bone: IBone = context.entity().geoInstance().getBone(name) ?: return null
        return getParam(bone)
    }
}