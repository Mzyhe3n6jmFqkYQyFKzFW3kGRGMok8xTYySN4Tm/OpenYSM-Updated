package rip.ysm.compat.touhoulittlemaid

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.ModCompat
import rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

// TODO: Fix anim on maid
object TouhouLittleMaidCompat : ModCompat("touhou_little_maid") {
    fun isMaidEntity(entity: Entity): Boolean = isModLoaded && TouhouLittleMaidCompatImpl.isMaidEntity(entity)

    fun isMaidRideable(entity: Entity): Boolean = isModLoaded && TouhouLittleMaidCompatImpl.isMaidRideable(entity)

    fun isSimplePlanesEntity(entity: Entity): Boolean =
        isModLoaded && TouhouLittleMaidCompatImpl.isSimplePlanesEntity(entity)

    fun isImmersiveAircraftEntity(entity: Entity): Boolean =
        isModLoaded && TouhouLittleMaidCompatImpl.isImmersiveAircraftEntity(entity)

    fun isMaidItem(item: Item): Boolean = isModLoaded && TouhouLittleMaidCompatImpl.isMaidItem(item)

    fun getMaidEntityId(entity: Entity): String? {
        if (!isModLoaded) return null
        return TouhouLittleMaidCompatImpl.getMaidEntityId(entity)
    }

    fun isMaidSitting(livingEntity: LivingEntity): Boolean =
        isModLoaded && TouhouLittleMaidCompatImpl.isMaidSitting(livingEntity)

    fun registerMaidAnimStates(tlmBinding: TLMBinding) {
        if (isModLoaded)
            TouhouLittleMaidCompatImpl.registerMaidAnimStates(tlmBinding)
        else
            registerDummyBindings(tlmBinding)
    }

    private fun registerDummyBindings(tlmBinding: TLMBinding) {
        tlmBinding.livingEntityVar("is_begging") { false }
        tlmBinding.livingEntityVar("is_sitting") { false }
        tlmBinding.livingEntityVar("has_backpack") { false }
        tlmBinding.livingEntityVar("favorability_point") { 0 }
        tlmBinding.livingEntityVar("favorability_level") { 0 }
        tlmBinding.livingEntityVar("task_id") { StringPool.EMPTY }
        tlmBinding.livingEntityVar("schedule") { StringPool.EMPTY }
        tlmBinding.livingEntityVar("activity") { StringPool.EMPTY }
        tlmBinding.livingEntityVar("gomoku_win_count") { 0 }
        tlmBinding.livingEntityVar("gomoku_rank") { 1 }
        tlmBinding.livingEntityVar("game_statue") { StringPool.EMPTY }
        tlmBinding.livingEntityVar("backpack_type") { StringPool.EMPTY }
        tlmBinding.livingEntityVar("is_entity") { true }
        tlmBinding.livingEntityVar("is_statue") { false }
        tlmBinding.livingEntityVar("is_garage_kit") { false }
        tlmBinding.livingEntityVar("show_item") { StringPool.EMPTY }
    }

    fun handleMaidInteraction(
        event: AnimationEvent<LivingAnimatable<*>>,
        livingEntity: LivingEntity,
        entity: Entity
    ): PlayState? {
        if (!isModLoaded) return null
        return TouhouLittleMaidCompatImpl.handleMaidInteraction(event, livingEntity, entity)
    }

    fun isMaidChatAvailable(): Boolean = isModLoaded && TouhouLittleMaidCompatImpl.isMaidChatAvailable()

    fun openMaidChat() {
        if (!isModLoaded) return
        TouhouLittleMaidCompatImpl.openMaidChat()
    }

    fun buildControllers(
        modelBundle: PlayerModelBundle,
        resourceBundle: ModelResourceBundle
    ): ((MaidAnimatable) -> Unit)? {
        if (!isModLoaded) return null
        return TouhouLittleMaidCompatImpl.buildControllers(modelBundle, resourceBundle)
    }
}
