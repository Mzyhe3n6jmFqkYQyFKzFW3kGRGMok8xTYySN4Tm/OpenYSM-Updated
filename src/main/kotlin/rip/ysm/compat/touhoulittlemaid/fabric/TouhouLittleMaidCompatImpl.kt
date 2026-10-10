package rip.ysm.compat.touhoulittlemaid.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.ModCompat
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.*
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim.MaidAnimationController

object TouhouLittleMaidCompatImpl : ModCompat("touhou_little_maid") {
    private fun initClient() {
        if (!isModLoaded) return
        MaidClientSetup.init()
    }

    init {
        initClient()
    }

    fun isMaidEntity(entity: Entity): Boolean = MaidEventHandler.isMaid(entity)

    fun isMaidRideable(entity: Entity): Boolean = MaidEventHandler.isYsmModelMaid(entity)

    fun isSimplePlanesEntity(entity: Entity): Boolean = MaidEventHandler.isChair(entity)

    fun isImmersiveAircraftEntity(entity: Entity): Boolean = MaidEventHandler.isSit(entity)

    fun isMaidItem(item: Item): Boolean = MaidEventHandler.isGohei(item)

    fun getMaidEntityId(entity: Entity): String = MaidEventHandler.getChairModelId(entity)

    fun isMaidSitting(livingEntity: LivingEntity): Boolean = MaidEventHandler.isMaidFishing(livingEntity)

    fun registerMaidAnimStates(tlmBinding: TLMBinding) = MaidBinding.registerBindings(tlmBinding)

    fun handleMaidInteraction(
        event: AnimationEvent<LivingAnimatable<*>>,
        livingEntity: LivingEntity,
        entity: Entity
    ): PlayState? = MaidInteractionAnimHandler.handleMaidInteractionAnim(event, livingEntity, entity)

    fun isMaidChatAvailable(): Boolean = MaidAnimationRoulette.canOpenRoulette()

    fun openMaidChat() {
        MaidAnimationRoulette.openRouletteScreen()
    }

    fun buildControllers(
        modelBundle: PlayerModelBundle,
        resourceBundle: ModelResourceBundle
    ): (MaidAnimatable) -> Unit = MaidAnimationController.buildControllers(modelBundle, resourceBundle)
}
