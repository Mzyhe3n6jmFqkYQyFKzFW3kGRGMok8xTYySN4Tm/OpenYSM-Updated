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
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
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
import org.jetbrains.annotations.Nullable
import org.joml.Matrix4fStack
import org.joml.Quaternionf
import rip.ysm.gui.components.BooleanOptionRow
import rip.ysm.gui.components.RadioOptionRow
import rip.ysm.gui.components.SliderOptionRow
import rip.ysm.gui.components.groups.IdentifiedGroup
import rip.ysm.gui.molang.MolangOption
import java.util.ArrayList
import java.util.List

open class ModelSettingsScreen : OptionScreen() {
    var modelAssembly: ModelAssembly = null
    var animatable: AnimatableEntity<*> = null
    var initialGroupId: String = null
    var previewLeft: Int = 0
    var previewTop: Int = 0
    var previewRight: Int = 0
    var previewBottom: Int = 0
    var yaw: Float = 200.0f
    var pitch: Float = 0.0f
    var zoom: Float = 90.0f
    var offsetX: Float = 0.0f
    var offsetY: Float = 0.0f
    var draggingPreview: Boolean = false
    var draggingButton: Int = -1
    constructor(modelAssembly: ModelAssembly, animatable: AnimatableEntity<*>, parent: Screen, initialGroupId: String) {
        super(Component.translatable("gui.yes_steve_model.model_settings.title"), parent)
        this.modelAssembly = modelAssembly
        this.animatable = animatable
        this.initialGroupId = initialGroupId
    }
    open fun computePanelWidth(): Int {
        return Math.min(this.width - 40, 640)
    }
    open fun computePanelHeight(): Int {
        return Math.min(this.height - 40, 360)
    }
    open fun shouldUseCompactTabs(): Boolean {
        return this.width < 620
    }
    open fun computeRowAreaRight(): Int {
        return panelRight - previewWidth() - 4
    }
    open fun previewWidth(): Int {
        if (compactTabs) {
            var panelW: Int = panelRight - panelLeft
            return Mth.clamp(panelW / 3, 110, 180)
        }
        return 200
    }
    open fun init() {
        super.init()
        removeWidget(applyBtn)
        removeWidget(undoBtn)
        removeWidget(cancelBtn)
        applyBtn.visible = false
        undoBtn.visible = false
        cancelBtn.visible = false
        applyBtn.active = false
        undoBtn.active = false
        saveBtn.setMessage(Component.translatable("gui.yes_steve_model.config.done"))
        saveBtn.setX(panelRight - saveBtn.getWidth())
        previewLeft = panelRight - previewWidth()
        previewTop = rowAreaTop
        previewRight = panelRight
        previewBottom = panelBottom - 60
        if (initialGroupId != null) {
            for (g in groups) {
                if (g is IdentifiedGroup && initialGroupId.equals(ig.id)) {
                    selectGroup(g)
                    break
                }
            }
        }
    }
    open fun onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen)
        }
    }
    open fun collectBlurRegions(out: MutableList<IntArray>) {
        super.collectBlurRegions(out)
        out.add(intArrayOf(previewLeft, previewTop, previewRight - previewLeft, previewBottom - previewTop))
    }
    open fun registerGroups() {
        var ordered: MutableList<ExtraAnimationButtons> = ArrayList(modelAssembly.getModelData().getModelProperties().getExtraAnimationButtons().values())
        ordered.sort({ a, b -> a.getId().compareTo(b.getId()) })
        for (cfgGroup in ordered) {
            var g: IdentifiedGroup = IdentifiedGroup(cfgGroup.getId(), groupLabel(cfgGroup))
            var formIndex: Int = 0
            for (form in cfgGroup.getConfigForms()) {
                var row: OptionRow<*> = buildRow(cfgGroup.getId(), formIndex, form)
                if (row != null) {
                    g.add(row)
                }
                formIndex++
            }
            groups.add(g)
        }
    }
    open fun groupLabel(group: ExtraAnimationButtons): String {
        var fallback: String = if (group.getName() == null || group.getName().isEmpty()) group.getId() else group.getName()
        return ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "properties.extra_animation_buttons.%s.name".formatted(group.getId()), fallback)
    }
    open fun buildRow(groupId: String, formIndex: Int, form: AbstractConfig): OptionRow<*> {
        var title: String = ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "properties.extra_animation_buttons.%s.config_forms.%d.title".formatted(groupId, formIndex), form.getTitle())
        var desc: String = ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "properties.extra_animation_buttons.%s.config_forms.%d.description".formatted(groupId, formIndex), form.getDescription())
        if (form is CheckboxConfig) {
            return BooleanOptionRow(0, 0, 0, 22, MolangOption.ofBoolean(title, desc, animatable, cfg.getValue()))
        }
        if (form is RangeConfig) {
            return SliderOptionRow(0, 0, 0, 22, MolangOption.ofDouble(title, desc, animatable, cfg.getValue()), cfg.getMin(), cfg.getMax(), cfg.getStep(), "")
        }
        if (form is RadioConfig) {
            var labels: OrderedStringMap<String, String> = cfg.getLabels()
            var texts: MutableList<String> = ArrayList(labels.size())
            var writeExprs: Array<String> = arrayOfNulls<String>(labels.size())
            var i = 0
            while (i < labels.size()) {
                texts.add(ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "properties.extra_animation_buttons.%s.config_forms.%d.labels.%d".formatted(groupId, formIndex, i), labels.getKeyAt(i)))
                writeExprs[i] = labels.getValueAt(i)
                i++
            }
            return RadioOptionRow(0, 0, 0, 22, MolangOption.ofIndex(title, desc, animatable, cfg.getValue(), writeExprs), texts)
        }
        return null
    }
    open fun renderExtras(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(previewLeft, previewTop, previewRight, previewBottom, 0x66000000)
        renderPreview(g, partialTick)
    }
    open fun renderPreview(g: GuiGraphics, partialTick: Float) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return
        }
        if (!animatable is LivingAnimatable<*>) {
            return
        }
        var scale: Double = this.minecraft.getWindow().getGuiScale()
        var sx: Int = (previewLeft * scale as Int)
        var sy: Int = (this.minecraft.getWindow().getHeight() - previewBottom * scale as Int)
        var sw: Int = (previewRight - previewLeft * scale as Int)
        var sh: Int = (previewBottom - previewTop * scale as Int)
        RenderSystem.enableScissorForRenderTypeDraws(sx, sy, sw, sh)
        var cx: Float = previewLeft + previewRight / 2.0f + offsetX
        var cy: Float = previewTop + previewBottom - previewTop * 0.65f + offsetY
        renderPlayerForSettings(cx, cy, zoom, pitch, yaw, partialTick, la, RendererManager.getPlayerRenderer())
        RenderSystem.disableScissorForRenderTypeDraws()
    }
    open fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (isInPreview(event.x(), event.y())) {
            draggingPreview = true
            draggingButton = event.button()
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }
    open fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (draggingPreview && event.button() == draggingButton) {
            draggingPreview = false
            draggingButton = -1
            return true
        }
        return super.mouseReleased(event)
    }
    open fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        var button: Int = event.button()
        if (draggingPreview && button == draggingButton) {
            if (button == 0) {
                yaw = (yaw + dragX * 1.2 as Float)
                pitch = Mth.clamp((pitch - dragY * 0.8 as Float), -85.0f, 85.0f)
            } else {
                if (button == 1) {
                    offsetX = (offsetX + dragX as Float)
                    offsetY = (offsetY + dragY as Float)
                }
            }
            return true
        }
        return super.mouseDragged(event, dragX, dragY)
    }
    open fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        var delta: Double = scrollY
        if (isInPreview(mouseX, mouseY)) {
            zoom = Mth.clamp((zoom * 1.0 + delta * 0.1 as Float), 30.0f, 400.0f)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }
    open fun isInPreview(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= previewLeft && mouseX < previewRight && mouseY >= previewTop && mouseY < previewBottom
    }
    companion object {
        @JvmStatic fun renderPlayerForSettings(x: Float, y: Float, scale: Float, pitch: Float, yaw: Float, partialTick: Float, animatable: LivingAnimatable, renderer: GeoReplacedEntityRenderer) {
            ModelPreviewRenderer.setPreviewMode(true)
            var livingEntity: LivingEntity = (animatable.getEntity() as LivingEntity)
            var modelViewStack: Matrix4fStack = RenderSystem.getModelViewStack()
            modelViewStack.pushMatrix()
            modelViewStack.translate(x, y, 1250.0f)
            modelViewStack.scale(1.0f, 1.0f, -1.0f)
            var poseStack: PoseStack = PoseStack()
            poseStack.translate(0.0, 0.0, 1000.0)
            poseStack.scale(scale, scale, scale)
            poseStack.translate(0.0, 0.8, 0.0)
            var rotationZ: Quaternionf = Axis.ZP.rotationDegrees(180.0f)
            var rotationX: Quaternionf = Axis.XP.rotationDegrees(-10.0f + pitch)
            rotationZ.mul(rotationX)
            poseStack.mulPose(rotationZ)
            var oldBodyRot: Float = livingEntity.yBodyRot
            var oldBodyRotO: Float = livingEntity.yBodyRotO
            var oldYRot: Float = livingEntity.getYRot()
            var oldYRotO: Float = livingEntity.yRotO
            var oldXRot: Float = livingEntity.getXRot()
            var oldXRotO: Float = livingEntity.xRotO
            var oldHeadRot: Float = livingEntity.yHeadRot
            var oldHeadRotO: Float = livingEntity.yHeadRotO
            livingEntity.yBodyRot = -yaw
            livingEntity.yBodyRotO = -yaw
            livingEntity.setYRot(180.0f)
            livingEntity.yRotO = 180.0f
            livingEntity.setXRot(0.0f)
            livingEntity.xRotO = 0.0f
            livingEntity.yHeadRot = -yaw
            livingEntity.yHeadRotO = -yaw
            rotationX.conjugate()
            poseStack.mulPose(rotationX)
            var bufferSource: MultiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
            var state: AvatarRenderState = AvatarRenderState()
            try {
                renderer.renderEntity(animatable, state, 0.0f, partialTick, poseStack, bufferSource, 15728880)
                bufferSource.endBatch()
            } finally {
                livingEntity.yBodyRot = oldBodyRot
                livingEntity.yBodyRotO = oldBodyRotO
                livingEntity.setYRot(oldYRot)
                livingEntity.yRotO = oldYRotO
                livingEntity.setXRot(oldXRot)
                livingEntity.xRotO = oldXRotO
                livingEntity.yHeadRot = oldHeadRot
                livingEntity.yHeadRotO = oldHeadRotO
                modelViewStack.popMatrix()
                ModelPreviewRenderer.setPreviewMode(false)
            }
        }
    }
}