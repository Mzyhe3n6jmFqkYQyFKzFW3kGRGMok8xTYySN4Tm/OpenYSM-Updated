package com.elfmcys.yesstevemodel.geckolib3.core.event.predicate

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData

open class AnimationEvent<T : AnimatableEntity<*>>(
    private val animatable: T,
    val limbSwing: Float,
    val limbSwingAmount: Float,
    val tickCount: Int,
    val partialTick: Float,
    val frameTime: Float,
    val isMoving: Boolean,
    val isFirstPerson: Boolean,
    val modelData: EntityModelData
) {
    @JvmField var currentTick: Float = tickCount + frameTime
    @JvmField var controller: PredicateBasedController<T>? = null

    open fun getCurrentTick(): Float = currentTick
    open fun getAnimatable(): T = animatable
    open fun getLimbSwing(): Float = limbSwing
    open fun getLimbSwingAmount(): Float = limbSwingAmount
    open fun getTickCount(): Int = tickCount
    open fun getPartialTick(): Float = partialTick
    open fun getFrameTime(): Float = frameTime
    open fun isMoving(): Boolean = isMoving
    open fun isFirstPerson(): Boolean = isFirstPerson
    open fun getController(): PredicateBasedController<T>? = controller
    open fun setController(controller: PredicateBasedController<T>?) {
        this.controller = controller
    }
    open fun getModelData(): EntityModelData = modelData
}