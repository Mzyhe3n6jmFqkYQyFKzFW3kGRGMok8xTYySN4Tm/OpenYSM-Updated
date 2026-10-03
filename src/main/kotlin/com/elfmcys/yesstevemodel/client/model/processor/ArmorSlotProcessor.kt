package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.world.entity.EquipmentSlot
import org.apache.commons.lang3.function.TriFunction
import org.apache.commons.lang3.tuple.Pair
import java.util.ArrayList

open class ArmorSlotProcessor<T> : ModelProcessor<T, PlayerModelBundle> {
    var prefix: String = null
    var category: String = null
    var animationDataProvider: AnimationDataProvider<PlayerModelBundle> = null
    var controllerFactory: TriFunction<String, T, EquipmentSlot, IAnimationController<T>> = null
    constructor(prefix: String, category: String, animationDataProvider: AnimationDataProvider<PlayerModelBundle>, controllerFactory: TriFunction<String, T, EquipmentSlot, IAnimationController<T>>) {
        this.prefix = prefix
        this.category = category
        this.animationDataProvider = animationDataProvider
        this.controllerFactory = controllerFactory
    }
    open fun process(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): ControllerFactory<T> {
        var matchingSlots: ArrayList<Pair<String, EquipmentSlot>> = ArrayList()
        var armorCondition: ConditionArmor = this.animationDataProvider.getConditionArmor(modelBundle, resourceBundle)
        var animationEntries: Object2ReferenceMap<String, AnimationController> = this.animationDataProvider.getAnimationEntries(modelBundle, resourceBundle)
        var animations: Object2ReferenceMap<String, Animation> = this.animationDataProvider.getAnimations(modelBundle, resourceBundle)
        for (slot in EquipmentSlot.values()) {
            var slotKey: String = String.format("%s.%s_%s", this.prefix, this.category, slot.getName())
            if (animationEntries.containsKey(slotKey)) {
                matchingSlots.add(Pair.of(slotKey, slot))
            } else {
                if (resourceBundle.getEvents().containsKey(String.format("%s_ctrl_%s_%s", this.prefix, this.category, slot.getName()))) {
                    matchingSlots.add(Pair.of(slotKey, slot))
                } else {
                    if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && armorCondition.hasFilter(slot) || animations.containsKey(slot.getName() + ":default")) {
                        matchingSlots.add(Pair.of(slotKey, slot))
                    }
                }
            }
        }
        return { entity, consumer -> if (!entity is IPreviewAnimatable) {  } }
    }
}