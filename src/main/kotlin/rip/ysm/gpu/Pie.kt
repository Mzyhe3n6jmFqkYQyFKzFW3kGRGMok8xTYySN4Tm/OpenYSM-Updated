@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package rip.ysm.gpu

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object Pie {
    const val TAU: Float = (Math.PI * 2.0).toFloat()

    @Suppress("MayBeConstant")
    @JvmField
    val tau: Float = TAU

    private val mvpScratch: Matrix4f = Matrix4f()
    private val mvpFloats: FloatArray = FloatArray(16)

    @JvmStatic
    fun draw(
        graphics: GuiGraphics,
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float,
        startAngle: Float,
        endAngle: Float,
        rgba: Int
    ) {
        draw(graphics, centerX, centerY, innerRadius, outerRadius, startAngle, endAngle, rgba, 1.0f)
    }

    @JvmStatic
    fun draw(
        graphics: GuiGraphics,
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float,
        startAngle: Float,
        endAngle: Float,
        rgba: Int,
        feather: Float
    ) {
        if (!PieShader.ensureCompiled()) return

        val pad = feather + 1.0f
        val rectX = centerX - outerRadius - pad
        val rectY = centerY - outerRadius - pad
        val rectW = (outerRadius + pad) * 2.0f
        val rectH = (outerRadius + pad) * 2.0f

        val sw = Minecraft.getInstance().window.guiScaledWidth
        val sh = Minecraft.getInstance().window.guiScaledHeight
        mvpScratch.identity().ortho(0.0f, sw.toFloat(), sh.toFloat(), 0.0f, 1000.0f, 21000.0f)
        mvpScratch.mul(RenderSystem.getModelViewMatrix())
        mvpScratch.mul(graphics.pose())
        mvpScratch.get(mvpFloats)

        val cr = (rgba shr 16 and 0xFF) / 255.0f
        val cg = (rgba shr 8 and 0xFF) / 255.0f
        val cb = (rgba and 0xFF) / 255.0f
        val ca = (rgba ushr 24 and 0xFF) / 255.0f

        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ONE_MINUS_SRC_ALPHA
        )
        GlStateManager._disableCull()
        GlStateManager._disableDepthTest()

        GlStateManager._glUseProgram(PieShader.program())

        if (PieShader.locProj() >= 0) GL20.glUniformMatrix4fv(PieShader.locProj(), false, mvpFloats)
        if (PieShader.locRect() >= 0) GL20.glUniform4f(PieShader.locRect(), rectX, rectY, rectW, rectH)
        if (PieShader.locCenter() >= 0) GL20.glUniform2f(PieShader.locCenter(), centerX, centerY)
        if (PieShader.locOuterRadius() >= 0) GL20.glUniform1f(PieShader.locOuterRadius(), outerRadius)
        if (PieShader.locInnerRadius() >= 0) GL20.glUniform1f(PieShader.locInnerRadius(), maxOf(0.0f, innerRadius))
        if (PieShader.locStartAngle() >= 0) GL20.glUniform1f(PieShader.locStartAngle(), startAngle)
        if (PieShader.locEndAngle() >= 0) GL20.glUniform1f(PieShader.locEndAngle(), endAngle)
        if (PieShader.locColor() >= 0) GL20.glUniform4f(PieShader.locColor(), cr, cg, cb, ca)
        if (PieShader.locFeather() >= 0) GL20.glUniform1f(PieShader.locFeather(), feather)

        GlStateManager._glBindVertexArray(PieShader.dummyVao())
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6)

        GlStateManager._glUseProgram(0)
        GlStateManager._glBindVertexArray(0)
        GlStateManager._disableBlend()
    }
}