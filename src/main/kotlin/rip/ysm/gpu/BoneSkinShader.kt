package rip.ysm.gpu

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL31
import org.lwjgl.opengl.GL43

object BoneSkinShader {
    const val SSBO: Int = 0
    const val LIGHT_UBO_BINDING: Int = 1
    const val PROJ_UBO_BINDING: Int = 2

    private var program: Int = 0
    private var locModelView: Int = -1
    private var locColor: Int = -1
    private var locOverlay: Int = -1
    private var locFogStart: Int = -1
    private var locFogEnd: Int = -1
    private var locFogColor: Int = -1
    private var locFogShape: Int = -1
    private var locAlphaMode: Int = -1
    private var failed: Boolean = false

    @Synchronized
    @JvmStatic
    fun ensureCompiled(): Boolean {
        if (program != 0) return true
        if (failed) return false
        RenderSystem.assertOnRenderThread()
        return runCatching {
            val vs = ShaderUtil.compileShaderFromResource(GL20.GL_VERTEX_SHADER, "/bone_skin.vsh")
            val fs = ShaderUtil.compileShaderFromResource(GL20.GL_FRAGMENT_SHADER, "/bone_skin.fsh")
            val prog = ShaderUtil.linkProgramWith({ p ->
                GL20.glBindAttribLocation(p, 0, "a_position")
                GL20.glBindAttribLocation(p, 1, "a_uv")
                GL20.glBindAttribLocation(p, 2, "a_normal")
                GL20.glBindAttribLocation(p, 3, "a_boneId")
                GL20.glBindAttribLocation(p, 4, "a_cullable")
            }, vs, fs)

            val ssboBlock = GL43.glGetProgramResourceIndex(prog, GL43.GL_SHADER_STORAGE_BLOCK, "BoneBlock")
            if (ssboBlock != GL43.GL_INVALID_INDEX) {
                GL43.glShaderStorageBlockBinding(prog, ssboBlock, SSBO)
            }

            locModelView = GL20.glGetUniformLocation(prog, "u_modelView")
            locColor = GL20.glGetUniformLocation(prog, "u_color")
            locOverlay = GL20.glGetUniformLocation(prog, "u_packedOverlay")
            locFogStart = GL20.glGetUniformLocation(prog, "u_fogStart")
            locFogEnd = GL20.glGetUniformLocation(prog, "u_fogEnd")
            locFogColor = GL20.glGetUniformLocation(prog, "u_fogColor")
            locFogShape = GL20.glGetUniformLocation(prog, "u_fogShape")
            locAlphaMode = GL20.glGetUniformLocation(prog, "u_alphaMode")

            val lightBlockIdx = GL31.glGetUniformBlockIndex(prog, "LightingBlock")
            if (lightBlockIdx != GL31.GL_INVALID_INDEX) {
                GL31.glUniformBlockBinding(prog, lightBlockIdx, LIGHT_UBO_BINDING)
            }

            val projBlockIdx = GL31.glGetUniformBlockIndex(prog, "ProjectionBlock")
            if (projBlockIdx != GL31.GL_INVALID_INDEX) {
                GL31.glUniformBlockBinding(prog, projBlockIdx, PROJ_UBO_BINDING)
            }

            val locSampler0 = GL20.glGetUniformLocation(prog, "Sampler0")
            val locSampler1 = GL20.glGetUniformLocation(prog, "Sampler1")
            val locSampler2 = GL20.glGetUniformLocation(prog, "Sampler2")
            GL20.glUseProgram(prog)
            if (locSampler0 >= 0) {
                GL20.glUniform1i(locSampler0, 0)
            }
            if (locSampler1 >= 0) {
                GL20.glUniform1i(locSampler1, 1)
            }
            if (locSampler2 >= 0) {
                GL20.glUniform1i(locSampler2, 2)
            }
            GL20.glUseProgram(0)

            program = prog
            true
        }.getOrElse {
            ChatLogger.logFormatted("Failed to compile shader program, please check the log")
            Constants.LOGGER.error("Failed to compile shader program.", it)
            failed = true
            false
        }
    }

    @JvmStatic
    fun program(): Int = program

    @JvmStatic
    fun locModelView(): Int = locModelView

    @JvmStatic
    fun locColor(): Int = locColor

    @JvmStatic
    fun locOverlay(): Int = locOverlay

    @JvmStatic
    fun locFogStart(): Int = locFogStart

    @JvmStatic
    fun locFogEnd(): Int = locFogEnd

    @JvmStatic
    fun locFogColor(): Int = locFogColor

    @JvmStatic
    fun locFogShape(): Int = locFogShape

    @JvmStatic
    fun locAlphaMode(): Int = locAlphaMode
}