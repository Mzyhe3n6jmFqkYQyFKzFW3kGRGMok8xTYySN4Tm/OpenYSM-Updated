package rip.ysm.compat.carryon

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.ModCompat
import rip.ysm.compat.carryon.fabric.CarryOnRendererImpl

object CarryOnRenderer : ModCompat("carryon") {
    @JvmStatic
    fun render(
        player: Player,
        poseStack: PoseStack,
        packedLight: Int,
        partialTick: Float,
        nodeCollector: SubmitNodeCollector
    ): Boolean {
        if (!isModLoaded) return false
        return CarryOnRendererImpl.render(player, poseStack, packedLight, partialTick, nodeCollector)
    }
}
