package rip.ysm.compat.touhoulittlemaid.fabric

import com.elfmcys.yesstevemodel.network.message.FeedbackData
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidEventHandler
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidModelHandler

object TouhouMaidCompatImpl {
    private const val MOD_ID = "touhou_little_maid"
    private val IS_LOADED = FabricLoader.getInstance().isModLoaded(MOD_ID)

    @JvmStatic
    fun isLoaded(): Boolean = IS_LOADED

    @JvmStatic
    fun init() {
    }

    @JvmStatic
    fun isMaidEntity(entity: Entity): Boolean = isLoaded() && MaidEventHandler.isMaid(entity)

    @JvmStatic
    fun handleProjectileOwner(projectile: Projectile, entity: Entity) {
    }

    @JvmStatic
    fun registerAnimationRoulette(entity: Entity, classify: String, index: Int) {
        if (isLoaded()) {
            MaidModelHandler.activateRouletteAnimation(entity, classify, index)
        }
    }

    @JvmStatic
    fun applyFeedback(entity: Entity, message: FeedbackData) {
    }

    @Environment(EnvType.CLIENT)
    @JvmStatic
    fun playMaidAnimation(entity: Entity, expression: String) {
        if (isLoaded()) {
            MaidModelHandler.executeMaidMolang(entity, expression)
        }
    }
}
