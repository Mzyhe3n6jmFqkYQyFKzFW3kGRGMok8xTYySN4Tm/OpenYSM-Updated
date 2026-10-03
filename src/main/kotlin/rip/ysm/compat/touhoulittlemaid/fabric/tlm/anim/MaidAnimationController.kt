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
import org.apache.commons.lang3.function.TriFunction
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable
import java.util.function.BiFunction
import java.util.function.Consumer

@Suppress("UNCHECKED_CAST")
@Environment(EnvType.CLIENT)
object MaidAnimationController {
    private const val PLAYER_PREFIX = "player"
    private const val MAID_PREFIX = "maid"

    private val REGISTRY: ProcessorPipeline<MaidAnimatable, PlayerModelBundle> = ProcessorPipeline()

    @JvmStatic
    fun buildControllers(
        modelBundle: PlayerModelBundle,
        resourceBundle: ModelResourceBundle
    ): Consumer<MaidAnimatable> {
        if (REGISTRY.isEmpty()) {
            registerControllers()
        }
        return REGISTRY.buildAll(modelBundle, resourceBundle)
    }

    private fun registerControllers() {
        registerParallelController("pre_parallel") { key, animatable, linkedName ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                (if (linkedName != null) NamedAnimationPredicate(linkedName) else StopAnimationPredicate.INSTANCE) as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerController("vehicle") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                LivingMovementAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerSlotController("pre_main") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerController("main") { key, animatable ->
            CompositeAnimationController(animatable, key, 0.1f, MaidAnimationPredicate())
        }

        registerSlotController("post_main") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerSlotController("pre_hold") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerController("hold_offhand") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                OffHandHoldPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerController("hold_mainhand") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                MainHandHoldPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerSlotController("post_hold") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerSlotController("pre_swing") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerController("swing") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                ItemHoldAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerSlotController("post_swing") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerSlotController("pre_use") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerController("use") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.1f,
                InteractionHandAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerSlotController("post_use") { key, animatable ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                StopAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
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
                OffhandAttackAnimationPredicate() as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
            )
        }

        registerController("cap") { key, animatable ->
            PredicateBasedController(animatable, key, 0.0f, MaidIdleAnimPredicate())
        }

        registerParallelController("parallel") { key, animatable, linkedName ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                (if (linkedName != null) NamedAnimationPredicate(linkedName) else StopAnimationPredicate.INSTANCE) as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>,
                true
            )
        }

        registerArmorController("armor") { key, animatable, equipmentSlot ->
            CompositeAnimationController(
                animatable, key, 0.0f,
                ArmorPredicate(equipmentSlot) as Any as com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate<MaidAnimatable>
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
        controllerFactory: BiFunction<String, MaidAnimatable, IAnimationController<MaidAnimatable>>
    ) {
        val controllerKey = "$PLAYER_PREFIX.$controllerName"
        val processor: ModelProcessor<MaidAnimatable, PlayerModelBundle> =
            ModelProcessor { _, _ ->
                ControllerFactory { animatable, consumer ->
                    consumer.accept(controllerFactory.apply(controllerKey, animatable))
                }
            }
        REGISTRY.register(processor)
    }

    private fun registerSlotController(
        slotName: String,
        controllerFactory: BiFunction<String, MaidAnimatable, IAnimationController<MaidAnimatable>>
    ) {
        REGISTRY.register(
            ControllerSlotBinder(
                PLAYER_PREFIX,
                slotName,
                MaidAnimationDataProvider.INSTANCE,
                controllerFactory
            )
        )
    }

    private fun registerNamedController(
        slotName: String,
        requiredAnimations: Array<String>,
        checkAnimationEntries: Boolean,
        controllerFactory: BiFunction<String, MaidAnimatable, IAnimationController<MaidAnimatable>>
    ) {
        REGISTRY.register(
            NamedModelProcessor(
                MAID_PREFIX,
                slotName,
                requiredAnimations,
                checkAnimationEntries,
                MaidAnimationDataProvider.INSTANCE,
                controllerFactory
            )
        )
    }

    private fun registerParallelController(
        slotName: String,
        controllerFactory: TriFunction<String, MaidAnimatable, String, IAnimationController<MaidAnimatable>>
    ) {
        REGISTRY.register(
            ParallelProcessor(
                PLAYER_PREFIX,
                slotName,
                true,
                MaidAnimationDataProvider.INSTANCE,
                controllerFactory
            )
        )
    }

    private fun registerArmorController(
        category: String,
        controllerFactory: TriFunction<String, MaidAnimatable, EquipmentSlot, IAnimationController<MaidAnimatable>>
    ) {
        REGISTRY.register(
            ArmorSlotProcessor(
                PLAYER_PREFIX,
                category,
                MaidAnimationDataProvider.INSTANCE,
                controllerFactory
            )
        )
    }

    private object MaidAnimationDataProvider : AnimationDataProvider<PlayerModelBundle> {
        val INSTANCE = MaidAnimationDataProvider

        override fun getAnimationEntries(
            modelBundle: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, AnimationController> {
            return modelBundle.animationEntries
        }

        override fun getAnimations(
            modelBundle: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): Object2ReferenceMap<String, Animation> {
            return modelBundle.mainAnimations
        }

        override fun getConditionArmor(
            modelBundle: PlayerModelBundle,
            resourceBundle: ModelResourceBundle
        ): ConditionArmor {
            return modelBundle.conditionManager.armor
        }
    }
}
