package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import java.util.function.BiFunction
import java.util.function.Predicate
import java.util.regex.Pattern

open class ControllerSlotBinder<T, TModel> : ModelProcessor<T, TModel> {
    var controllerNameMatcher: Predicate<String> = null
    var molangEventMatcher: Predicate<String> = null
    var animationDataProvider: AnimationDataProvider<TModel> = null
    var controllerFactory: BiFunction<String, T, IAnimationController<T>> = null
    constructor(prefix: String, slotName: String, animationDataProvider: AnimationDataProvider<TModel>, controllerFactory: BiFunction<String, T, IAnimationController<T>>) {
        this.controllerNameMatcher = Pattern.compile(String.format("^%s\\.%s(_.+){0,1}$", prefix, slotName)).asMatchPredicate()
        this.molangEventMatcher = Pattern.compile(String.format("^%s_ctrl_%s(_.+){0,1}$", prefix, slotName)).asMatchPredicate()
        this.animationDataProvider = animationDataProvider
        this.controllerFactory = controllerFactory
    }
    open fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        var controllerNames: ObjectRBTreeSet<String> = ObjectRBTreeSet()
        Object2ReferenceMaps.fastForEach(this.animationDataProvider.getAnimationEntries(modelData, resourceBundle), { entry -> if (this.controllerNameMatcher.test(entry.getKey())) { controllerNames.add(entry.getKey()) } })
        Object2ReferenceMaps.fastForEach(resourceBundle.getEvents(), { entry -> if (this.molangEventMatcher.test(entry.getKey())) { controllerNames.add(entry.getKey().replace("_ctrl_", ".")) } })
        return { entity, consumer ->  }
    }
}