package rip.ysm.compat.touhoulittlemaid.fabric

import com.elfmcys.yesstevemodel.network.message.FeedbackData
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidEventHandler
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidModelHandler

object TouhouMaidCompatImpl {
    @JvmStatic
    fun isMaidEntity(entity: Entity): Boolean = MaidEventHandler.isMaid(entity)

    @JvmStatic
    fun handleProjectileOwner(projectile: Projectile, entity: Entity) {
    }

    @JvmStatic
    fun registerAnimationRoulette(entity: Entity, classify: String, index: Int) {
        MaidModelHandler.activateRouletteAnimation(entity, classify, index)
    }

    @JvmStatic
    fun applyFeedback(entity: Entity, message: FeedbackData) {
    }

    @Environment(EnvType.CLIENT)
    @JvmStatic
    fun playMaidAnimation(entity: Entity, expression: String) {
        MaidModelHandler.executeMaidMolang(entity, expression)
    }
}
