@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers

import com.elfmcys.yesstevemodel.client.animation.AnimationManager
import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.client.animation.predicate.*
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.client.model.processor.*
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.world.entity.EquipmentSlot
import rip.ysm.compat.carryon.CarryOnCompat
import rip.ysm.compat.gun.common.ItemUseAnimationPredicate
import rip.ysm.compat.parcool.ParcoolCompat

object PlayerAnimationController {
    object PlayerAnimationDataProvider : AnimationDataProvider<PlayerModelBundle> {
        override fun getAnimationEntries(
            t: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, AnimationController> {
            return t.animationEntries
        }

        override fun getAnimations(
            t: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, Animation> {
            return t.mainAnimations
        }

        override fun getConditionArmor(
            t: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): ConditionArmor = t.conditionManager.armor
    }

    val REGISTRY: ProcessorPipeline<CustomPlayerEntity, PlayerModelBundle> = ProcessorPipeline()
    private const val PLAYER_PREFIX: String = "player"

    const val CAP_CONTROLLER_KEY: String = "$PLAYER_PREFIX.cap"

    fun registerControllers() {
        registerParallelController("pre_parallel") { animationEntryKey, entity, linkedAnimationName ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                if (linkedAnimationName != null) NamedAnimationPredicate(linkedAnimationName) else StopAnimationPredicate
            )
        }
        ParcoolCompat.controllerFactory?.let { registerController("parcool", it) }
        registerController("vehicle") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.1f,
                LivingMovementAnimationPredicate()
            )
        }
        registerSlotController("pre_main") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        registerController("main") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.1f,
                AnimationManager()
            )
        }
        registerSlotController("post_main") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        registerSlotController("pre_hold") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        registerController("hold_offhand") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.1f,
                OffHandHoldPredicate()
            )
        }
        registerController("hold_mainhand") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.1f,
                MainHandHoldPredicate()
            )
        }
        registerSlotController("post_hold") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        if (ItemUseAnimationPredicate.isModLoaded) {
            registerController("fire") { animationEntryKey, entity ->
                CompositeAnimationController(
                    entity,
                    animationEntryKey,
                    0.0f,
                    ItemUseAnimationPredicate()
                )
            }
        }
        registerSlotController("pre_swing") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        registerController("swing") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                ItemHoldAnimationPredicate()
            )
        }
        registerSlotController("post_swing") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        registerSlotController("pre_use") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        registerController("use") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.1f,
                InteractionHandAnimationPredicate()
            )
        }
        registerSlotController("post_use") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                StopAnimationPredicate
            )
        }
        registerController("passenger") { animationEntryKey, entity ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.1f,
                OffhandAttackAnimationPredicate()
            )
        }
        CarryOnCompat.controllerFactory?.let { registerController("carry_on", it) }
        registerController("cap") { animationEntryKey, entity ->
            PredicateBasedController(
                entity,
                animationEntryKey,
                0.0f,
                PlayerBaseAnimationPredicate()
            )
        }
        registerController("gui_hover", true) { animationEntryKey, entity ->
            PredicateBasedController(
                entity,
                animationEntryKey,
                0.0f,
                PlayerCustomAnimationPredicate()
            )
        }
        registerController("gui_focus", true) { animationEntryKey, entity ->
            PredicateBasedController(
                entity,
                animationEntryKey,
                0.0f,
                PlayerIdleAnimationPredicate()
            )
        }
        registerParallelController("parallel") { animationEntryKey, entity, linkedAnimationName ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                if (linkedAnimationName != null) NamedAnimationPredicate(linkedAnimationName) else StopAnimationPredicate,
                true
            )
        }
        registerArmorController("armor") { animationEntryKey, entity, equipmentSlot ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                ArmorPredicate(equipmentSlot)
            )
        }
    }

    fun buildControllers(
        modelBundle: PlayerModelBundle,
        resourceBundle: ModelResourceBundle
    ): (CustomPlayerEntity) -> Unit {
        if (REGISTRY.isEmpty()) registerControllers()
        return REGISTRY.buildAll(modelBundle, resourceBundle)
    }

    fun registerController(
        controllerName: String,
        controllerFactory: (String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>
    ) {
        registerController(controllerName, false, controllerFactory)
    }

    fun registerController(
        controllerName: String,
        guiOnly: Boolean,
        controllerFactory: (String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>
    ) {
        val controllerKey = "$PLAYER_PREFIX.$controllerName"
        var processor: ModelProcessor<CustomPlayerEntity, PlayerModelBundle> =
            { _, _ -> { entity, consumer -> consumer(controllerFactory(controllerKey, entity)) } }
        if (guiOnly) {
            processor = processor.withFilter { entity -> entity is IPreviewAnimatable }
        }
        REGISTRY.register(processor)
    }

    fun registerSlotController(
        slotName: String,
        controllerFactory: (String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>
    ) {
        REGISTRY.register(
            ControllerSlotBinder(
                PLAYER_PREFIX,
                slotName,
                PlayerAnimationDataProvider,
                controllerFactory
            )
        )
    }

    fun registerNamedController(
        slotName: String,
        requiredAnimations: Array<String>?,
        checkAnimationEntries: Boolean,
        controllerFactory: (String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>
    ) {
        REGISTRY.register(
            NamedModelProcessor(
                PLAYER_PREFIX,
                slotName,
                requiredAnimations,
                checkAnimationEntries,
                PlayerAnimationDataProvider,
                controllerFactory
            )
        )
    }

    fun registerParallelController(
        slotName: String,
        controllerFactory: (String, CustomPlayerEntity, String?) -> IAnimationController<CustomPlayerEntity>
    ) {
        REGISTRY.register(
            ParallelProcessor(
                PLAYER_PREFIX,
                slotName,
                true,
                PlayerAnimationDataProvider,
                controllerFactory
            )
        )
    }

    fun registerArmorController(
        category: String,
        controllerFactory: (String, CustomPlayerEntity, EquipmentSlot) -> IAnimationController<CustomPlayerEntity>
    ) {
        REGISTRY.register(
            ArmorSlotProcessor(
                PLAYER_PREFIX,
                category,
                PlayerAnimationDataProvider,
                controllerFactory
            )
        )
    }
}