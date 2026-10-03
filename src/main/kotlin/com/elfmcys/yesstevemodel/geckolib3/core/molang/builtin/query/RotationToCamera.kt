package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft

class RotationToCamera : ContextFunction<Any>() {
    override fun eval(context: ExecutionContext<IContext<Any>>, arguments: Function.ArgumentCollection): Any? {
        val args: Int = arguments.getAsInt(context, 0)
        if (args < 0 || args > 1) {
            return null
        }
        val mainCamera: Camera = Minecraft.getInstance().gameRenderer.mainCamera
        if (args == 0) {
            return -mainCamera.xRot()
        }
        return 180.0f + mainCamera.yRot()
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}