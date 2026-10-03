package rip.ysm.compat.touhoulittlemaid

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl

object TouhouLittleMaidCompat {
    @JvmStatic
    fun isLoaded(): Boolean = TouhouLittleMaidCompatImpl.isLoaded()

    @JvmStatic
    fun isMaidEntity(entity: Entity): Boolean = TouhouLittleMaidCompatImpl.isMaidEntity(entity)

    @JvmStatic
    fun isMaidRideable(entity: Entity): Boolean = TouhouLittleMaidCompatImpl.isMaidRideable(entity)

    @JvmStatic
    fun isSimplePlanesEntity(entity: Entity): Boolean = TouhouLittleMaidCompatImpl.isSimplePlanesEntity(entity)

    @JvmStatic
    fun isImmersiveAircraftEntity(entity: Entity): Boolean =
        TouhouLittleMaidCompatImpl.isImmersiveAircraftEntity(entity)

    @JvmStatic
    fun isMaidItem(item: Item): Boolean = TouhouLittleMaidCompatImpl.isMaidItem(item)

    @JvmStatic
    fun getMaidEntityId(entity: Entity): String = TouhouLittleMaidCompatImpl.getMaidEntityId(entity)

    @JvmStatic
    fun isMaidSitting(livingEntity: LivingEntity): Boolean = TouhouLittleMaidCompatImpl.isMaidSitting(livingEntity)

    @JvmStatic
    fun registerMaidAnimStates(tlmBinding: TLMBinding) {
        TouhouLittleMaidCompatImpl.registerMaidAnimStates(tlmBinding)
    }

    @JvmStatic
    fun handleMaidInteraction(
        event: AnimationEvent<LivingAnimatable<*>>,
        livingEntity: LivingEntity,
        entity: Entity
    ): PlayState? = TouhouLittleMaidCompatImpl.handleMaidInteraction(event, livingEntity, entity)

    @JvmStatic
    fun isMaidChatAvailable(): Boolean = TouhouLittleMaidCompatImpl.isMaidChatAvailable()

    @JvmStatic
    fun openMaidChat() {
        TouhouLittleMaidCompatImpl.openMaidChat()
    }

    @JvmStatic
    fun buildControllers(modelBundle: PlayerModelBundle, resourceBundle: ModelResourceBundle): java.util.function.Consumer<rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable>? =
        TouhouLittleMaidCompatImpl.buildControllers(modelBundle, resourceBundle)
}
