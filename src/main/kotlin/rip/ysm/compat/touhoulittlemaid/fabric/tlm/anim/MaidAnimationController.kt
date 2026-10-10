package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.client.animation.predicate.*
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
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.EquipmentSlot
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

@Environment(EnvType.CLIENT)
object MaidAnimationController {
    private const val PLAYER_PREFIX = "player"
    private const val MAID_PREFIX = "maid"

    private val REGISTRY: ProcessorPipeline<MaidAnimatable, PlayerModelBundle> = ProcessorPipeline()

    fun buildControllers(
        modelBundle: PlayerModelBundle,
        resourceBundle: ModelResourceBundle
    ): (MaidAnimatable) -> Unit {
        if (REGISTRY.isEmpty()) registerControllers()
        return REGISTRY.buildAll(modelBundle, resourceBundle)
    }

    private fun registerControllers() {
        registerParallelController("pre_parallel") { key, animatable, linkedName ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                if (linkedName != null) NamedAnimationPredicate(linkedName) else StopAnimationPredicate
            )
        }

        registerController("vehicle") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                LivingMovementAnimationPredicate()
            )
        }

        registerSlotController("pre_main") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerController("main") { key, animatable ->
            CompositeAnimationController(animatable, key, 0.1f, MaidAnimationPredicate())
        }

        registerSlotController("post_main") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerSlotController("pre_hold") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerController("hold_offhand") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                OffHandHoldPredicate()
            )
        }

        registerController("hold_mainhand") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                MainHandHoldPredicate()
            )
        }

        registerSlotController("post_hold") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerSlotController("pre_swing") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerController("swing") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                ItemHoldAnimationPredicate()
            )
        }

        registerSlotController("post_swing") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerSlotController("pre_use") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerController("use") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                InteractionHandAnimationPredicate()
            )
        }

        registerSlotController("post_use") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate
            )
        }

        registerNamedController(
            "misc",
            MaidGameStateAnimationPredicate.GAME_STATE_ANIMATIONS,
            true
        ) { key, animatable ->
            CompositeAnimationController(animatable, key, 0.1f, MaidGameStateAnimationPredicate())
        }

        registerController("passenger") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                OffhandAttackAnimationPredicate()
            )
        }

        registerController("cap") { key, animatable ->
            PredicateBasedController(animatable, key, 0.0f, MaidIdleAnimPredicate())
        }

        registerParallelController("parallel") { key, animatable, linkedName ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                if (linkedName != null) NamedAnimationPredicate(linkedName) else StopAnimationPredicate,
                true
            )
        }

        registerArmorController("armor") { key, animatable, equipmentSlot ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                ArmorPredicate(equipmentSlot)
            )
        }

        registerNamedController(
            "statue",
            MaidStatusAnimationPredicate.RENDER_STATES,
            true
        ) { key, animatable ->
            CompositeAnimationController(animatable, key, 0.0f, MaidStatusAnimationPredicate())
        }
    }

    private fun registerController(
        controllerName: String,
        controllerFactory: (String, MaidAnimatable) -> IAnimationController<MaidAnimatable>
    ) {
        val controllerKey = "$PLAYER_PREFIX.$controllerName"
        val processor: ModelProcessor<MaidAnimatable, PlayerModelBundle> =
            ModelProcessor { _, _ ->
                ControllerFactory { animatable, consumer ->
                    consumer(controllerFactory(controllerKey, animatable))
                }
            }
        REGISTRY.register(processor)
    }

    private fun registerSlotController(
        slotName: String,
        controllerFactory: (String, MaidAnimatable) -> IAnimationController<MaidAnimatable>
    ) {
        REGISTRY.register(
            ControllerSlotBinder(
                PLAYER_PREFIX,
                slotName,
                MaidAnimationDataProvider,
                controllerFactory
            )
        )
    }

    private fun registerNamedController(
        slotName: String,
        requiredAnimations: Array<String>?,
        checkAnimationEntries: Boolean,
        controllerFactory: (String, MaidAnimatable) -> IAnimationController<MaidAnimatable>
    ) {
        REGISTRY.register(
            NamedModelProcessor(
                MAID_PREFIX,
                slotName,
                requiredAnimations,
                checkAnimationEntries,
                MaidAnimationDataProvider,
                controllerFactory
            )
        )
    }

    private fun registerParallelController(
        slotName: String,
        controllerFactory: (String, MaidAnimatable, String?) -> IAnimationController<MaidAnimatable>
    ) {
        REGISTRY.register(
            ParallelProcessor(
                PLAYER_PREFIX,
                slotName,
                true,
                MaidAnimationDataProvider,
                controllerFactory
            )
        )
    }

    private fun registerArmorController(
        category: String,
        controllerFactory: (String, MaidAnimatable, EquipmentSlot) -> IAnimationController<MaidAnimatable>
    ) {
        REGISTRY.register(
            ArmorSlotProcessor(
                PLAYER_PREFIX,
                category,
                MaidAnimationDataProvider,
                controllerFactory
            )
        )
    }

    private object MaidAnimationDataProvider : AnimationDataProvider<PlayerModelBundle> {
        override fun getAnimationEntries(
            t: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, AnimationController> = t.animationEntries

        override fun getAnimations(
            t: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, Animation> = t.mainAnimations

        override fun getConditionArmor(
            t: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): ConditionArmor = t.conditionManager.armor
    }
}
