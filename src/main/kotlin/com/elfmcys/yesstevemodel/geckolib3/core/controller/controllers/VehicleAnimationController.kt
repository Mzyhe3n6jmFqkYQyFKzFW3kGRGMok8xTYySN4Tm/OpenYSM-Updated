package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers

import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.client.animation.predicate.EntityMovementPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.MovementAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.RideStateAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.client.entity.VehicleRotationController
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.VehicleModelBundle
import com.elfmcys.yesstevemodel.client.model.processor.ModelProcessor
import com.elfmcys.yesstevemodel.client.model.processor.NamedModelProcessor
import com.elfmcys.yesstevemodel.client.model.processor.ParallelProcessor
import com.elfmcys.yesstevemodel.client.model.processor.ProcessorPipeline
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import org.apache.commons.lang3.function.TriFunction
import java.util.function.BiFunction

object VehicleAnimationController {
    object VehicleAnimationDataProvider : AnimationDataProvider<VehicleModelBundle> {
        override fun getAnimationEntries(
            t: VehicleModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, AnimationController> = t.animationControllers

        override fun getAnimations(
            t: VehicleModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, Animation> = t.animations

        override fun getConditionArmor(
            t: VehicleModelBundle,
            resourceBundle: ModelResourceBundle
        ): ConditionArmor? = null
    }

    private const val VEHICLE_PREFIX: String = "vehicle"
    const val ORIGIN_CONTROLLER_KEY: String = "vehicle.origin"

    @JvmField
    val REGISTRY: ProcessorPipeline<GeckoVehicleEntity, VehicleModelBundle> = ProcessorPipeline()

    @JvmStatic
    fun registerControllers() {
        registerParallelController("pre_parallel") { animationEntryKey, entity, linkedAnimationName ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                if (linkedAnimationName != null) NamedAnimationPredicate(linkedAnimationName) else StopAnimationPredicate.getInstance()
            )
        }
        registerNamedController("pre_main", null, true) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.0f, StopAnimationPredicate())
        }
        registerNamedController("main", EntityMovementPredicate.MOVEMENT_STATES, true) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.1f, EntityMovementPredicate())
        }
        registerNamedController("move", MovementAnimationPredicate.ANIMATION_NAMES, true) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.1f, MovementAnimationPredicate())
        }
        registerOriginController("origin") { animationEntryKey, entity ->
            VehicleRotationController(entity, animationEntryKey)
        }
        registerNamedController(
            "ride",
            RideStateAnimationPredicate.ANIMATION_NAMES,
            true
        ) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.1f, RideStateAnimationPredicate())
        }
        registerNamedController("post_main", null, true) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.0f, StopAnimationPredicate())
        }
        registerParallelController("parallel") { animationEntryKey, entity, linkedAnimationName ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                if (linkedAnimationName != null) NamedAnimationPredicate(linkedAnimationName) else StopAnimationPredicate.getInstance(),
                true
            )
        }
    }

    @JvmStatic
    fun buildControllers(
        modelBundle: VehicleModelBundle,
        resourceBundle: ModelResourceBundle
    ): (GeckoVehicleEntity) -> Unit {
        if (REGISTRY.isEmpty()) registerControllers()
        return REGISTRY.buildAll(modelBundle, resourceBundle)
    }

    @JvmStatic
    fun registerOriginController(
        controllerName: String,
        controllerFactory: BiFunction<String, GeckoVehicleEntity, IAnimationController<GeckoVehicleEntity>>
    ): ModelProcessor<GeckoVehicleEntity, VehicleModelBundle> {
        val controllerKey = "$VEHICLE_PREFIX.$controllerName"
        return REGISTRY.register { _, _ ->
            { entity, consumer -> consumer(controllerFactory.apply(controllerKey, entity)) }
        }
    }

    @JvmStatic
    fun registerNamedController(
        slotName: String,
        requiredAnimations: Array<String>?,
        checkAnimationEntries: Boolean,
        controllerFactory: BiFunction<String, GeckoVehicleEntity, IAnimationController<GeckoVehicleEntity>>
    ): ModelProcessor<GeckoVehicleEntity, VehicleModelBundle> {
        return REGISTRY.register(
            NamedModelProcessor(
                VEHICLE_PREFIX,
                slotName,
                requiredAnimations,
                checkAnimationEntries,
                VehicleAnimationDataProvider,
                controllerFactory
            )
        )
    }

    @JvmStatic
    fun registerParallelController(
        slotName: String,
        controllerFactory: TriFunction<String, GeckoVehicleEntity, String, IAnimationController<GeckoVehicleEntity>>
    ): ModelProcessor<GeckoVehicleEntity, VehicleModelBundle> {
        return REGISTRY.register(
            ParallelProcessor(
                VEHICLE_PREFIX,
                slotName,
                false,
                VehicleAnimationDataProvider,
                controllerFactory
            )
        )
    }
}