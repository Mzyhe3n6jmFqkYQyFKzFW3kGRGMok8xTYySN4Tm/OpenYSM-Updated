package rip.ysm.compat.touhoulittlemaid

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.network.message.FeedbackData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import rip.ysm.compat.ModCompat
import rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl

object TouhouMaidCompat : ModCompat("touhou_little_maid") {
    init {
        Constants.doNothing(TouhouMaidCompatImpl)
    }

    @JvmStatic
    fun isMaidEntity(entity: Entity): Boolean = isModLoaded && TouhouMaidCompatImpl.isMaidEntity(entity)

    @JvmStatic
    fun handleProjectileOwner(projectile: Projectile, entity: Entity) {
        if (!isModLoaded) return
        TouhouMaidCompatImpl.handleProjectileOwner(projectile, entity)
    }

    @JvmStatic
    fun registerAnimationRoulette(entity: Entity, classify: String, index: Int) {
        if (!isModLoaded) return
        TouhouMaidCompatImpl.registerAnimationRoulette(entity, classify, index)
    }

    @JvmStatic
    fun applyFeedback(entity: Entity, message: FeedbackData) {
        if (!isModLoaded) return
        TouhouMaidCompatImpl.applyFeedback(entity, message)
    }

    @JvmStatic
    fun playMaidAnimation(entity: Entity, expression: String) {
        if (!isModLoaded) return
        TouhouMaidCompatImpl.playMaidAnimation(entity, expression)
    }
}
