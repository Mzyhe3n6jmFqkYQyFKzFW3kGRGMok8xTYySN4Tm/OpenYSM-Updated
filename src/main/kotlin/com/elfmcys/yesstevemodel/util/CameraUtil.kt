package com.elfmcys.yesstevemodel.util

import rip.ysm.compat.oculus.OculusCompat
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity

object CameraUtil {
    @JvmStatic
    fun getCameraType(ctx: IContext<out Entity>): Int {
        if (ctx.entity() == Minecraft.getInstance().player && ModelPreviewRenderer.isFirstPerson()) {
            return ctx.mc().options.cameraType.ordinal
        }
        return CameraType.THIRD_PERSON_FRONT.ordinal
    }

    @JvmStatic
    fun isFirstPerson(animatableEntity: AnimatableEntity<out Entity>): Boolean {
        return animatableEntity.entity == Minecraft.getInstance().player &&
                ModelPreviewRenderer.isFirstPerson() &&
                !OculusCompat.isPBRActive() &&
                Minecraft.getInstance().options.cameraType == CameraType.FIRST_PERSON
    }

    @JvmStatic
    fun isThirdPerson(ctx: IContext<out Entity>): Boolean {
        return isThirdPersonModel(ctx.geoInstance())
    }

    @JvmStatic
    fun isThirdPersonModel(model: AnimatableEntity<*>): Boolean {
        return model is IPreviewAnimatable || ModelPreviewRenderer.isPreview()
    }
}