package rip.ysm.compat.touhoulittlemaid.fabric

import com.elfmcys.yesstevemodel.network.message.FeedbackData
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import rip.ysm.compat.ModCompat
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidEventHandler
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidModelHandler

object TouhouMaidCompatImpl : ModCompat("touhou_little_maid") {
    fun isMaidEntity(entity: Entity): Boolean = MaidEventHandler.isMaid(entity)

    fun handleProjectileOwner(projectile: Projectile, entity: Entity) {
    }

    fun registerAnimationRoulette(entity: Entity, classify: String, index: Int) {
        MaidModelHandler.activateRouletteAnimation(entity, classify, index)
    }

    fun applyFeedback(entity: Entity, message: FeedbackData) {
    }

    @Environment(EnvType.CLIENT)
    fun playMaidAnimation(entity: Entity, expression: String) {
        MaidModelHandler.executeMaidMolang(entity, expression)
    }
}
