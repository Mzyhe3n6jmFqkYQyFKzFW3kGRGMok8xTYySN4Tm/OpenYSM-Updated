package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft

open class RotationToCamera : ContextFunction<Any>() {
    open fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any {
        var args: Int = arguments.getAsInt(context, 0)
        if (args < 0 || args > 1) {
            return null
        }
        var mainCamera: Camera = Minecraft.getInstance().gameRenderer.getMainCamera()
        if (args == 0) {
            return -mainCamera.xRot()
        }
        return 180.0f + mainCamera.yRot()
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}