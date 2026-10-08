package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister
import com.elfmcys.yesstevemodel.client.input.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.network.chat.Component
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

@Environment(EnvType.CLIENT)
object ClientSetupEvent {
    init {
        registerKeyMappings()
        if (YesSteveModel.isAvailable) Constants.doNothing(AnimationRegister)
        ClientLifecycleEvents.CLIENT_STARTED.register {
            if (YesSteveModel.isAvailable) checkNativeInitialization()
        }
    }

    private fun registerKeyMappings() {
        KeyBindingHelper.registerKeyBinding(PlayerModelToggleKey.KEY_MAPPING)
        if (!YesSteveModel.isAvailable) return
        KeyBindingHelper.registerKeyBinding(AnimationRouletteKey.KEY_ROULETTE)
        KeyBindingHelper.registerKeyBinding(AnimationRouletteKey.KEY_LOCK)
        KeyBindingHelper.registerKeyBinding(DebugAnimationKey.KEY_MAPPING)
        KeyBindingHelper.registerKeyBinding(ExtraPlayerRenderKey.KEY_MAPPING)
        for (mapping in ExtraAnimationKey.keyMappings) KeyBindingHelper.registerKeyBinding(mapping)
    }

    private fun nativeClientInit(): Component? {
        return runCatching {
            val maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE)
            if (maxTexSize <= 0) {
                return Component.literal("YSM: OpenGL context not available")
            }
            val shaderResult = runCatching {
                val testShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
                if (testShader != 0) {
                    GL20.glDeleteShader(testShader)
                }
            }
            if (shaderResult.isFailure) {
                return Component.literal("YSM: GL20 (shaders) not available")
            }
            null
        }.getOrElse { e ->
            Component.literal("YSM Client Init Failed: ${e.message}")
        }
    }

    @JvmStatic
    fun checkNativeInitialization() {
        val component = nativeClientInit()
        if (component != null) {
            throw RuntimeException("YSM Client Initialization Failed: " + component.getString(256))
        }
    }
}
