package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers

import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.ProjectileAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.ProjectileModelBundle
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
import java.util.function.Consumer

object ProjectileAnimationController {
    object ProjectileAnimationDataProvider : AnimationDataProvider<ProjectileModelBundle> {
        override fun getAnimationEntries(
            modelBundle: ProjectileModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, AnimationController> = modelBundle.animationControllers

        override fun getAnimations(
            modelBundle: ProjectileModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, Animation> = modelBundle.animations

        override fun getConditionArmor(
            modelBundle: ProjectileModelBundle,
            resourceBundle: ModelResourceBundle
        ): ConditionArmor? = null
    }

    private const val PROJECTILE_PREFIX: String = "projectile"

    @JvmField
    val REGISTRY: ProcessorPipeline<GeckoProjectileEntity, ProjectileModelBundle> = ProcessorPipeline()

    @JvmStatic
    fun registerControllers() {
        registerNamedController("pre_main", null, true) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.0f, StopAnimationPredicate())
        }
        registerNamedController(
            "main",
            ProjectileAnimationPredicate.ENVIRONMENT_STATES,
            true
        ) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.1f, ProjectileAnimationPredicate())
        }
        registerNamedController("post_main", null, true) { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.0f, StopAnimationPredicate())
        }
        registerParallelController("parallel") { animationEntryKey, entity, linkedAnimationName ->
            CompositeAnimationController(
                entity,
                animationEntryKey,
                0.0f,
                if (linkedAnimationName != null) NamedAnimationPredicate(linkedAnimationName) else StopAnimationPredicate.INSTANCE,
                true
            )
        }
    }

    @JvmStatic
    fun buildControllers(
        modelBundle: ProjectileModelBundle,
        resourceBundle: ModelResourceBundle
    ): Consumer<GeckoProjectileEntity> {
        if (REGISTRY.isEmpty()) {
            registerControllers()
        }
        return REGISTRY.buildAll(modelBundle, resourceBundle)
    }

    @JvmStatic
    fun registerController(
        controllerName: String,
        controllerFactory: BiFunction<String, GeckoProjectileEntity, IAnimationController<GeckoProjectileEntity>>
    ): ModelProcessor<GeckoProjectileEntity, ProjectileModelBundle> {
        val controllerKey = "$PROJECTILE_PREFIX.$controllerName"
        return REGISTRY.register { _, _ ->
            { entity, consumer -> consumer.accept(controllerFactory.apply(controllerKey, entity)) }
        }
    }

    @JvmStatic
    fun registerNamedController(
        slotName: String,
        requiredAnimations: Array<String>?,
        checkAnimationEntries: Boolean,
        controllerFactory: BiFunction<String, GeckoProjectileEntity, IAnimationController<GeckoProjectileEntity>>
    ): ModelProcessor<GeckoProjectileEntity, ProjectileModelBundle> {
        return REGISTRY.register(
            NamedModelProcessor(
                PROJECTILE_PREFIX,
                slotName,
                requiredAnimations,
                checkAnimationEntries,
                ProjectileAnimationDataProvider,
                controllerFactory
            )
        )
    }

    @JvmStatic
    fun registerParallelController(
        slotName: String,
        controllerFactory: TriFunction<String, GeckoProjectileEntity, String, IAnimationController<GeckoProjectileEntity>>
    ): ModelProcessor<GeckoProjectileEntity, ProjectileModelBundle> {
        return REGISTRY.register(
            ParallelProcessor(
                PROJECTILE_PREFIX,
                slotName,
                false,
                ProjectileAnimationDataProvider,
                controllerFactory
            )
        )
    }
}