@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.geckolib3.core.event.predicate

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData

data class AnimationEvent<T : AnimatableEntity<*>>(
    val animatable: T,
    val limbSwing: Float,
    val limbSwingAmount: Float,
    val tickCount: Int,
    val partialTick: Float,
    val frameTime: Float,
    val isMoving: Boolean,
    val isFirstPerson: Boolean,
    val modelData: EntityModelData
) {
    var currentTick: Float = tickCount + frameTime
    var controller: PredicateBasedController<T>? = null
}