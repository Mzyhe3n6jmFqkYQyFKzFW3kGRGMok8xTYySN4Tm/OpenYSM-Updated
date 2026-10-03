package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import java.util.function.BiFunction

open class NamedModelProcessor<T : GeoEntity<*>, TModel>(
    prefix: String,
    slotName: String,
    private val requiredAnimations: Array<String>?,
    private val checkAnimationEntries: Boolean,
    private val animationDataProvider: AnimationDataProvider<TModel>,
    private val controllerFactory: BiFunction<String, T, IAnimationController<T>>
) : ModelProcessor<T, TModel> {

    private val animationEntryKey: String = "$prefix.$slotName"
    private val controllerKey: String = "${prefix}_ctrl_$slotName"

    override fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        var hasContent = false
        if ((checkAnimationEntries && animationDataProvider.getAnimationEntries(modelData, resourceBundle)
                .containsKey(animationEntryKey)) ||
            resourceBundle.events.containsKey(controllerKey)
        ) {
            hasContent = true
        } else if (requiredAnimations != null) {
            val animations = animationDataProvider.getAnimations(modelData, resourceBundle)
            for (requiredAnimation in requiredAnimations) {
                val animation = animations.get(requiredAnimation)
                if (animation != null && !animation.isEmpty()) {
                    hasContent = true
                    break
                }
            }
        }

        if (hasContent) {
            return ControllerFactory { entity, consumer ->
                consumer.accept(controllerFactory.apply(animationEntryKey, entity))
            }
        }
        return ControllerFactory { _, _ -> }
    }
}
