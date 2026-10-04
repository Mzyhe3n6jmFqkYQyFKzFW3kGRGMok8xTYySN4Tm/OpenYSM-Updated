package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import java.util.regex.Pattern

open class ControllerSlotBinder<T : GeoEntity<*>, TModel>(
    prefix: String,
    slotName: String,
    private val animationDataProvider: AnimationDataProvider<TModel>,
    private val controllerFactory: (String, T) -> IAnimationController<T>
) : ModelProcessor<T, TModel> {
    private val controllerNameMatcher: (String) -> Boolean =
        Pattern.compile("^${Pattern.quote(prefix)}\\.${Pattern.quote(slotName)}(_.+)?$").asMatchPredicate()::test

    private val molangEventMatcher: (String) -> Boolean =
        Pattern.compile("^${Pattern.quote(prefix)}_ctrl_${Pattern.quote(slotName)}(_.+)?$").asMatchPredicate()::test

    override fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        val controllerNames = ObjectRBTreeSet<String>()
        Object2ReferenceMaps.fastForEach(
            animationDataProvider.getAnimationEntries(
                modelData,
                resourceBundle
            )
        ) { entry ->
            if (controllerNameMatcher(entry.key)) {
                controllerNames.add(entry.key)
            }
        }
        Object2ReferenceMaps.fastForEach(resourceBundle.events) { entry ->
            if (molangEventMatcher(entry.key)) {
                controllerNames.add(entry.key.replace("_ctrl_", "."))
            }
        }
        return ControllerFactory { entity, consumer ->
            for (controllerName in controllerNames) consumer(controllerFactory(controllerName, entity))
        }
    }
}
