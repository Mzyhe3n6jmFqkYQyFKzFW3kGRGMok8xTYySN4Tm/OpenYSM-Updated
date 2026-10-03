package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.Constants
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.event.OpenYsmMaidScreenEvent
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.event.YsmMaidClientTickEvent
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.EntityReference
import net.minecraft.world.entity.LivingEntity
import org.jetbrains.annotations.Nullable
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim.MaidAnimationStates
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui.MaidModelScreen
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.render.MaidGeoRenderer
import java.util.UUID

class MaidClientSetup {
    constructor() {
    }
    companion object {
        @JvmField var maidRenderer: MaidGeoRenderer = null
        @JvmStatic fun init() {
            EntityMaidRenderer.YSM_ENTITY_MAID_RENDERER = { context -> 
maidRenderer = MaidGeoRenderer()
return maidRenderer
 }
            MaidAnimationStates.register()
            registerTickHandler()
            registerScreenHandler()
            Constants.LOGGER.info("[YSM-TLM] 女仆渲染钩子已装载，动画状态 / tick / 模型选择屏均已接线")
        }
        @JvmStatic fun registerScreenHandler() {
            OpenYsmMaidScreenEvent.CALLBACK.register({ event -> 
Minecraft.getInstance().setScreen(MaidModelScreen(event.getMaid()))
 })
        }
        @JvmStatic fun registerTickHandler() {
            YsmMaidClientTickEvent.CALLBACK.register({ event -> 
if (localPlayer == null) { return }
if (localPlayer.getUUID().equals(ownerUuid)) { MaidRenderStore.getOrCreate(maid) }
 })
        }
        @JvmStatic fun getMaidRenderer(): MaidGeoRenderer {
            maidRenderer
        }
    }
}