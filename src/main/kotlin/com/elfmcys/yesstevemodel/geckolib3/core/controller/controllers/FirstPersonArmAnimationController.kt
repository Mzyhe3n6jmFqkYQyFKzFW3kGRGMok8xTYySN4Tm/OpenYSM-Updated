package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers

import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.client.animation.predicate.EquipmentSlotAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.client.model.processor.*
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.world.entity.EquipmentSlot
import org.apache.commons.lang3.function.TriFunction
import java.util.function.BiFunction

@Suppress("unused")
object FirstPersonArmAnimationController {
    object DefaultBoneExpressionProvider : AnimationDataProvider<PlayerModelBundle> {
        override fun getAnimationEntries(
            modelBundle: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, AnimationController> = modelBundle.animationEntries

        override fun getAnimations(
            modelBundle: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, Animation> = modelBundle.armAnimations

        override fun getConditionArmor(
            modelBundle: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): ConditionArmor = modelBundle.modelProcessor.conditionArmor
    }

    private const val FP_ARM_PREFIX: String = "fp.arm"

    @JvmField
    val processorRegistry: ProcessorPipeline<PlayerGeoEntity, PlayerModelBundle> = ProcessorPipeline()

    @JvmStatic
    fun registerDefaultProcessors() {
        registerNamedProcessor("misc", null, true) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.0f, StopAnimationPredicate())
        }
        registerParallelProcessor("parallel") { animationEntryKey, entity, linkedAnimationName ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                if (linkedAnimationName != null) NamedAnimationPredicate(linkedAnimationName) else StopAnimationPredicate.getInstance(),
                true
            )
        }
        registerArmorProcessor("armor") { animationEntryKey, entity, equipmentSlot ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                EquipmentSlotAnimationPredicate(equipmentSlot)
            )
        }
    }

    @JvmStatic
    fun buildControllers(
        modelBundle: PlayerModelBundle,
        resourceBundle: ModelResourceBundle
    ): (PlayerGeoEntity) -> Unit {
        if (processorRegistry.isEmpty()) registerDefaultProcessors()
        return processorRegistry.buildAll(modelBundle, resourceBundle)
    }

    @JvmStatic
    fun registerSimpleProcessor(
        slotName: String,
        controllerFactory: BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>>
    ) {
        registerProcessorWithFilter(slotName, false, controllerFactory)
    }

    @JvmStatic
    fun registerProcessorWithFilter(
        slotName: String,
        skipOnPreview: Boolean,
        controllerFactory: BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>>
    ) {
        val animationEntryKey = "$FP_ARM_PREFIX.$slotName"
        var processor: ModelProcessor<PlayerGeoEntity, PlayerModelBundle> = { _, _ ->
            { entity, consumer -> consumer(controllerFactory.apply(animationEntryKey, entity)) }
        }
        if (skipOnPreview) {
            processor = processor.withFilter { entity -> entity is IPreviewAnimatable }
        }
        processorRegistry.register(processor)
    }

    @JvmStatic
    fun registerMolangProcessor(
        slotName: String,
        controllerFactory: BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>>
    ) {
        processorRegistry.register(
            ControllerSlotBinder(
                FP_ARM_PREFIX,
                slotName,
                DefaultBoneExpressionProvider,
                controllerFactory
            )
        )
    }

    @JvmStatic
    fun registerNamedProcessor(
        slotName: String,
        requiredAnimations: Array<String>?,
        checkAnimationEntries: Boolean,
        controllerFactory: BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>>
    ) {
        processorRegistry.register(
            NamedModelProcessor(
                FP_ARM_PREFIX,
                slotName,
                requiredAnimations,
                checkAnimationEntries,
                DefaultBoneExpressionProvider,
                controllerFactory
            )
        )
    }

    @JvmStatic
    fun registerParallelProcessor(
        slotName: String,
        controllerFactory: TriFunction<String, PlayerGeoEntity, String, IAnimationController<PlayerGeoEntity>>
    ) {
        processorRegistry.register(
            ParallelProcessor(
                FP_ARM_PREFIX,
                slotName,
                true,
                DefaultBoneExpressionProvider,
                controllerFactory
            )
        )
    }

    @JvmStatic
    fun registerArmorProcessor(
        category: String,
        controllerFactory: TriFunction<String, PlayerGeoEntity, EquipmentSlot, IAnimationController<PlayerGeoEntity>>
    ) {
        processorRegistry.register(
            ArmorSlotProcessor(
                FP_ARM_PREFIX,
                category,
                DefaultBoneExpressionProvider,
                controllerFactory
            )
        )
    }
}