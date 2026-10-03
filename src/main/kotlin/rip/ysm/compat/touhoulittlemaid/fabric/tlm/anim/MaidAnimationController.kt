package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.client.animation.predicate.ArmorPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.InteractionHandAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.ItemHoldAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.LivingMovementAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.MainHandHoldPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.OffHandHoldPredicate
import com.elfmcys.yesstevemodel.client.animation.predicate.OffhandAttackAnimationPredicate
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.client.model.processor.ArmorSlotProcessor
import com.elfmcys.yesstevemodel.client.model.processor.ControllerSlotBinder
import com.elfmcys.yesstevemodel.client.model.processor.ModelProcessor
import com.elfmcys.yesstevemodel.client.model.processor.NamedModelProcessor
import com.elfmcys.yesstevemodel.client.model.processor.ParallelProcessor
import com.elfmcys.yesstevemodel.client.model.processor.ProcessorPipeline
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

class MaidAnimationController {
    constructor() {
    }
    class MaidAnimationDataProvider : AnimationDataProvider<PlayerModelBundle> {
        constructor() {
        }
        open fun getAnimationEntries(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): Object2ReferenceMap<String, AnimationController> {
            modelBundle.getAnimationEntries()
        }
        open fun getAnimations(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): Object2ReferenceMap<String, Animation> {
            modelBundle.getMainAnimations()
        }
        open fun getConditionArmor(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): ConditionArmor {
            modelBundle.getConditionManager().getArmor()
        }
        companion object {
            @JvmField var INSTANCE: MaidAnimationDataProvider = MaidAnimationDataProvider()
        }
    }
    companion object {
        @JvmField var PLAYER_PREFIX: String = "player"
        @JvmField var MAID_PREFIX: String = "maid"
        @JvmField var REGISTRY: ProcessorPipeline<MaidAnimatable, PlayerModelBundle> = ProcessorPipeline()
        @JvmStatic fun buildControllers(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): Consumer<MaidAnimatable> {
            if (REGISTRY.isEmpty()) {
                registerControllers()
            }
            return REGISTRY.buildAll(modelBundle, resourceBundle)
        }
        @JvmStatic fun registerControllers() {
            registerParallelController("pre_parallel", { key, animatable, linkedName -> 
CompositeAnimationController(animatable, key, 0.0f, if (linkedName != null) NamedAnimationPredicate(linkedName) else StopAnimationPredicate.INSTANCE)
 })
            registerController("vehicle", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.1f, LivingMovementAnimationPredicate())
 })
            registerSlotController("pre_main", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerController("main", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.1f, MaidAnimationPredicate())
 })
            registerSlotController("post_main", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerSlotController("pre_hold", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerController("hold_offhand", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.1f, OffHandHoldPredicate())
 })
            registerController("hold_mainhand", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.1f, MainHandHoldPredicate())
 })
            registerSlotController("post_hold", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerSlotController("pre_swing", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerController("swing", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, ItemHoldAnimationPredicate())
 })
            registerSlotController("post_swing", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerSlotController("pre_use", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerController("use", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.1f, InteractionHandAnimationPredicate())
 })
            registerSlotController("post_use", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, StopAnimationPredicate())
 })
            registerNamedController("misc", MaidGameStateAnimationPredicate.GAME_STATE_ANIMATIONS, true, { key, animatable -> 
CompositeAnimationController(animatable, key, 0.1f, MaidGameStateAnimationPredicate())
 })
            registerController("passenger", { key, animatable -> 
CompositeAnimationController(animatable, key, 0.1f, OffhandAttackAnimationPredicate())
 })
            registerController("cap", { key, animatable -> 
PredicateBasedController(animatable, key, 0.0f, MaidIdleAnimPredicate())
 })
            registerParallelController("parallel", { key, animatable, linkedName -> 
CompositeAnimationController(animatable, key, 0.0f, if (linkedName != null) NamedAnimationPredicate(linkedName) else StopAnimationPredicate.INSTANCE, true)
 })
            registerArmorController("armor", { key, animatable, equipmentSlot -> 
CompositeAnimationController(animatable, key, 0.0f, ArmorPredicate(equipmentSlot))
 })
            registerNamedController("statue", MaidStatusAnimationPredicate.RENDER_STATES, true, { key, animatable -> 
CompositeAnimationController(animatable, key, 0.0f, MaidStatusAnimationPredicate())
 })
        }
        @JvmStatic fun registerController(controllerName: String, controllerFactory: BiFunction<String, MaidAnimatable, IAnimationController<MaidAnimatable>>) {
            var controllerKey: String = String.format("%s.%s", PLAYER_PREFIX, controllerName)
            var processor: ModelProcessor<MaidAnimatable, PlayerModelBundle> = { modelBundle, resourceBundle -> 
{ animatable, consumer -> 
consumer.accept(controllerFactory.apply(controllerKey, animatable))
 }
 }
            REGISTRY.register(processor)
        }
        @JvmStatic fun registerSlotController(slotName: String, controllerFactory: BiFunction<String, MaidAnimatable, IAnimationController<MaidAnimatable>>) {
            REGISTRY.register(ControllerSlotBinder(PLAYER_PREFIX, slotName, MaidAnimationDataProvider.INSTANCE, controllerFactory))
        }
        @JvmStatic fun registerNamedController(slotName: String, requiredAnimations: Array<String>, checkAnimationEntries: Boolean, controllerFactory: BiFunction<String, MaidAnimatable, IAnimationController<MaidAnimatable>>) {
            REGISTRY.register(NamedModelProcessor(MAID_PREFIX, slotName, requiredAnimations, checkAnimationEntries, MaidAnimationDataProvider.INSTANCE, controllerFactory))
        }
        @JvmStatic fun registerParallelController(slotName: String, controllerFactory: TriFunction<String, MaidAnimatable, String, IAnimationController<MaidAnimatable>>) {
            REGISTRY.register(ParallelProcessor(PLAYER_PREFIX, slotName, true, MaidAnimationDataProvider.INSTANCE, controllerFactory))
        }
        @JvmStatic fun registerArmorController(category: String, controllerFactory: TriFunction<String, MaidAnimatable, EquipmentSlot, IAnimationController<MaidAnimatable>>) {
            REGISTRY.register(ArmorSlotProcessor(PLAYER_PREFIX, category, MaidAnimationDataProvider.INSTANCE, controllerFactory))
        }
    }
}