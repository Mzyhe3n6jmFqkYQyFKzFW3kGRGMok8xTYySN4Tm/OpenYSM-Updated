package rip.ysm.gpu

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object PieShader {
    private var program: Int = 0
    private var dummyVao: Int = 0
    private var locProj: Int = -1
    private var locRect: Int = -1
    private var locCenter: Int = -1
    private var locOuterRadius: Int = -1
    private var locInnerRadius: Int = -1
    private var locStartAngle: Int = -1
    private var locEndAngle: Int = -1
    private var locColor: Int = -1
    private var locFeather: Int = -1
    private var failed: Boolean = false

    @Synchronized
    @JvmStatic
    fun ensureCompiled(): Boolean {
        if (program != 0) return true
        if (failed) return false
        RenderSystem.assertOnRenderThread()
        return try {
            val vs = ShaderUtil.compileShaderFromResource(GL20.GL_VERTEX_SHADER, "/pie.vsh")
            val fs = ShaderUtil.compileShaderFromResource(GL20.GL_FRAGMENT_SHADER, "/pie.fsh")
            val prog = ShaderUtil.linkProgram(vs, fs)

            locProj = GL20.glGetUniformLocation(prog, "u_proj")
            locRect = GL20.glGetUniformLocation(prog, "u_rect")
            locCenter = GL20.glGetUniformLocation(prog, "u_center")
            locOuterRadius = GL20.glGetUniformLocation(prog, "u_outerRadius")
            locInnerRadius = GL20.glGetUniformLocation(prog, "u_innerRadius")
            locStartAngle = GL20.glGetUniformLocation(prog, "u_startAngle")
            locEndAngle = GL20.glGetUniformLocation(prog, "u_endAngle")
            locColor = GL20.glGetUniformLocation(prog, "u_color")
            locFeather = GL20.glGetUniformLocation(prog, "u_feather")

            dummyVao = GL30.glGenVertexArrays()
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
    fun dummyVao(): Int = dummyVao

    @JvmStatic
    fun locProj(): Int = locProj

    @JvmStatic
    fun locRect(): Int = locRect

    @JvmStatic
    fun locCenter(): Int = locCenter

    @JvmStatic
    fun locOuterRadius(): Int = locOuterRadius

    @JvmStatic
    fun locInnerRadius(): Int = locInnerRadius

    @JvmStatic
    fun locStartAngle(): Int = locStartAngle

    @JvmStatic
    fun locEndAngle(): Int = locEndAngle

    @JvmStatic
    fun locColor(): Int = locColor

    @JvmStatic
    fun locFeather(): Int = locFeather
}