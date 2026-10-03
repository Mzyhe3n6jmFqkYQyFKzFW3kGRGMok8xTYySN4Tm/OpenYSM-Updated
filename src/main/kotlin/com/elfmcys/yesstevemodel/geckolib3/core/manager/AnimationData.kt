package com.elfmcys.yesstevemodel.geckolib3.core.manager

import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList

open class AnimationData {
    @JvmField
    var limbSwing: Float = 0.0f
    @JvmField
    val animationControllers: MutableList<IAnimationController<*>> = ReferenceArrayList(0)
    @JvmField
    val animationControllerMap: Object2ReferenceOpenHashMap<String, IAnimationController<*>> =
        Object2ReferenceOpenHashMap(0)

    @JvmField
    var startTick: Float = -1.0f
    @JvmField
    var resetTickLength: Float = 3.0f

    open fun addAnimationController(value: IAnimationController<*>) {
        animationControllers.add(value)
    }

    open fun getResetSpeed(): Float = resetTickLength

    open fun setResetSpeedInTicks(resetTickLength: Float) {
        this.resetTickLength = if (resetTickLength < 0) 0.0f else resetTickLength
    }

    open fun getAnimationControllers(): MutableList<IAnimationController<*>> = animationControllers

    open fun getAnimationControllerByName(name: String): IAnimationController<*>? {
        if (animationControllerMap.isEmpty() && animationControllers.isNotEmpty()) {
            for (controller in animationControllers) {
                animationControllerMap.put(controller.getName(), controller)
            }
        }
        return animationControllerMap.get(name)
    }

    open fun clear() {
        limbSwing = 0.0f
        startTick = -1.0f
        resetTickLength = 3.0f
        for (controller in animationControllers) {
            controller.reset()
        }
        animationControllers.clear()
        animationControllerMap.clear()
    }
}