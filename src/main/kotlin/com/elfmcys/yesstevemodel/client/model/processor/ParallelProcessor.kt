package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceRBTreeMap
import org.apache.commons.lang3.function.TriFunction
import java.util.function.Predicate
import java.util.regex.Pattern

open class ParallelProcessor<T : GeoEntity<*>, TModel>(
    private val prefix: String,
    private val slotName: String,
    allowExtraSlots: Boolean,
    private val animationDataProvider: AnimationDataProvider<TModel>,
    private val controllerFactory: TriFunction<String, T, String, IAnimationController<T>>
) : ModelProcessor<T, TModel> {

    private val animationEntryMatcher: Predicate<String>
    private val controllerEntryMatcher: Predicate<String>
    private val animationNameMatcher: Predicate<String> =
        Pattern.compile("^${Pattern.quote(slotName)}[0-7]$").asMatchPredicate()

    init {
        if (allowExtraSlots) {
            animationEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}\\.${Pattern.quote(slotName)}_.+").asMatchPredicate()
            controllerEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}_ctrl_${Pattern.quote(slotName)}_.+").asMatchPredicate()
        } else {
            animationEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}\\.${Pattern.quote(slotName)}_[0-7]$").asMatchPredicate()
            controllerEntryMatcher =
                Pattern.compile("^${Pattern.quote(prefix)}_ctrl_${Pattern.quote(slotName)}_[0-7]$").asMatchPredicate()
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
            if (animationEntryMatcher.test(entry.key)) {
                matchedSlots.put(entry.key, null)
            }
        }
        val animations = animationDataProvider.getAnimations(modelData, resourceBundle)
        Object2ReferenceMaps.fastForEach(resourceBundle.events) { event ->
            if (controllerEntryMatcher.test(event.key)) {
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
                matchedSlots.put(controllerName, null)
            }
        }
        Object2ReferenceMaps.fastForEach(animations) { animEntry ->
            if (!animEntry.value.isEmpty() && animationNameMatcher.test(animEntry.key)) {
                matchedSlots.put(
                    "${prefix}.${slotName}_${animEntry.key.substring(slotName.length)}",
                    animEntry.key
                )
            }
        }
        return ControllerFactory { entity, consumer ->
            Object2ReferenceMaps.fastForEach(matchedSlots) { slot ->
                consumer.accept(controllerFactory.apply(slot.key, entity, slot.value))
            }
        }
    }
}
