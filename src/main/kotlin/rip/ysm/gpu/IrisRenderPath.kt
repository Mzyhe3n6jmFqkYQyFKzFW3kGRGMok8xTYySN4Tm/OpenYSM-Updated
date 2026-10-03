package rip.ysm.gpu

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.Identifier

object IrisRenderPath {
    @JvmStatic
    fun tryRender(
        model: GeoModel,
        pose: PoseStack.Pose,
        boneParams: FloatArray,
        renderPartMask: Int,
        packedLight: Int,
        packedOverlay: Int,
        r: Float,
        g: Float,
        b: Float,
        a: Float,
        textureLocation: Identifier
    ): Boolean {
        return false
    }
}