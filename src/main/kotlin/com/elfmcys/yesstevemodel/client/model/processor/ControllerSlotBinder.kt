package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import java.util.function.BiFunction
import java.util.function.Predicate
import java.util.regex.Pattern

open class ControllerSlotBinder<T : GeoEntity<*>, TModel>(
    prefix: String,
    slotName: String,
    private val animationDataProvider: AnimationDataProvider<TModel>,
    private val controllerFactory: BiFunction<String, T, IAnimationController<T>>
) : ModelProcessor<T, TModel> {
    private val controllerNameMatcher: Predicate<String> =
        Pattern.compile("^${Pattern.quote(prefix)}\\.${Pattern.quote(slotName)}(_.+)?$").asMatchPredicate()

    private val molangEventMatcher: Predicate<String> =
        Pattern.compile("^${Pattern.quote(prefix)}_ctrl_${Pattern.quote(slotName)}(_.+)?$").asMatchPredicate()

    override fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        val controllerNames = ObjectRBTreeSet<String>()
        Object2ReferenceMaps.fastForEach(
            animationDataProvider.getAnimationEntries(
                modelData,
                resourceBundle
            )
        ) { entry ->
            if (controllerNameMatcher.test(entry.key)) {
                controllerNames.add(entry.key)
            }
        }
        Object2ReferenceMaps.fastForEach(resourceBundle.events) { entry ->
            if (molangEventMatcher.test(entry.key)) {
                controllerNames.add(entry.key.replace("_ctrl_", "."))
            }
        }
        return ControllerFactory { entity, consumer ->
            for (controllerName in controllerNames) consumer(controllerFactory.apply(controllerName, entity))
        }
    }
}
