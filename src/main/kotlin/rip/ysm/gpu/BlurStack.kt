@file:Suppress("unused")

package rip.ysm.gpu

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

object BlurStack {
    private val regions: MutableList<Region> = ArrayList()
    private val mvpScratch: Matrix4f = Matrix4f()
    private val mvpFloats: FloatArray = FloatArray(16)
    private var frameCounter: Long = 0L

    private class Region {
        var isPie: Boolean = false
        var x: Float = 0.0f
        var y: Float = 0.0f
        var w: Float = 0.0f
        var h: Float = 0.0f
        var cornerRadius: Float = 0.0f
        var pieCenterX: Float = 0.0f
        var pieCenterY: Float = 0.0f
        var pieInner: Float = 0.0f
        var pieOuter: Float = 0.0f
        var pieStart: Float = 0.0f
        var pieEnd: Float = 0.0f
        var blurRadius: Float = 0.0f
        var tintRgba: Int = 0
    }

    fun pushBlur(x: Float, y: Float, w: Float, h: Float, cornerRadius: Float, blurRadius: Float) {
        pushBlur(x, y, w, h, cornerRadius, blurRadius, -1)
    }

    fun pushBlur(x: Float, y: Float, w: Float, h: Float, cornerRadius: Float, blurRadius: Float, tintRgba: Int) {
        val r = Region().apply {
            isPie = false
            this.x = x
            this.y = y
            this.w = w
            this.h = h
            this.cornerRadius = cornerRadius
            this.blurRadius = blurRadius
            this.tintRgba = tintRgba
        }
        regions.add(r)
    }

    fun pushBlurPie(
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float,
        startAngle: Float,
        endAngle: Float,
        blurRadius: Float
    ) {
        pushBlurPie(centerX, centerY, innerRadius, outerRadius, startAngle, endAngle, blurRadius, -1)
    }

    fun pushBlurPie(
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float,
        startAngle: Float,
        endAngle: Float,
        blurRadius: Float,
        tintRgba: Int
    ) {
        val pad = 1.0f
        val r = Region().apply {
            isPie = true
            x = centerX - outerRadius - pad
            y = centerY - outerRadius - pad
            w = (outerRadius + pad) * 2.0f
            h = (outerRadius + pad) * 2.0f
            pieCenterX = centerX
            pieCenterY = centerY
            pieInner = innerRadius
            pieOuter = outerRadius
            pieStart = startAngle
            pieEnd = endAngle
            this.blurRadius = blurRadius
            this.tintRgba = tintRgba
        }
        regions.add(r)
    }

    fun popBlur() {
        if (regions.isNotEmpty()) {
            regions.removeAt(regions.size - 1)
        }
    }

    fun clear() {
        regions.clear()
    }

    fun isEmpty(): Boolean = regions.isEmpty()

    fun flush(graphics: GuiGraphics) {
        if (regions.isEmpty()) return
        if (!BlurShader.ensureCompiled()) {
            regions.clear()
            return
        }
        frameCounter++
        BlurShader.captureScreen(frameCounter)
        val sw = Minecraft.getInstance().window.guiScaledWidth
        val sh = Minecraft.getInstance().window.guiScaledHeight
        mvpScratch.identity().ortho(0.0f, sw.toFloat(), sh.toFloat(), 0.0f, 1000.0f, 21000.0f)
        mvpScratch.mul(RenderSystem.getModelViewMatrix())
        mvpScratch.mul(graphics.pose())
        mvpScratch.get(mvpFloats)
        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ONE_MINUS_SRC_ALPHA
        )
        GlStateManager._disableCull()
        GlStateManager._disableDepthTest()
        GlStateManager._activeTexture(GL13.GL_TEXTURE0)
        GlStateManager._bindTexture(BlurShader.captureTextureId())
        GlStateManager._glUseProgram(BlurShader.program())
        if (BlurShader.locProj() >= 0) {
            GL20.glUniformMatrix4fv(BlurShader.locProj(), false, mvpFloats)
        }
        if (BlurShader.locScreenSize() >= 0) {
            GL20.glUniform2f(
                BlurShader.locScreenSize(),
                BlurShader.captureWidth().toFloat(),
                BlurShader.captureHeight().toFloat()
            )
        }
        if (BlurShader.locGamma() >= 0) {
            GL20.glUniform1f(BlurShader.locGamma(), 6.0f)
        }
        GlStateManager._glBindVertexArray(BlurShader.dummyVao())
        for (r in regions) {
            val tr = ((r.tintRgba shr 16) and 0xFF) / 255.0f
            val tg = ((r.tintRgba shr 8) and 0xFF) / 255.0f
            val tb = (r.tintRgba and 0xFF) / 255.0f
            val ta = ((r.tintRgba ushr 24) and 0xFF) / 255.0f
            if (BlurShader.locRect() >= 0) {
                GL20.glUniform4f(BlurShader.locRect(), r.x, r.y, r.w, r.h)
            }
            if (BlurShader.locRectSize() >= 0) {
                GL20.glUniform2f(BlurShader.locRectSize(), r.w, r.h)
            }
            if (BlurShader.locBlurRadius() >= 0) {
                GL20.glUniform1f(BlurShader.locBlurRadius(), maxOf(1.0f, r.blurRadius))
            }
            if (BlurShader.locTint() >= 0) {
                GL20.glUniform4f(BlurShader.locTint(), tr, tg, tb, ta)
            }
            if (r.isPie) {
                if (BlurShader.locMode() >= 0) {
                    GL20.glUniform1i(BlurShader.locMode(), 1)
                }
                if (BlurShader.locPieCenter() >= 0) {
                    GL20.glUniform2f(BlurShader.locPieCenter(), r.pieCenterX, r.pieCenterY)
                }
                if (BlurShader.locPieInner() >= 0) {
                    GL20.glUniform1f(BlurShader.locPieInner(), r.pieInner)
                }
                if (BlurShader.locPieOuter() >= 0) {
                    GL20.glUniform1f(BlurShader.locPieOuter(), r.pieOuter)
                }
                if (BlurShader.locPieStart() >= 0) {
                    GL20.glUniform1f(BlurShader.locPieStart(), r.pieStart)
                }
                if (BlurShader.locPieEnd() >= 0) {
                    GL20.glUniform1f(BlurShader.locPieEnd(), r.pieEnd)
                }
                if (BlurShader.locPieFeather() >= 0) {
                    GL20.glUniform1f(BlurShader.locPieFeather(), 1.0f)
                }
            } else {
                if (BlurShader.locMode() >= 0) {
                    GL20.glUniform1i(BlurShader.locMode(), 0)
                }
                if (BlurShader.locRadius() >= 0) {
                    GL20.glUniform1f(BlurShader.locRadius(), r.cornerRadius)
                }
                if (BlurShader.locCorner() >= 0) {
                    GL20.glUniform4f(BlurShader.locCorner(), 1.0f, 1.0f, 1.0f, 1.0f)
                }
            }
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6)
        }
        GlStateManager._glUseProgram(0)
        GlStateManager._glBindVertexArray(0)
        GlStateManager._disableBlend()
        regions.clear()
    }
}