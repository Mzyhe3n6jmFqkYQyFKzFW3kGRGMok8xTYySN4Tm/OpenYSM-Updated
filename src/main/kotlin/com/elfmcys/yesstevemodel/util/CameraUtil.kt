package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import rip.ysm.compat.oculus.OculusCompat

object CameraUtil {
    fun getCameraType(ctx: IContext<out Entity>): Int {
        if (ctx.entity == Minecraft.getInstance().player && ModelPreviewRenderer.isFirstPerson()) {
            return ctx.mc.options.cameraType.ordinal
        }
        return CameraType.THIRD_PERSON_FRONT.ordinal
    }

    fun isFirstPerson(animatableEntity: AnimatableEntity<out Entity>): Boolean {
        return animatableEntity.entity == Minecraft.getInstance().player &&
                ModelPreviewRenderer.isFirstPerson() &&
                !OculusCompat.isPBRActive() &&
                Minecraft.getInstance().options.cameraType == CameraType.FIRST_PERSON
    }

    fun isThirdPerson(ctx: IContext<out Entity>): Boolean {
        return isThirdPersonModel(ctx.geoInstance)
    }

    fun isThirdPersonModel(model: AnimatableEntity<*>): Boolean {
        return model is IPreviewAnimatable || ModelPreviewRenderer.isPreview
    }
}