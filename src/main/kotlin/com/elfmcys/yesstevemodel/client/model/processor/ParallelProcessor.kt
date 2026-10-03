package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceRBTreeMap
import org.apache.commons.lang3.function.TriFunction
import java.util.function.Predicate
import java.util.regex.Pattern

open class ParallelProcessor<T, TModel> : ModelProcessor<T, TModel> {
    var prefix: String = null
    var slotName: String = null
    var animationEntryMatcher: Predicate<String> = null
    var controllerEntryMatcher: Predicate<String> = null
    var animationNameMatcher: Predicate<String> = null
    var animationDataProvider: AnimationDataProvider<TModel> = null
    var controllerFactory: TriFunction<String, T, String, IAnimationController<T>> = null
    constructor(prefix: String, slotName: String, allowExtraSlots: Boolean, animationDataProvider: AnimationDataProvider<TModel>, controllerFactory: TriFunction<String, T, String, IAnimationController<T>>) {
        this.prefix = prefix
        this.slotName = slotName
        if (allowExtraSlots) {
            this.animationEntryMatcher = Pattern.compile(String.format("^%s\\.%s_.+", prefix, slotName)).asMatchPredicate()
            this.controllerEntryMatcher = Pattern.compile(String.format("^%s_ctrl_%s_.+", prefix, slotName)).asMatchPredicate()
        } else {
            this.animationEntryMatcher = Pattern.compile(String.format("^%s\\.%s_[0-7]$", prefix, slotName)).asMatchPredicate()
            this.controllerEntryMatcher = Pattern.compile(String.format("^%s_ctrl_%s_[0-7]$", prefix, slotName)).asMatchPredicate()
        }
        this.animationNameMatcher = Pattern.compile(String.format("^%s[0-7]$", slotName)).asMatchPredicate()
        this.animationDataProvider = animationDataProvider
        this.controllerFactory = controllerFactory
    }
    open fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        var matchedSlots: Object2ReferenceRBTreeMap = Object2ReferenceRBTreeMap()
        Object2ReferenceMaps.fastForEach(this.animationDataProvider.getAnimationEntries(modelData, resourceBundle), { entry -> if (this.animationEntryMatcher.test(entry.getKey())) { matchedSlots.put(entry.getKey(), null) } })
        var animations: Object2ReferenceMap<String, Animation> = this.animationDataProvider.getAnimations(modelData, resourceBundle)
        Object2ReferenceMaps.fastForEach(resourceBundle.getEvents(), { event -> if (this.controllerEntryMatcher.test(event.getKey())) { var controllerName: String = event.getKey().replace("_ctrl_", ".")

matchedSlots.put(controllerName, null) } })
        Object2ReferenceMaps.fastForEach(animations, { animEntry -> if (!animEntry.getValue().isEmpty() && this.animationNameMatcher.test(animEntry.getKey())) { matchedSlots.put(String.format("%s.%s_%s", this.prefix, this.slotName, animEntry.getKey().substring(this.slotName.length())), animEntry.getKey()) } })
        return { entity, consumer -> Object2ReferenceMaps.fastForEach(matchedSlots, { slot -> consumer.accept(this.controllerFactory.apply((slot.getKey() as String), entity, (slot.getValue() as String))) }) }
    }
}