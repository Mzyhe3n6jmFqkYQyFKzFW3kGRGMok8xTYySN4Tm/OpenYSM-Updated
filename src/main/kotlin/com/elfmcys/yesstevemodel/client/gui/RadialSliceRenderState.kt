package com.elfmcys.yesstevemodel.client.gui

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiElementRenderState
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.util.Mth
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fc
import org.joml.Vector2f
import kotlin.math.max
import kotlin.math.min

data class RadialSliceRenderState(
    val pose: Matrix3x2fc,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val x3: Float,
    val y3: Float,
    val color: Int,
    val scissorArea: ScreenRectangle?,
    val bounds: ScreenRectangle?
) : GuiElementRenderState {

    override fun scissorArea(): ScreenRectangle? = scissorArea

    override fun bounds(): ScreenRectangle? = bounds

    override fun buildVertices(consumer: VertexConsumer) {
        consumer.addVertexWith2DPose(pose, x0, y0).setColor(color)
        consumer.addVertexWith2DPose(pose, x1, y1).setColor(color)
        consumer.addVertexWith2DPose(pose, x2, y2).setColor(color)
        consumer.addVertexWith2DPose(pose, x3, y3).setColor(color)
    }

    override fun pipeline(): RenderPipeline = RenderPipelines.GUI

    override fun textureSetup(): TextureSetup = TextureSetup.noTexture()

    companion object {
        fun of(
            currentPose: Matrix3x2fc,
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            x3: Float,
            y3: Float,
            color: Int,
            scissorArea: ScreenRectangle?
        ): RadialSliceRenderState {
            val snapshot = Matrix3x2f(currentPose)
            return RadialSliceRenderState(
                snapshot,
                x0,
                y0,
                x1,
                y1,
                x2,
                y2,
                x3,
                y3,
                color,
                scissorArea,
                computeBounds(snapshot, x0, y0, x1, y1, x2, y2, x3, y3, scissorArea)
            )
        }

        fun computeBounds(
            pose: Matrix3x2fc,
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            x3: Float,
            y3: Float,
            scissor: ScreenRectangle?
        ): ScreenRectangle {
            val v0 = pose.transformPosition(x0, y0, Vector2f())
            val v1 = pose.transformPosition(x1, y1, Vector2f())
            val v2 = pose.transformPosition(x2, y2, Vector2f())
            val v3 = pose.transformPosition(x3, y3, Vector2f())
            val minX = min(min(v0.x, v1.x), min(v2.x, v3.x))
            val maxX = max(max(v0.x, v1.x), max(v2.x, v3.x))
            val minY = min(min(v0.y, v1.y), min(v2.y, v3.y))
            val maxY = max(max(v0.y, v1.y), max(v2.y, v3.y))
            val bounds = ScreenRectangle(
                Mth.floor(minX),
                Mth.floor(minY),
                Mth.ceil(maxX - minX),
                Mth.ceil(maxY - minY)
            )
            return scissor?.let { bounds.intersection(it) } ?: bounds
        }
    }
}