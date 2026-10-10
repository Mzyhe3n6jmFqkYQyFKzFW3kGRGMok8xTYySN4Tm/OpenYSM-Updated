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
    fun ensureCompiled(): Boolean {
        if (program != 0) return true
        if (failed) return false
        RenderSystem.assertOnRenderThread()
        return runCatching {
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
        }.getOrElse {
            ChatLogger.logFormatted("Failed to compile shader program, please check the log")
            Constants.LOGGER.error("Failed to compile shader program.", it)
            failed = true
            false
        }
    }

    fun program(): Int = program

    fun dummyVao(): Int = dummyVao

    fun locProj(): Int = locProj

    fun locRect(): Int = locRect

    fun locCenter(): Int = locCenter

    fun locOuterRadius(): Int = locOuterRadius

    fun locInnerRadius(): Int = locInnerRadius

    fun locStartAngle(): Int = locStartAngle

    fun locEndAngle(): Int = locEndAngle

    fun locColor(): Int = locColor

    fun locFeather(): Int = locFeather
}