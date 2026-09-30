package rip.ysm.compat.touhoulittlemaid

import com.elfmcys.yesstevemodel.network.message.FeedbackData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl

object TouhouMaidCompat {
    @JvmStatic
    fun isLoaded(): Boolean = TouhouMaidCompatImpl.isLoaded()

    @JvmStatic
    fun init() {
        TouhouMaidCompatImpl.init()
    }

    @JvmStatic
    fun isMaidEntity(entity: Entity): Boolean = TouhouMaidCompatImpl.isMaidEntity(entity)

    @JvmStatic
    fun handleProjectileOwner(projectile: Projectile, entity: Entity) {
        TouhouMaidCompatImpl.handleProjectileOwner(projectile, entity)
    }

    @JvmStatic
    fun registerAnimationRoulette(entity: Entity, classify: String, index: Int) {
        TouhouMaidCompatImpl.registerAnimationRoulette(entity, classify, index)
    }

    @JvmStatic
    fun applyFeedback(entity: Entity, message: FeedbackData) {
        TouhouMaidCompatImpl.applyFeedback(entity, message)
    }

    @JvmStatic
    fun playMaidAnimation(entity: Entity, expression: String) {
        TouhouMaidCompatImpl.playMaidAnimation(entity, expression)
    }
}
