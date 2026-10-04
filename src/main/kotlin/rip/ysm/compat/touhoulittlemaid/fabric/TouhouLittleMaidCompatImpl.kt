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
    @JvmStatic
    private fun initClient() {
        if (!isModLoaded) return
        MaidClientSetup.init()
    }

    init {
        initClient()
    }

    @JvmStatic
    fun isMaidEntity(entity: Entity): Boolean = MaidEventHandler.isMaid(entity)

    @JvmStatic
    fun isMaidRideable(entity: Entity): Boolean = MaidEventHandler.isYsmModelMaid(entity)

    @JvmStatic
    fun isSimplePlanesEntity(entity: Entity): Boolean = MaidEventHandler.isChair(entity)

    @JvmStatic
    fun isImmersiveAircraftEntity(entity: Entity): Boolean = MaidEventHandler.isSit(entity)

    @JvmStatic
    fun isMaidItem(item: Item): Boolean = MaidEventHandler.isGohei(item)

    @JvmStatic
    fun getMaidEntityId(entity: Entity): String = MaidEventHandler.getChairModelId(entity)

    @JvmStatic
    fun isMaidSitting(livingEntity: LivingEntity): Boolean = MaidEventHandler.isMaidFishing(livingEntity)

    @JvmStatic
    fun registerMaidAnimStates(tlmBinding: TLMBinding) = MaidBinding.registerBindings(tlmBinding)

    @JvmStatic
    fun handleMaidInteraction(
        event: AnimationEvent<LivingAnimatable<*>>,
        livingEntity: LivingEntity,
        entity: Entity
    ): PlayState? = MaidInteractionAnimHandler.handleMaidInteractionAnim(event, livingEntity, entity)

    @JvmStatic
    fun isMaidChatAvailable(): Boolean = MaidAnimationRoulette.canOpenRoulette()

    @JvmStatic
    fun openMaidChat() {
        MaidAnimationRoulette.openRouletteScreen()
    }

    @JvmStatic
    fun buildControllers(
        modelBundle: PlayerModelBundle,
        resourceBundle: ModelResourceBundle
    ): (MaidAnimatable) -> Unit = MaidAnimationController.buildControllers(modelBundle, resourceBundle)
}
