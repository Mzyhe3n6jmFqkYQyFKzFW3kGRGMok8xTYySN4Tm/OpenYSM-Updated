package rip.ysm.compat.touhoulittlemaid

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item

class TouhouLittleMaidCompat {
    constructor() {
    }
    companion object {
        @JvmStatic fun isLoaded(): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isLoaded()
        }
        @JvmStatic fun isMaidEntity(entity: Entity): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidEntity(entity)
        }
        @JvmStatic fun isMaidRideable(entity: Entity): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidRideable(entity)
        }
        @JvmStatic fun isSimplePlanesEntity(entity: Entity): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isSimplePlanesEntity(entity)
        }
        @JvmStatic fun isImmersiveAircraftEntity(entity: Entity): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isImmersiveAircraftEntity(entity)
        }
        @JvmStatic fun isMaidItem(item: Item): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidItem(item)
        }
        @JvmStatic fun getMaidEntityId(entity: Entity): String {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.getMaidEntityId(entity)
        }
        @JvmStatic fun isMaidSitting(livingEntity: LivingEntity): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidSitting(livingEntity)
        }
        @JvmStatic fun registerMaidAnimStates(tlmBinding: TLMBinding) {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.registerMaidAnimStates(tlmBinding)
        }
        @JvmStatic fun handleMaidInteraction(event: AnimationEvent<LivingAnimatable<*>>, livingEntity: LivingEntity, entity: Entity): PlayState {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.handleMaidInteraction(event, livingEntity, entity)
        }
        @JvmStatic fun isMaidChatAvailable(): Boolean {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidChatAvailable()
        }
        @JvmStatic fun openMaidChat() {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.openMaidChat()
        }
        @JvmStatic fun buildControllers(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): Any {
            rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.buildControllers(modelBundle, resourceBundle)
        }
    }
}