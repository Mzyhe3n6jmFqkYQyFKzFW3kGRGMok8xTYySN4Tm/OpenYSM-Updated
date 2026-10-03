package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import java.util.function.BiFunction

open class NamedModelProcessor<T, TModel> : ModelProcessor<T, TModel> {
    var animationEntryKey: String = null
    var controllerKey: String = null
    var requiredAnimations: Array<String> = null
    var checkAnimationEntries: Boolean = false
    var animationDataProvider: AnimationDataProvider<TModel> = null
    var controllerFactory: BiFunction<String, T, IAnimationController<T>> = null
    constructor(prefix: String, slotName: String, requiredAnimations: Array<String>, checkAnimationEntries: Boolean, animationDataProvider: AnimationDataProvider<TModel>, controllerFactory: BiFunction<String, T, IAnimationController<T>>) {
        this.animationEntryKey = String.format("%s.%s", prefix, slotName)
        this.controllerKey = String.format("%s_ctrl_%s", prefix, slotName)
        this.requiredAnimations = requiredAnimations
        this.checkAnimationEntries = checkAnimationEntries
        this.animationDataProvider = animationDataProvider
        this.controllerFactory = controllerFactory
    }
    open fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        var hasContent: Boolean = false
        if (this.checkAnimationEntries && this.animationDataProvider.getAnimationEntries(modelData, resourceBundle).containsKey(this.animationEntryKey) || resourceBundle.getEvents().containsKey(this.controllerKey)) {
            hasContent = true
        } else {
            if (this.requiredAnimations != null) {
                var animations: Object2ReferenceMap<String, Animation> = this.animationDataProvider.getAnimations(modelData, resourceBundle)
                for (requiredAnimation in this.requiredAnimations) {
                    var animation: Animation = animations.get(requiredAnimation)
                    if (animation != null && !animation.isEmpty()) {
                        hasContent = true
                        break
                    }
                }
            }
        }
        if (hasContent) {
            return { entity, consumer -> consumer.accept(this.controllerFactory.apply(this.animationEntryKey, entity)) }
        }
        return { entity, consumer ->  }
    }
}