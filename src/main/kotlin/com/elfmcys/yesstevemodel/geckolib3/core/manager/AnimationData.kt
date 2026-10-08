package com.elfmcys.yesstevemodel.geckolib3.core.manager

import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList

class AnimationData {
    var limbSwing: Float = 0.0f
    val animationControllers: List<IAnimationController<*>>
        field: MutableList<IAnimationController<*>> = ReferenceArrayList(0)
    private val animationControllerMap: Object2ReferenceOpenHashMap<String, IAnimationController<*>> =
        Object2ReferenceOpenHashMap(0)
    var startTick: Float = -1.0f
    private var resetTickLength: Float = 3.0f

    fun addAnimationController(value: IAnimationController<*>) {
        animationControllers.add(value)
    }

    var resetSpeed: Float
        get() = resetTickLength
        set(value) {
            resetTickLength = value.coerceAtLeast(0.0f)
        }

    fun getAnimationControllerByName(name: String): IAnimationController<*>? {
        if (animationControllerMap.isEmpty() && animationControllers.isNotEmpty()) {
            for (controller in animationControllers) {
                animationControllerMap[controller.name] = controller
            }
        }
        return animationControllerMap[name]
    }

    fun clear() {
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