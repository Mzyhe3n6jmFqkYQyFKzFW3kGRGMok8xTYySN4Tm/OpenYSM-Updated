package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap

interface AnimationDataProvider<T> {
    fun getAnimationEntries(t: T, resourceBundle: ModelResourceBundle): Object2ReferenceMap<String, AnimationController>
    fun getAnimations(t: T, resourceBundle: ModelResourceBundle): Object2ReferenceMap<String, Animation>
    fun getConditionArmor(t: T, resourceBundle: ModelResourceBundle): ConditionArmor?
}