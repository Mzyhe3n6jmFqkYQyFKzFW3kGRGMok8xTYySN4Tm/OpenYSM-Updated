package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.EquipmentSlot
import org.apache.commons.lang3.function.TriFunction
import org.apache.commons.lang3.tuple.Pair

open class ArmorSlotProcessor<T : GeoEntity<*>>(
    private val prefix: String,
    private val category: String,
    private val animationDataProvider: AnimationDataProvider<PlayerModelBundle>,
    private val controllerFactory: TriFunction<String, T, EquipmentSlot, IAnimationController<T>>
) : ModelProcessor<T, PlayerModelBundle> {

    override fun process(modelData: PlayerModelBundle, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        val matchingSlots = ArrayList<Pair<String, EquipmentSlot>>()
        val armorCondition = animationDataProvider.getConditionArmor(modelData, resourceBundle)
        val animationEntries = animationDataProvider.getAnimationEntries(modelData, resourceBundle)
        val animations = animationDataProvider.getAnimations(modelData, resourceBundle)
        for (slot in EquipmentSlot.values()) {
            val slotKey = "$prefix.${category}_${slot.getName()}"
            if (animationEntries.containsKey(slotKey)) {
                matchingSlots.add(Pair.of(slotKey, slot))
            } else if (resourceBundle.events.containsKey("${prefix}_ctrl_${category}_${slot.getName()}")) {
                matchingSlots.add(Pair.of(slotKey, slot))
            } else if (slot.type == EquipmentSlot.Type.HUMANOID_ARMOR && (armorCondition?.hasFilter(slot) == true || animations.containsKey(
                    "${slot.getName()}:default"
                ))
            ) {
                matchingSlots.add(Pair.of(slotKey, slot))
            }
        }
        return ControllerFactory { entity, consumer ->
            if (entity !is IPreviewAnimatable) {
                for (pair in matchingSlots) {
                    consumer.accept(controllerFactory.apply(pair.left, entity, pair.right))
                }
            }
        }
    }
}
