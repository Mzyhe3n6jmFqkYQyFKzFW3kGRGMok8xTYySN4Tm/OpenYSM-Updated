@file:Suppress("MemberVisibilityCanBePrivate")

package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.Constants
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.event.OpenYsmMaidScreenEvent
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.event.YsmMaidClientTickEvent
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.TamableAnimal
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim.MaidAnimationStates
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui.MaidModelScreen
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.render.MaidGeoRenderer
import java.util.*

@Environment(EnvType.CLIENT)
object MaidClientSetup {
    var maidRenderer: MaidGeoRenderer? = null
        private set

    @JvmStatic
    fun init() {
        EntityMaidRenderer.YSM_ENTITY_MAID_RENDERER = { _ ->
            val renderer = MaidGeoRenderer()
            maidRenderer = renderer
            renderer
        }
        MaidAnimationStates.register()
        registerTickHandler()
        registerScreenHandler()
        Constants.LOGGER.info("[YSM-TLM] 女仆渲染钩子已装载，动画状态 / tick / 模型选择屏均已接线")
    }

    private fun registerScreenHandler() {
        OpenYsmMaidScreenEvent.CALLBACK.register { event ->
            Minecraft.getInstance().setScreen(MaidModelScreen(event.maid))
        }
    }

    private fun registerTickHandler() {
        YsmMaidClientTickEvent.CALLBACK.register { event ->
            val localPlayer = Minecraft.getInstance().player ?: return@register
            val maid: EntityMaid = event.maid
            val ownerRef = (maid as TamableAnimal).ownerReference
            val ownerUuid: UUID? = ownerRef?.uuid
            if (localPlayer.uuid == ownerUuid) {
                MaidRenderStore.getOrCreate(maid)
            }
        }
    }
}
