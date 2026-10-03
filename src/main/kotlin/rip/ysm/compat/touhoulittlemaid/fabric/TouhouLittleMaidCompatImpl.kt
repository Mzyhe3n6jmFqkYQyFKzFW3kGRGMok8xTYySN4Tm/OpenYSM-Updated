package rip.ysm.compat.touhoulittlemaid.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimationRoulette
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidBinding
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidEventHandler
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidInteractionAnimHandler
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim.MaidAnimationController

class TouhouLittleMaidCompatImpl {
    constructor() {
    }
    companion object {
        @JvmField var MOD_ID: String = "touhou_little_maid"
        @JvmField var IS_LOADED: Boolean = FabricLoader.getInstance().isModLoaded(MOD_ID)
        @JvmStatic fun isLoaded(): Boolean {
            IS_LOADED
        }
        @JvmStatic fun initClient() {
            if (isLoaded()) {
                rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidClientSetup.init()
            }
        }
        @JvmStatic fun isMaidEntity(entity: Entity): Boolean {
            isLoaded() && MaidEventHandler.isMaid(entity)
        }
        @JvmStatic fun isMaidRideable(entity: Entity): Boolean {
            isLoaded() && MaidEventHandler.isYsmModelMaid(entity)
        }
        @JvmStatic fun isSimplePlanesEntity(entity: Entity): Boolean {
            isLoaded() && MaidEventHandler.isChair(entity)
        }
        @JvmStatic fun isImmersiveAircraftEntity(entity: Entity): Boolean {
            isLoaded() && MaidEventHandler.isSit(entity)
        }
        @JvmStatic fun isMaidItem(item: Item): Boolean {
            isLoaded() && MaidEventHandler.isGohei(item)
        }
        @JvmStatic fun getMaidEntityId(entity: Entity): String {
            if (isLoaded()) MaidEventHandler.getChairModelId(entity) else StringPool.EMPTY
        }
        @JvmStatic fun isMaidSitting(livingEntity: LivingEntity): Boolean {
            isLoaded() && MaidEventHandler.isMaidFishing(livingEntity)
        }
        @JvmStatic fun registerMaidAnimStates(tlmBinding: TLMBinding) {
            if (isLoaded()) {
                MaidBinding.registerBindings(tlmBinding)
            } else {
                registerDummyBindings(tlmBinding)
            }
        }
        @JvmStatic fun registerDummyBindings(tlmBinding: TLMBinding) {
            tlmBinding.livingEntityVar("is_begging", { ctx -> 
false
 })
            tlmBinding.livingEntityVar("is_sitting", { ctx -> 
false
 })
            tlmBinding.livingEntityVar("has_backpack", { ctx -> 
false
 })
            tlmBinding.livingEntityVar("favorability_point", { ctx -> 
0
 })
            tlmBinding.livingEntityVar("favorability_level", { ctx -> 
0
 })
            tlmBinding.livingEntityVar("task_id", { ctx -> 
StringPool.EMPTY
 })
            tlmBinding.livingEntityVar("schedule", { ctx -> 
StringPool.EMPTY
 })
            tlmBinding.livingEntityVar("activity", { ctx -> 
StringPool.EMPTY
 })
            tlmBinding.livingEntityVar("gomoku_win_count", { ctx -> 
0
 })
            tlmBinding.livingEntityVar("gomoku_rank", { ctx -> 
1
 })
            tlmBinding.livingEntityVar("game_statue", { ctx -> 
StringPool.EMPTY
 })
            tlmBinding.livingEntityVar("backpack_type", { ctx -> 
StringPool.EMPTY
 })
            tlmBinding.livingEntityVar("is_entity", { ctx -> 
true
 })
            tlmBinding.livingEntityVar("is_statue", { ctx -> 
false
 })
            tlmBinding.livingEntityVar("is_garage_kit", { ctx -> 
false
 })
            tlmBinding.livingEntityVar("show_item", { ctx -> 
StringPool.EMPTY
 })
        }
        @JvmStatic fun handleMaidInteraction(event: AnimationEvent<LivingAnimatable<*>>, livingEntity: LivingEntity, entity: Entity): PlayState {
            if (isLoaded()) {
                MaidInteractionAnimHandler.handleMaidInteractionAnim(event, livingEntity, entity)
            }
            return null
        }
        @JvmStatic fun isMaidChatAvailable(): Boolean {
            isLoaded() && MaidAnimationRoulette.canOpenRoulette()
        }
        @JvmStatic fun openMaidChat() {
            if (isLoaded()) {
                MaidAnimationRoulette.openRouletteScreen()
            }
        }
        @JvmStatic fun buildControllers(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): Any {
            if (!isLoaded()) {
                null
            }
            return MaidAnimationController.buildControllers(modelBundle, resourceBundle)
        }
    }
}