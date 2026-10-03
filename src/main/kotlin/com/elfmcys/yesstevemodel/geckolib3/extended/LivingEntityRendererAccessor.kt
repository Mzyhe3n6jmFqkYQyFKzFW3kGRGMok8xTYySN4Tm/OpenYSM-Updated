package com.elfmcys.yesstevemodel.geckolib3.extended

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.state.CameraRenderState

interface LivingEntityRendererAccessor {
    fun `tlm$renderNameTag`(
        state: LivingEntityRenderState,
        pPoseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraState: CameraRenderState
    )
}