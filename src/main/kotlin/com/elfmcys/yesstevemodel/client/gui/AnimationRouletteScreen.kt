package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.event.AnimationLockEvent
import com.elfmcys.yesstevemodel.client.gui.button.AnimationSlider
import com.elfmcys.yesstevemodel.client.gui.button.ConfigCheckBox
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton
import com.elfmcys.yesstevemodel.client.gui.button.FlatIconButton
import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons
import com.elfmcys.yesstevemodel.client.gui.custom.configs.CheckboxConfig
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RadioConfig
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RangeConfig
import com.elfmcys.yesstevemodel.client.input.AnimationRouletteKey
import com.elfmcys.yesstevemodel.client.input.ExtraAnimationKey
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket
import com.elfmcys.yesstevemodel.network.message.C2SRequestExecuteMolangPacket
import com.elfmcys.yesstevemodel.resource.models.ModelProperties
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.gui.ModelSettingsScreen
import java.util.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class AnimationRouletteScreen : Screen {
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var hoveredIndex: Int = -1
    private var hoveredConfigIndex: Int = -1
    private var currentConfigGroup: ExtraAnimationButtons? = null
    private var currentNavEntry: Pair<String, Int>
    private var configScrollOffset: Int = 0
    private var maxConfigScroll: Int = 0
    private var scrollUpButton: FlatColorButton? = null
    private var scrollDownButton: FlatColorButton? = null

    private val currentProperties: OrderedStringMap<String, String>
    private val renderGroups: Map<String, ExtraAnimationButtons>
    private val textProperties: Map<String, OrderedStringMap<String, String>>
    private val timingConfig: ModelProperties
    private val animatableModel: AnimatableEntity<*>
    private val renderContext: ModelAssembly

    constructor(
        renderGroups: Map<String, ExtraAnimationButtons>,
        textProperties: Map<String, OrderedStringMap<String, String>>,
        modelAssembly: ModelAssembly,
        animatableModel: AnimatableEntity<*>
    ) : super(Component.literal("Animation Roulette GUI")) {
        this.renderContext = modelAssembly
        this.timingConfig = modelAssembly.modelData.modelProperties
        this.animatableModel = animatableModel
        this.textProperties = textProperties
        this.renderGroups = renderGroups

        val lastNav = navigationStack.peekLast()
        if (lastNav != null && this.textProperties.containsKey(lastNav.left)) {
            this.currentProperties = this.textProperties[lastNav.left] ?: this.timingConfig.extraAnimation
            this.currentNavEntry = lastNav
            return
        }
        this.currentProperties = this.timingConfig.extraAnimation
        navigationStack.clear()
        val entry = MutablePair.of(StringPool.EMPTY, lastNav?.right ?: 0)
        navigationStack.add(entry)
        this.currentNavEntry = entry
    }

    constructor(
        modelId: String,
        modelAssembly: ModelAssembly,
        animatableModel: AnimatableEntity<*>
    ) : super(Component.literal("Animation Roulette GUI")) {
        this.renderContext = modelAssembly
        this.timingConfig = modelAssembly.modelData.modelProperties
        this.animatableModel = animatableModel
        this.textProperties = this.timingConfig.extraAnimationClassify
        this.renderGroups = this.timingConfig.extraAnimationButtons

        if (lastModelId != modelId) {
            navigationStack.clear()
            lastModelId = modelId
        }
        if (navigationStack.isEmpty()) {
            navigationStack.add(MutablePair.of(StringPool.EMPTY, 0))
        }
        val lastNav = navigationStack.peekLast()!!
        if (this.textProperties.containsKey(lastNav.left)) {
            this.currentProperties = this.textProperties[lastNav.left] ?: this.timingConfig.extraAnimation
            this.currentNavEntry = lastNav
            return
        }
        this.currentProperties = this.timingConfig.extraAnimation
        navigationStack.clear()
        val entry = MutablePair.of(StringPool.EMPTY, lastNav.right)
        navigationStack.add(entry)
        this.currentNavEntry = entry
    }

    override fun init() {
        clearWidgets()
        centerX = (width / 2) - 70
        centerY = (height / 2) - 8

        if (currentProperties.size < (currentNavEntry.right * 8) + 1) {
            currentNavEntry.setValue(0)
        }
        if (currentProperties.size <= hoveredIndex) {
            hoveredIndex = 0
        }

        if (animatableModel.entity is Player) {
            addRenderableWidget(object : FlatColorButton(centerX - 20, centerY - 10, 40, 20, Component.empty(), {
                AnimationLockEvent.toggleLock()
            }) {
                override fun getMessage(): Component {
                    return if (AnimationLockEvent.isLocked()) {
                        Component.translatable("gui.yes_steve_model.roulette.lock_on")
                    } else {
                        Component.translatable("gui.yes_steve_model.roulette.lock_off")
                    }
                }
            })
        } else {
            addRenderableWidget(
                FlatColorButton(
                    centerX - 20,
                    centerY - 10,
                    40,
                    20,
                    Component.translatable("gui.yes_steve_model.roulette.stop")
                ) {
                    val entity = animatableModel.entity
                    if (entity != null) {
                        NetworkHandler.sendToServer(C2SPlayAnimationPacket.createWithIndex(entity.id))
                    }
                    onClose()
                })
        }

        addRenderableWidget(FlatColorButton(centerX + 125, centerY - 102, 30, 30, Component.literal("<")) {
            previousPage()
        })
        addRenderableWidget(FlatColorButton(centerX + 240, centerY - 102, 30, 30, Component.literal(">")) {
            nextPage()
        })
        addRenderableWidget(
            FlatColorButton(
                centerX + 125,
                centerY - 70,
                145,
                22,
                Component.translatable("gui.yes_steve_model.model.return")
            ) {
                navigateBack()
            })

        val configGroup = currentConfigGroup
        if (configGroup != null) {
            val upBtn = FlatColorButton(centerX + 242, centerY - 46, 28, 60, Component.literal("↑")) {
                scrollConfigUp(50)
                if (configScrollOffset == 0) {
                    scrollUpButton?.active = false
                }
                scrollDownButton?.active = true
            }
            val downBtn = FlatColorButton(centerX + 242, centerY + 50, 28, 60, Component.literal("↓")) {
                scrollConfigDown(50)
                if (configScrollOffset == maxConfigScroll) {
                    scrollDownButton?.active = false
                }
                scrollUpButton?.active = true
            }
            scrollUpButton = upBtn
            scrollDownButton = downBtn
            addRenderableWidget(upBtn)
            addRenderableWidget(downBtn)

            val yOffset = intArrayOf(-46)
            val formIndex = intArrayOf(0)
            for (config in configGroup.configForms) {
                renderConfigFormItem(config, yOffset, formIndex)
            }
        }
    }

    private fun renderConfigFormItem(abstractConfig: AbstractConfig, yOffset: IntArray, formIndex: IntArray) {
        when (abstractConfig) {
            is CheckboxConfig -> {
                executeExpression(abstractConfig.value) { str ->
                    minecraft.execute {
                        addRenderableWidget(createCheckbox(abstractConfig, str, yOffset, formIndex))
                        yOffset[0] += 14
                        formIndex[0] += 1
                        maxConfigScroll = max(0, yOffset[0] - 110)
                    }
                }
            }

            is RangeConfig -> {
                executeExpression(abstractConfig.value) { str2 ->
                    minecraft.execute {
                        addRenderableWidget(createSlider(abstractConfig, str2, yOffset, formIndex))
                        yOffset[0] += 17
                        formIndex[0] += 1
                        maxConfigScroll = max(0, yOffset[0] - 110)
                    }
                }
            }

            is RadioConfig -> {
                executeExpression(abstractConfig.value) { str3 ->
                    minecraft.execute {
                        renderRadioGroup(abstractConfig, str3, yOffset, formIndex)
                    }
                }
            }
        }
    }

    private fun renderRadioGroup(radioConfig: RadioConfig, valueStr: String, yOffset: IntArray, formIndex: IntArray) {
        val group = currentConfigGroup ?: return
        var selectedIdx = parseFloatValue(valueStr).roundToInt()
        val labels = radioConfig.labels
        if (selectedIdx < 0 || labels.size <= selectedIdx) {
            selectedIdx = 0
        }
        var maxWidth = 0
        for ((idx, key) in labels.keys.withIndex()) {
            val labelText = ModelMetadataPresenter.getLocalizedModelString(
                renderContext,
                CONFIG_LABEL_FORMAT.format(group.id, formIndex[0], idx),
                key
            )
            maxWidth = max(maxWidth, font.width(labelText) + 16)
        }
        if (maxWidth == 0) {
            maxWidth = 115
        }
        val cols = max(1, 115 / maxWidth)
        val titleText = ModelMetadataPresenter.getLocalizedModelString(
            renderContext,
            CONFIG_TITLE_FORMAT.format(group.id, formIndex[0]),
            radioConfig.title
        )
        val descText = ModelMetadataPresenter.getLocalizedModelString(
            renderContext,
            CONFIG_DESC_FORMAT.format(group.id, formIndex[0]),
            radioConfig.description
        )
        val titleComponent = Component.literal(titleText)
        val tooltip = Tooltip.create(Component.literal(descText))
        val totalHeight = ((((labels.size - 1) / cols) + 1) * 14) + 14
        val iconButton = FlatIconButton(centerX + 125, centerY + yOffset[0], totalHeight, titleComponent)
        iconButton.setTooltip(tooltip)
        addRenderableOnly(iconButton)

        var rowY = yOffset[0] + 14
        var idx = 0
        while (idx < labels.size) {
            val labelKey = labels.getKeyAt(idx)
            val expr = labels.getValueAt(idx)
            val labelComp = Component.literal(
                ModelMetadataPresenter.getLocalizedModelString(
                    renderContext,
                    CONFIG_LABEL_FORMAT.format(group.id, formIndex[0], idx),
                    labelKey
                )
            )
            val isSelected = selectedIdx == idx
            val btnWidth = (110.0f / cols).roundToInt()
            val checkbox = ConfigCheckBox(
                centerX + 127 + (btnWidth * (idx % cols)),
                centerY + rowY,
                btnWidth,
                labelComp
            ) {
                executeExpression(expr, null)
                val entity = animatableModel.entity
                if (!GeckoLibCache.isRoamingVariableAssignment(expr) && NetworkHandler.isClientConnected() && !ServerConfig.LOW_BANDWIDTH_USAGE.get() && entity != null) {
                    NetworkHandler.sendToServer(C2SRequestExecuteMolangPacket(expr, entity.id))
                }
                init()
            }
            checkbox.isStateTriggered = isSelected
            addRenderableWidget(checkbox)
            if (idx % cols == cols - 1) {
                rowY += 14
            }
            idx++
        }
        yOffset[0] += totalHeight + 3
        formIndex[0] += 1
        maxConfigScroll = max(0, yOffset[0] - 110)
    }

    private fun createSlider(
        rangeConfig: RangeConfig,
        valueStr: String,
        yOffset: IntArray,
        formIndex: IntArray
    ): AnimationSlider {
        val group = currentConfigGroup!!
        val title = ModelMetadataPresenter.getLocalizedModelString(
            renderContext,
            CONFIG_TITLE_FORMAT.format(group.id, formIndex[0]),
            rangeConfig.title
        )
        val desc = ModelMetadataPresenter.getLocalizedModelString(
            renderContext,
            CONFIG_DESC_FORMAT.format(group.id, formIndex[0]),
            rangeConfig.description
        )
        val titleComponent = Component.literal(title)
        val tooltip = Tooltip.create(Component.literal(desc))
        val animationSlider = AnimationSlider(
            centerX + 125,
            centerY + yOffset[0],
            titleComponent,
            parseFloatValue(valueStr).toDouble(),
            animatableModel,
            rangeConfig.value,
            rangeConfig.step,
            rangeConfig.min,
            rangeConfig.max
        )
        animationSlider.setTooltip(tooltip)
        return animationSlider
    }

    private fun createCheckbox(
        checkboxConfig: CheckboxConfig,
        valueStr: String,
        yOffset: IntArray,
        formIndex: IntArray
    ): ConfigCheckBox {
        val group = currentConfigGroup!!
        val title = ModelMetadataPresenter.getLocalizedModelString(
            renderContext,
            CONFIG_TITLE_FORMAT.format(group.id, formIndex[0]),
            checkboxConfig.title
        )
        val desc = ModelMetadataPresenter.getLocalizedModelString(
            renderContext,
            CONFIG_DESC_FORMAT.format(group.id, formIndex[0]),
            checkboxConfig.description
        )
        val titleComponent = Component.literal(title)
        val tooltip = Tooltip.create(Component.literal(desc))
        val parsedValue = parseFloatValue(valueStr)
        val configCheckBox = object : ConfigCheckBox(
            centerX + 125,
            centerY + yOffset[0],
            titleComponent,
            Consumer { isChecked ->
                val expr = "${checkboxConfig.value}=${if (isChecked) "1" else "0"}"
                executeExpression(expr, null)
                val entity = animatableModel.entity
                if (!GeckoLibCache.isRoamingVariableAssignment(expr) && NetworkHandler.isClientConnected() && !ServerConfig.LOW_BANDWIDTH_USAGE.get() && entity != null) {
                    NetworkHandler.sendToServer(C2SRequestExecuteMolangPacket(expr, entity.id))
                }
            }
        ) {
            override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
                guiGraphics.fill(x, y, x + width, y + height, -280804798)
                super.renderContents(guiGraphics, mouseX, mouseY, partialTick)
            }
        }
        configCheckBox.isStateTriggered = parsedValue > 0.0f
        configCheckBox.setTooltip(tooltip)
        return configCheckBox
    }

    private fun parseFloatValue(str: String): Float {
        return when {
            str == "null" -> 0.0f
            NumberUtils.isParsable(str) -> str.toFloat()
            BooleanUtils.toBooleanObject(str) != null -> if (BooleanUtils.toBoolean(str)) 1.0f else 0.0f
            else -> 0.0f
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val pathNames = navigationStack.map { it.left }
        val pathStr = StringUtils.joinWith(" > ", *pathNames.toTypedArray())
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.yes_steve_model.roulette.path", pathStr),
            centerX + 195,
            centerY - 100,
            -1
        )
        renderRadialBackground(guiGraphics, mouseX, mouseY)
        renderRadialButtons(guiGraphics)
        renderPageInfo(guiGraphics)

        for (renderable in renderables) {
            if (renderable !is ISpecialWidget) {
                renderable.render(guiGraphics, mouseX, mouseY, partialTick)
            }
        }

        guiGraphics.enableScissor(0, centerY - 46, width, centerY + 110)
        val scrolledMouseY = if (mouseY < centerY - 46 || centerY + 110 < mouseY) {
            -1000
        } else {
            mouseY + configScrollOffset
        }
        guiGraphics.pose().pushMatrix()
        guiGraphics.pose().translate(0.0f, (-configScrollOffset).toFloat())
        for (renderable in renderables) {
            if (renderable is ISpecialWidget) {
                renderable.render(guiGraphics, mouseX, scrolledMouseY, partialTick)
            }
        }
        guiGraphics.pose().popMatrix()
        guiGraphics.disableScissor()
        renderHoverTooltip(guiGraphics, mouseX, scrolledMouseY)
    }

    private fun renderHoverTooltip(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (hoveredIndex in 0 until currentProperties.size) {
            val desc = ModelMetadataPresenter.getLocalizedModelString(
                renderContext,
                "properties.extra_animation.%s.desc".format(currentProperties.getKeyAt(hoveredIndex)),
                StringPool.EMPTY
            )
            if (desc.isNotBlank()) {
                guiGraphics.setTooltipForNextFrame(font, font.split(Component.literal(desc), 240), mouseX, mouseY)
            }
        }
    }

    private fun executeExpression(expr: String, consumer: Consumer<String>?) {
        runCatching {
            animatableModel.executeExpression(GeckoLibCache.parseSimpleExpression(expr), true, false, consumer)
        }.onFailure { e ->
            Constants.LOGGER.error(e.message, e)
        }
    }

    private fun renderPageInfo(guiGraphics: GuiGraphics) {
        guiGraphics.fill(centerX + 157, centerY - 87, centerX + 238, centerY - 72, -822083584)
        val pageStr = "${currentNavEntry.right + 1}/${((currentProperties.size - 1) / 8) + 1}"
        val color = ChatFormatting.AQUA.color ?: 0x55FFFF
        guiGraphics.drawCenteredString(font, pageStr, centerX + 197, centerY - 83, color or 0xFF000000.toInt())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (scrollY < 0.0) {
            if (mouseX < centerX + 110) {
                nextPage()
                return true
            }
            scrollConfigDown(20)
            return true
        }
        if (scrollY > 0.0) {
            if (mouseX < centerX + 110) {
                previousPage()
                return true
            }
            scrollConfigUp(20)
            return true
        }
        return false
    }

    private fun previousPage() {
        currentNavEntry.setValue(max(0, currentNavEntry.right - 1))
    }

    private fun nextPage() {
        if (currentProperties.size > (currentNavEntry.right + 1) * 8) {
            currentNavEntry.setValue(currentNavEntry.right + 1)
        }
    }

    private fun scrollConfigUp(amount: Int) {
        configScrollOffset = max(0, configScrollOffset - amount)
    }

    private fun scrollConfigDown(amount: Int) {
        configScrollOffset = min(maxConfigScroll, configScrollOffset + amount)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val button = event.button()
        if (hoveredIndex in 0 until currentProperties.size) {
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            val key = currentProperties.getKeyAt(hoveredIndex)
            if (key == RETURN_KEY) {
                navigateBack()
            } else if (key.startsWith(SUBMENU_PREFIX)) {
                navigateToSubmenu(key)
            } else {
                playAnimation(key)
            }
        } else if (hoveredConfigIndex in 0 until currentProperties.size) {
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            val value = currentProperties.getValueAt(hoveredConfigIndex)
            if (value.startsWith(SUBMENU_PREFIX)) {
                val groupKey = value.substring(SUBMENU_PREFIX.length)
                if (renderGroups.containsKey(groupKey)) {
                    if (GeneralConfig.ROULETTE_SETTINGS_MODE.get() == GeneralConfig.RouletteSettingsMode.CLASSIC) {
                        showConfigGroup(groupKey)
                    } else {
                        Minecraft.getInstance()
                            .setScreen(ModelSettingsScreen(renderContext, animatableModel, this, groupKey))
                    }
                }
            }
        }
        for (child in children()) {
            val scrolledEvent = if (child is ISpecialWidget) {
                MouseButtonEvent(event.x(), event.y() + configScrollOffset, event.buttonInfo())
            } else {
                event
            }
            if (child.mouseClicked(scrolledEvent, doubleClick)) {
                focused = child
                if (button == 0) {
                    isDragging = true
                }
                return true
            }
        }
        return false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (KeyMappingFactory.isActiveAndMatches(AnimationRouletteKey.KEY_ROULETTE, event)) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    private fun showConfigGroup(groupKey: String) {
        currentConfigGroup = renderGroups[groupKey]
        configScrollOffset = 0
        maxConfigScroll = 0
        init()
    }

    private fun playAnimation(animKey: String) {
        val localPlayer = Minecraft.getInstance().player
        if (NetworkHandler.isClientConnected()) {
            val lastNav = navigationStack.peekLast()
            val category = if (lastNav != null && lastNav.left.isNotBlank()) lastNav.left else StringPool.EMPTY
            val entity = animatableModel.entity
            if (entity is Player) {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket(hoveredIndex, category))
            } else if (entity != null) {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket(hoveredIndex, category, entity.id))
            }
        } else if (localPlayer != null) {
            PlayerCapability[localPlayer]?.requestModelSwitch(animKey)
        }
        if (localPlayer != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG.get()) {
            localPlayer.displayClientMessage(
                Component.translatable(
                    "message.yes_steve_model.model.animation_roulette.play",
                    animKey
                ), false
            )
        }
        Minecraft.getInstance().setScreen(null)
    }

    private fun navigateToSubmenu(key: String) {
        if (navigationStack.size > 5) {
            val localPlayer = Minecraft.getInstance().player
            localPlayer?.displayClientMessage(Component.translatable("gui.yes_steve_model.roulette.too_long"), false)
            return
        }
        val sub = key.substring(SUBMENU_PREFIX.length)
        if (textProperties[sub] != null) {
            navigationStack.addLast(MutablePair.of(sub, 0))
            Minecraft.getInstance()
                .setScreen(AnimationRouletteScreen(renderGroups, textProperties, renderContext, animatableModel))
        }
    }

    private fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeLast()
            Minecraft.getInstance()
                .setScreen(AnimationRouletteScreen(renderGroups, textProperties, renderContext, animatableModel))
            return
        }
        Minecraft.getInstance().setScreen(null)
    }

    override fun isPauseScreen(): Boolean = false

    private fun renderRadialButtons(guiGraphics: GuiGraphics) {
        var angle = 0.3926991f
        val remaining = currentProperties.size - (currentNavEntry.right * 8)
        val count = min(8, remaining)
        for (i in 0 until count) {
            val propIndex = i + (currentNavEntry.right * 8)
            val posX = (centerX + (65 * Mth.cos(angle.toDouble()))).toInt()
            val posY = centerY + (65 * Mth.sin(angle.toDouble()))
            val labelY = (posY - (9.0 / 2.0)).toInt()
            var value = currentProperties.getValueAt(propIndex)
            val isSubmenu = currentProperties.getKeyAt(propIndex).startsWith(SUBMENU_PREFIX)
            if (value.startsWith(SUBMENU_PREFIX)) {
                val groupKey = value.substring(SUBMENU_PREFIX.length)
                val group = renderGroups[groupKey]
                if (group != null) {
                    value = group.name
                    val gearX = (centerX + (35 * Mth.cos(angle.toDouble()))).toInt()
                    val gearY = centerY + (35 * Mth.sin(angle.toDouble()))
                    guiGraphics.drawCenteredString(
                        font,
                        Component.literal("⚙").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
                        gearX,
                        (gearY - (9.0 / 2.0)).toInt(),
                        -1
                    )
                }
            }
            if (value.isNotBlank()) {
                val label = ModelMetadataPresenter.getLocalizedModelString(
                    renderContext,
                    "properties.extra_animation.%s".format(currentProperties.getKeyAt(propIndex)),
                    value
                )
                renderWrappedLabel(guiGraphics, Component.literal(label), posX, labelY, isSubmenu)
            } else {
                val fallback = ModelMetadataPresenter.getLocalizedModelString(
                    renderContext,
                    "properties.extra_animation.%s".format(currentProperties.getKeyAt(propIndex)),
                    propIndex.toString()
                )
                guiGraphics.drawCenteredString(font, Component.literal(fallback), posX, labelY - 8, 0xFFF3F0E0.toInt())
            }
            if (currentNavEntry.right == 0 && navigationStack.size == 1) {
                renderKeyBindings(guiGraphics, propIndex, posX, labelY)
            }
            angle += 0.7853982f
        }
    }

    private fun renderKeyBindings(guiGraphics: GuiGraphics, slotIndex: Int, x: Int, y: Int) {
        val label = Component.literal("[ ").withStyle(ChatFormatting.YELLOW)
        val keyMapping: KeyMapping? = ExtraAnimationKey.KEY_MAPPINGS.getOrNull(slotIndex)
        if (keyMapping == null || keyMapping.isUnbound) {
            label.append(Component.translatable("key.yes_steve_model.extra_animation.none"))
        } else {
            label.append(keyMapping.translatedKeyMessage)
        }
        label.append(" ]")
        guiGraphics.drawCenteredString(font, label, x, y + 4, 0xFFF3F0E0.toInt())
    }

    private fun renderWrappedLabel(
        guiGraphics: GuiGraphics,
        component: MutableComponent,
        x: Int,
        y: Int,
        isSubmenu: Boolean
    ) {
        var styledComp = component
        if (isSubmenu) {
            styledComp = styledComp.withStyle(ChatFormatting.RED)
        }
        val lines = font.split(styledComp, 50)
        var lineY = (y - (lines.size * 9)) + 2
        if (currentNavEntry.right != 0 || navigationStack.size > 1) {
            lineY += 9
        }
        for (line in lines) {
            guiGraphics.drawCenteredString(font, line, x, lineY, 0xFFF3F0E0.toInt())
            lineY += 9
        }
    }

    private fun renderRadialBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (currentProperties.isEmpty()) return

        var pointerAngle = Mth.atan2((mouseY - centerY).toDouble(), (mouseX - centerX).toDouble()).toFloat()
        if (pointerAngle < 0.0f) {
            pointerAngle += 6.2831855f
        }
        val pointerRadius =
            Mth.sqrt(Mth.square((mouseY - centerY).toFloat()) + Mth.square((mouseX - centerX).toFloat()))
        var hoveredAny = false
        var hoveredConfig = false
        val sliceCount = min(8, currentProperties.size - (currentNavEntry.right * 8))
        for (i in 0 until sliceCount) {
            val startAngle = ((6.2831855f / 8) * i) + 0.034906585f
            val endAngle = ((6.2831855f / 8) * (i + 1)) - 0.034906585f
            val propIndex = i + (currentNavEntry.right * 8)
            val isSubmenu = currentProperties.getValueAt(propIndex).startsWith(SUBMENU_PREFIX)
            hoveredAny = checkRadialHover(
                guiGraphics,
                startAngle,
                pointerAngle,
                endAngle,
                pointerRadius,
                hoveredAny,
                isSubmenu,
                i
            )
            val isConfigSliceHovered =
                startAngle < pointerAngle && pointerAngle < endAngle && pointerRadius in 20.0f..50.0f
            if (isSubmenu) {
                if (isConfigSliceHovered) {
                    drawRadialSegment(guiGraphics, 15.0f, 50.0f, startAngle, endAngle, -268382465)
                    hoveredConfig = true
                    hoveredConfigIndex = propIndex
                } else {
                    drawRadialSegment(guiGraphics, 25.0f, 50.0f, startAngle, endAngle, 1879101183)
                }
            }
        }
        if (!hoveredAny) {
            hoveredIndex = -1
        }
        if (!hoveredConfig) {
            hoveredConfigIndex = -1
        }
    }

    private fun checkRadialHover(
        guiGraphics: GuiGraphics,
        startAngle: Float,
        pointerAngle: Float,
        endAngle: Float,
        pointerRadius: Float,
        alreadyHovered: Boolean,
        isSubmenu: Boolean,
        index: Int
    ): Boolean {
        val isHovered = startAngle < pointerAngle && pointerAngle < endAngle && pointerRadius in 50.0f..100.0f
        var hovered = alreadyHovered
        if (isHovered) {
            hovered = true
            hoveredIndex = index + (currentNavEntry.right * 8)
        }
        if (isHovered && index < currentProperties.size) {
            if (isSubmenu) {
                drawRadialSegment(guiGraphics, 50.0f, 115.0f, startAngle, endAngle, -251678464)
                drawRadialSegment(guiGraphics, 25.0f, 50.0f, startAngle, endAngle, -1879048192)
            } else {
                drawRadialSegment(guiGraphics, 25.0f, 115.0f, startAngle, endAngle, -251678464)
            }
        } else {
            drawRadialSegment(guiGraphics, 25.0f, 105.0f, startAngle, endAngle, -1879048192)
        }
        return hovered
    }

    private fun drawRadialSegment(
        guiGraphics: GuiGraphics,
        innerRadius: Float,
        outerRadius: Float,
        startAngle: Float,
        endAngle: Float,
        color: Int
    ) {
        val startCos = Mth.cos(startAngle.toDouble()).toFloat()
        val startSin = Mth.sin(startAngle.toDouble()).toFloat()
        val endCos = Mth.cos(endAngle.toDouble()).toFloat()
        val endSin = Mth.sin(endAngle.toDouble()).toFloat()
        val outerStartX = centerX + (outerRadius * startCos)
        val outerStartY = centerY + (outerRadius * startSin)
        val innerStartX = centerX + (innerRadius * startCos)
        val innerStartY = centerY + (innerRadius * startSin)
        val innerEndX = centerX + (innerRadius * endCos)
        val innerEndY = centerY + (innerRadius * endSin)
        val outerEndX = centerX + (outerRadius * endCos)
        val outerEndY = centerY + (outerRadius * endSin)
        guiGraphics.guiRenderState.submitGuiElement(
            RadialSliceRenderState.of(
                guiGraphics.pose(),
                outerStartX, outerStartY,
                innerStartX, innerStartY,
                innerEndX, innerEndY,
                outerEndX, outerEndY,
                color,
                null
            )
        )
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }

    companion object {
        const val SUBMENU_PREFIX: String = "#"
        const val RETURN_KEY: String = "#return"
        const val CONFIG_TITLE_FORMAT: String = "properties.extra_animation_buttons.%s.config_forms.%d.title"
        const val CONFIG_DESC_FORMAT: String = "properties.extra_animation_buttons.%s.config_forms.%d.description"
        const val CONFIG_LABEL_FORMAT: String = "properties.extra_animation_buttons.%s.config_forms.%d.labels.%d"
        val navigationStack: LinkedList<Pair<String, Int>> = LinkedList()
        var lastModelId: String = StringPool.EMPTY

        @JvmStatic
        fun setInitialSubmenu(str: String) {
            navigationStack.clear()
            navigationStack.addLast(MutablePair.of(StringPool.EMPTY, 0))
            navigationStack.addLast(MutablePair.of(str, 0))
        }
    }
}