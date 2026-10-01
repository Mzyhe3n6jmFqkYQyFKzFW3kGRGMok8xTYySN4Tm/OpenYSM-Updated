package rip.ysm.compat.carryon.fabric

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.world.entity.player.Player
import tschipp.carryon.client.render.CarriedObjectRender

object CarryOnRendererImpl {
    @JvmStatic
    fun render(
        player: Player,
        poseStack: PoseStack,
        packedLight: Int,
        partialTick: Float,
        nodeCollector: SubmitNodeCollector
    ): Boolean = CarriedObjectRender.draw(player, poseStack, packedLight, partialTick, nodeCollector, false)
}
