package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceRBTreeMap
import java.util.regex.Pattern

open class ParallelProcessor<T : GeoEntity<*>, TModel>(
    private val prefix: String,
    private val slotName: String,
    allowExtraSlots: Boolean,
    private val animationDataProvider: AnimationDataProvider<TModel>,
    private val controllerFactory: (String, T, String?) -> IAnimationController<T>
) : ModelProcessor<T, TModel> {

    private val animationEntryMatcher: (String) -> Boolean
    private val controllerEntryMatcher: (String) -> Boolean
    private val animationNameMatcher: (String) -> Boolean =
        Pattern.compile("^${Pattern.quote(slotName)}[0-7]$").asMatchPredicate()::test

    init {
        if (allowExtraSlots) {
            animationEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}\\.${Pattern.quote(slotName)}_.+").asMatchPredicate()::test
            controllerEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}_ctrl_${Pattern.quote(slotName)}_.+").asMatchPredicate()::test
        } else {
            animationEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}\\.${Pattern.quote(slotName)}_[0-7]$").asMatchPredicate()::test
            controllerEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}_ctrl_${Pattern.quote(slotName)}_[0-7]$").asMatchPredicate()::test
        }
    }

    override fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        val matchedSlots = Object2ReferenceRBTreeMap<String, String?>()
        Object2ReferenceMaps.fastForEach(
            animationDataProvider.getAnimationEntries(
                modelData,
                resourceBundle
            )
        ) { entry ->
            if (animationEntryMatcher(entry.key)) {
                matchedSlots[entry.key] = null
            }
        }
        val animations = animationDataProvider.getAnimations(modelData, resourceBundle)
        Object2ReferenceMaps.fastForEach(resourceBundle.events) { event ->
            if (controllerEntryMatcher(event.key)) {
                val controllerName = event.key.replace("_ctrl_", ".")
                runCatching {
                    val suffix = controllerName.substring(prefix.length + slotName.length + 2)
                    val slotIndex = suffix.toInt()
                    if (slotIndex in 0..7) {
                        val animationName = slotName + suffix
                        if (animations.containsKey(animationName)) {
                            matchedSlots.put(controllerName, animationName)
                            return@fastForEach
                        }
                    }
                }
                matchedSlots[controllerName] = null
            }
        }
        Object2ReferenceMaps.fastForEach(animations) { animEntry ->
            if (!animEntry.value.isEmpty() && animationNameMatcher(animEntry.key)) {
                matchedSlots["${prefix}.${slotName}_${animEntry.key.substring(slotName.length)}"] = animEntry.key
            }
        }
        return ControllerFactory { entity, consumer ->
            Object2ReferenceMaps.fastForEach(matchedSlots) { slot ->
                consumer(controllerFactory(slot.key, entity, slot.value))
            }
        }
    }
}
