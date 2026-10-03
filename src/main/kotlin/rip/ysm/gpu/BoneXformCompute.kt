package rip.ysm.gpu

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL43

object BoneXformCompute {
    private var program: Int = 0
    private var locColor: Int = -1
    private var locOverlay: Int = -1
    private var locModelView: Int = -1
    private var failed: Boolean = false

    @Synchronized
    @JvmStatic
    fun ensureCompiled(): Boolean {
        if (program != 0) return true
        if (failed) return false
        RenderSystem.assertOnRenderThread()
        return try {
            val cs = ShaderUtil.compileShaderFromResource(GL43.GL_COMPUTE_SHADER, "/bone_xform.csh")
            val prog = ShaderUtil.linkProgram(cs)
            locColor = GL20.glGetUniformLocation(prog, "u_color")
            locOverlay = GL20.glGetUniformLocation(prog, "u_packedOverlay")
            locModelView = GL20.glGetUniformLocation(prog, "u_modelView")
            program = prog
            true
        } catch (t: Throwable) {
            ChatLogger.INSTANCE.logFormatted("Failed to compile shader program, please check the log")
            Constants.LOGGER.error("Failed to compile shader program.", t)
            failed = true
            false
        }
    }

    @JvmStatic
    fun program(): Int = program

    @JvmStatic
    fun locColor(): Int = locColor

    @JvmStatic
    fun locOverlay(): Int = locOverlay

    @JvmStatic
    fun locModelView(): Int = locModelView

    @JvmStatic
    fun dispatchGroupCount(vertexCount: Int): Int = (vertexCount + 64 - 1) / 64
}