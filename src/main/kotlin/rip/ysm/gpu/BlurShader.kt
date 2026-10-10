package rip.ysm.gpu

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import com.mojang.blaze3d.opengl.DirectStateAccess
import com.mojang.blaze3d.opengl.GlDevice
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTextureView
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTextureView
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.*
import java.nio.ByteBuffer

object BlurShader {
    private var program: Int = 0
    private var dummyVao: Int = 0
    private var locProj: Int = -1
    private var locRect: Int = -1
    private var locScreenSize: Int = -1
    private var locRectSize: Int = -1
    private var locRadius: Int = -1
    private var locCorner: Int = -1
    private var locBlurRadius: Int = -1
    private var locGamma: Int = -1
    private var locTint: Int = -1
    private var locMode: Int = -1
    private var locPieCenter: Int = -1
    private var locPieInner: Int = -1
    private var locPieOuter: Int = -1
    private var locPieStart: Int = -1
    private var locPieEnd: Int = -1
    private var locPieFeather: Int = -1
    private var failed: Boolean = false

    private var captureTextureId: Int = 0
    private var captureWidth: Int = 0
    private var captureHeight: Int = 0
    private var lastCaptureFrame: Long = -1

    @Synchronized
    fun ensureCompiled(): Boolean {
        if (program != 0) return true
        if (failed) return false
        RenderSystem.assertOnRenderThread()
        return runCatching {
            val vs = ShaderUtil.compileShaderFromResource(GL20.GL_VERTEX_SHADER, "/blur.vsh")
            val fs = ShaderUtil.compileShaderFromResource(GL20.GL_FRAGMENT_SHADER, "/blur.fsh")
            val prog = ShaderUtil.linkProgram(vs, fs)

            locProj = GL20.glGetUniformLocation(prog, "u_proj")
            locRect = GL20.glGetUniformLocation(prog, "u_rect")
            locScreenSize = GL20.glGetUniformLocation(prog, "u_screenSize")
            locRectSize = GL20.glGetUniformLocation(prog, "u_rectSize")
            locRadius = GL20.glGetUniformLocation(prog, "u_radius")
            locCorner = GL20.glGetUniformLocation(prog, "u_corner")
            locBlurRadius = GL20.glGetUniformLocation(prog, "u_blurRadius")
            locGamma = GL20.glGetUniformLocation(prog, "u_gamma")
            locTint = GL20.glGetUniformLocation(prog, "u_tint")
            locMode = GL20.glGetUniformLocation(prog, "u_mode")
            locPieCenter = GL20.glGetUniformLocation(prog, "u_pieCenter")
            locPieInner = GL20.glGetUniformLocation(prog, "u_pieInner")
            locPieOuter = GL20.glGetUniformLocation(prog, "u_pieOuter")
            locPieStart = GL20.glGetUniformLocation(prog, "u_pieStart")
            locPieEnd = GL20.glGetUniformLocation(prog, "u_pieEnd")
            locPieFeather = GL20.glGetUniformLocation(prog, "u_pieFeather")

            val locScreen = GL20.glGetUniformLocation(prog, "u_screen")
            GL20.glUseProgram(prog)
            if (locScreen >= 0) {
                GL20.glUniform1i(locScreen, 0)
            }
            GL20.glUseProgram(0)

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

    fun locScreenSize(): Int = locScreenSize

    fun locRectSize(): Int = locRectSize

    fun locRadius(): Int = locRadius

    fun locCorner(): Int = locCorner

    fun locBlurRadius(): Int = locBlurRadius

    fun locGamma(): Int = locGamma

    fun locTint(): Int = locTint

    fun locMode(): Int = locMode

    fun locPieCenter(): Int = locPieCenter

    fun locPieInner(): Int = locPieInner

    fun locPieOuter(): Int = locPieOuter

    fun locPieStart(): Int = locPieStart

    fun locPieEnd(): Int = locPieEnd

    fun locPieFeather(): Int = locPieFeather

    fun captureTextureId(): Int = captureTextureId

    fun captureWidth(): Int = captureWidth

    fun captureHeight(): Int = captureHeight

    fun captureScreen(frameKey: Long) {
        if (frameKey == lastCaptureFrame && frameKey >= 0) return
        lastCaptureFrame = frameKey
        val main: RenderTarget = Minecraft.getInstance().mainRenderTarget
        val w = main.width
        val h = main.height
        ensureCaptureTexture(w, h)

        val colorView: GpuTextureView? = main.colorTextureView
        val depthView: GpuTextureView? = main.depthTextureView
        if (colorView !is GlTextureView || depthView !is GlTextureView) {
            return
        }
        val dsa: DirectStateAccess = (RenderSystem.getDevice() as GlDevice).directStateAccess()
        val mainFbo = colorView.getFbo(dsa, depthView.texture())

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFbo)
        GlStateManager._activeTexture(GL13.GL_TEXTURE0)
        GlStateManager._bindTexture(captureTextureId)
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, w, h)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFbo)
    }

    private fun ensureCaptureTexture(w: Int, h: Int) {
        if (captureTextureId != 0 && captureWidth == w && captureHeight == h) return
        if (captureTextureId != 0) {
            GL11.glDeleteTextures(captureTextureId)
        }
        captureTextureId = GL11.glGenTextures()
        GlStateManager._activeTexture(GL13.GL_TEXTURE0)
        GlStateManager._bindTexture(captureTextureId)

        var mipLevels = 1
        var mw = w
        var mh = h
        while (mw > 1 || mh > 1) {
            mw = maxOf(1, mw / 2)
            mh = maxOf(1, mh / 2)
            mipLevels++
        }

        var lw = w
        var lh = h
        for (i in 0 until mipLevels) {
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                i,
                GL11.GL_RGBA8,
                lw,
                lh,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                null as ByteBuffer?
            )
            lw = maxOf(1, lw / 2)
            lh = maxOf(1, lh / 2)
        }

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipLevels - 1)

        captureWidth = w
        captureHeight = h
    }
}