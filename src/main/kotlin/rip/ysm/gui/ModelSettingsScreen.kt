package rip.ysm.gui

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons
import com.elfmcys.yesstevemodel.client.gui.custom.configs.CheckboxConfig
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RadioConfig
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RangeConfig
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.client.renderer.RendererManager
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import org.joml.Matrix4fStack
import org.joml.Quaternionf
import rip.ysm.gui.components.BooleanOptionRow
import rip.ysm.gui.components.RadioOptionRow
import rip.ysm.gui.components.SliderOptionRow
import rip.ysm.gui.components.groups.IdentifiedGroup
import rip.ysm.gui.molang.MolangOption
import kotlin.math.min

open class ModelSettingsScreen(
    private val modelAssembly: ModelAssembly,
    private val animatable: AnimatableEntity<*>,
    parent: Screen? = null,
    private val initialGroupId: String? = null
) : OptionScreen(Component.translatable("gui.yes_steve_model.model_settings.title"), parent) {

    private var previewLeft: Int = 0
    private var previewTop: Int = 0
    private var previewRight: Int = 0
    private var previewBottom: Int = 0
    private var yaw: Float = 200.0f
    private var pitch: Float = 0.0f
    private var zoom: Float = 90.0f
    private var offsetX: Float = 0.0f
    private var offsetY: Float = 0.0f
    private var draggingPreview: Boolean = false
    private var draggingButton: Int = -1

    override fun computePanelWidth(): Int = min(width - 40, 640)

    override fun computePanelHeight(): Int = min(height - 40, 360)

    override fun shouldUseCompactTabs(): Boolean = width < 620

    override fun computeRowAreaRight(): Int = panelRight - previewWidth() - 4

    private fun previewWidth(): Int {
        if (compactTabs) {
            val panelW = panelRight - panelLeft
            return Mth.clamp(panelW / 3, 110, 180)
        }
        return 200
    }

    override fun init() {
        super.init()
        applyBtn?.let { removeWidget(it); it.visible = false; it.active = false }
        undoBtn?.let { removeWidget(it); it.visible = false; it.active = false }
        cancelBtn?.let { removeWidget(it); it.visible = false }
        saveBtn?.let {
            it.message = Component.translatable("gui.yes_steve_model.config.done")
            it.x = panelRight - it.width
        }
        previewLeft = panelRight - previewWidth()
        previewTop = rowAreaTop
        previewRight = panelRight
        previewBottom = panelBottom - 60
        if (initialGroupId != null) {
            for (g in groups) {
                if (g is IdentifiedGroup && initialGroupId == g.id) {
                    selectGroup(g)
                    break
                }
            }
        }
    }

    override fun onClose() {
        minecraft.setScreen(parentScreen)
    }

    override fun collectBlurRegions(out: MutableList<IntArray>) {
        super.collectBlurRegions(out)
        out.add(intArrayOf(previewLeft, previewTop, previewRight - previewLeft, previewBottom - previewTop))
    }

    override fun registerGroups() {
        val ordered = ArrayList(modelAssembly.modelData.modelProperties.extraAnimationButtons.values)
        ordered.sortWith(compareBy { it.id })
        for (cfgGroup in ordered) {
            val g = IdentifiedGroup(cfgGroup.id, groupLabel(cfgGroup))
            var formIndex = 0
            for (form in cfgGroup.configForms) {
                val row = buildRow(cfgGroup.id, formIndex, form)
                if (row != null) {
                    g.add(row)
                }
                formIndex++
            }
            groups.add(g)
        }
    }

    private fun groupLabel(group: ExtraAnimationButtons): String {
        val fallback = if (group.name.isNullOrEmpty()) group.id else group.name
        return ModelMetadataPresenter.getLocalizedModelString(
            modelAssembly,
            "properties.extra_animation_buttons.${group.id}.name",
            fallback
        )
    }

    private fun buildRow(groupId: String, formIndex: Int, form: AbstractConfig): OptionRow<*>? {
        val title = ModelMetadataPresenter.getLocalizedModelString(
            modelAssembly,
            "properties.extra_animation_buttons.$groupId.config_forms.$formIndex.title",
            form.title
        )
        val desc = ModelMetadataPresenter.getLocalizedModelString(
            modelAssembly,
            "properties.extra_animation_buttons.$groupId.config_forms.$formIndex.description",
            form.description
        )
        if (form is CheckboxConfig) {
            return BooleanOptionRow(0, 0, 0, 22, MolangOption.ofBoolean(title, desc, animatable, form.value))
        }
        if (form is RangeConfig) {
            return SliderOptionRow(
                0,
                0,
                0,
                22,
                MolangOption.ofDouble(title, desc, animatable, form.value),
                form.min,
                form.max,
                form.step,
                ""
            )
        }
        if (form is RadioConfig) {
            val labels = form.labels
            val texts = ArrayList<String>(labels.size)
            val writeExprs = Array(labels.size) { "" }
            for (i in 0 until labels.size) {
                texts.add(
                    ModelMetadataPresenter.getLocalizedModelString(
                        modelAssembly,
                        "properties.extra_animation_buttons.$groupId.config_forms.$formIndex.labels.$i",
                        labels.getKeyAt(i)
                    )
                )
                writeExprs[i] = labels.getValueAt(i)
            }
            return RadioOptionRow(
                0,
                0,
                0,
                22,
                MolangOption.ofIndex(title, desc, animatable, form.value, writeExprs),
                texts
            )
        }
        return null
    }

    override fun renderExtras(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(previewLeft, previewTop, previewRight, previewBottom, 0x66000000)
        renderPreview(g, partialTick)
    }

    private fun renderPreview(g: GuiGraphics, partialTick: Float) {
        val mc = minecraft
        if (mc.player == null) return
        val la = animatable as? LivingAnimatable<*> ?: return
        val scale = mc.window.guiScale
        val sx = (previewLeft * scale).toInt()
        val sy = (mc.window.height - previewBottom * scale).toInt()
        val sw = ((previewRight - previewLeft) * scale).toInt()
        val sh = ((previewBottom - previewTop) * scale).toInt()
        RenderSystem.enableScissorForRenderTypeDraws(sx, sy, sw, sh)
        val cx = (previewLeft + previewRight) / 2.0f + offsetX
        val cy = previewTop + (previewBottom - previewTop) * 0.65f + offsetY
        renderPlayerForSettings(cx, cy, zoom, pitch, yaw, partialTick, la, RendererManager.getPlayerRenderer())
        RenderSystem.disableScissorForRenderTypeDraws()
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (isInPreview(event.x(), event.y())) {
            draggingPreview = true
            draggingButton = event.button()
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (draggingPreview && event.button() == draggingButton) {
            draggingPreview = false
            draggingButton = -1
            return true
        }
        return super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        val button = event.button()
        if (draggingPreview && button == draggingButton) {
            if (button == 0) {
                yaw = (yaw + dragX * 1.2).toFloat()
                pitch = Mth.clamp((pitch - dragY * 0.8).toFloat(), -85.0f, 85.0f)
            } else if (button == 1) {
                offsetX = (offsetX + dragX).toFloat()
                offsetY = (offsetY + dragY).toFloat()
            }
            return true
        }
        return super.mouseDragged(event, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val delta = scrollY
        if (isInPreview(mouseX, mouseY)) {
            zoom = Mth.clamp((zoom * (1.0 + delta * 0.1)).toFloat(), 30.0f, 400.0f)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun isInPreview(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= previewLeft && mouseX < previewRight && mouseY >= previewTop && mouseY < previewBottom

    companion object {
        @JvmStatic
        fun renderPlayerForSettings(
            x: Float,
            y: Float,
            scale: Float,
            pitch: Float,
            yaw: Float,
            partialTick: Float,
            animatable: LivingAnimatable<*>,
            renderer: GeoReplacedEntityRenderer<*, *, *>
        ) {
            ModelPreviewRenderer.setPreviewMode(true)
            val livingEntity = animatable.entity
            val modelViewStack: Matrix4fStack = RenderSystem.getModelViewStack()
            modelViewStack.pushMatrix()
            modelViewStack.translate(x, y, 1250.0f)
            modelViewStack.scale(1.0f, 1.0f, -1.0f)
            val poseStack = PoseStack()
            poseStack.translate(0.0, 0.0, 1000.0)
            poseStack.scale(scale, scale, scale)
            poseStack.translate(0.0, 0.8, 0.0)
            val rotationZ: Quaternionf = Axis.ZP.rotationDegrees(180.0f)
            val rotationX: Quaternionf = Axis.XP.rotationDegrees(-10.0f + pitch)
            rotationZ.mul(rotationX)
            poseStack.mulPose(rotationZ)
            val oldBodyRot = livingEntity.yBodyRot
            val oldBodyRotO = livingEntity.yBodyRotO
            val oldYRot = livingEntity.yRot
            val oldYRotO = livingEntity.yRotO
            val oldXRot = livingEntity.xRot
            val oldXRotO = livingEntity.xRotO
            val oldHeadRot = livingEntity.yHeadRot
            val oldHeadRotO = livingEntity.yHeadRotO
            livingEntity.yBodyRot = -yaw
            livingEntity.yBodyRotO = -yaw
            livingEntity.yRot = 180.0f
            livingEntity.yRotO = 180.0f
            livingEntity.xRot = 0.0f
            livingEntity.xRotO = 0.0f
            livingEntity.yHeadRot = -yaw
            livingEntity.yHeadRotO = -yaw
            rotationX.conjugate()
            poseStack.mulPose(rotationX)
            val bufferSource: MultiBufferSource.BufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
            val state = AvatarRenderState()
            try {
                @Suppress("UNCHECKED_CAST")
                (renderer as GeoReplacedEntityRenderer<Player, LivingAnimatable<Player>, AvatarRenderState>).renderEntity(
                    animatable as LivingAnimatable<Player>,
                    state,
                    0.0f,
                    partialTick,
                    poseStack,
                    bufferSource,
                    15728880
                )
                bufferSource.endBatch()
            } finally {
                livingEntity.yBodyRot = oldBodyRot
                livingEntity.yBodyRotO = oldBodyRotO
                livingEntity.yRot = oldYRot
                livingEntity.yRotO = oldYRotO
                livingEntity.xRot = oldXRot
                livingEntity.xRotO = oldXRotO
                livingEntity.yHeadRot = oldHeadRot
                livingEntity.yHeadRotO = oldHeadRotO
                modelViewStack.popMatrix()
                ModelPreviewRenderer.setPreviewMode(false)
            }
        }
    }
}