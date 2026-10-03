package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.NativeLibLoader
import com.elfmcys.yesstevemodel.capability.AuthModelsCapability
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.button.*
import com.elfmcys.yesstevemodel.client.input.PlayerModelToggleKey
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.resource.models.Metadata
import com.elfmcys.yesstevemodel.resource.models.ModelPackData
import com.elfmcys.yesstevemodel.util.FileTypeUtil
import com.mojang.blaze3d.platform.InputConstants
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.sounds.SoundEvents
import org.apache.commons.lang3.StringUtils
import rip.ysm.api.PlatformAPI
import rip.ysm.gpu.GpuCapability
import rip.ysm.gui.ModernModelInfoScreen
import rip.ysm.gui.ModernPlayerTextureScreen
import java.util.*
import kotlin.math.roundToInt

open class PlayerModelScreen : Screen(Component.literal("YSM Player Model GUI")), IGuiWidget {
    private val hiddenModels: HashSet<String> = hashSetOf()
    private val modelPackMap: MutableMap<String, ModelPackData>
    private var filteredModels: MutableMap<String, ModelAssembly> = hashMapOf()
    private var filteredPacks: MutableMap<String, ModelPackData> = hashMapOf()
    private var sortedModelKeys: MutableList<String> = mutableListOf()
    private var sortedPackKeys: MutableList<String> = mutableListOf()

    var guiLeft: Int = 0
    var guiTop: Int = 0
    private var maxPage: Int = 0
    private var searchBox: EditBox? = null
    private var category: Category = Category.ALL

    init {
        if (NetworkHandler.isClientConnected()) {
            hiddenModels.addAll(ServerConfig.CLIENT_NOT_DISPLAY_MODELS.get())
        }
        ClientModelManager.registerGuiWidget(this)
        modelPackMap = Object2ReferenceOpenHashMap(ClientModelManager.getModelPackMap())
    }

    open fun createModelButton(
        x: Int,
        y: Int,
        isAuthLocked: Boolean,
        previewEntity: PlayerPreviewEntity,
        modelAssembly: ModelAssembly
    ): ModelButton {
        return ModelButton(x, y, isAuthLocked, previewEntity, modelAssembly)
    }

    open fun createTextureScreen(other: PlayerModelScreen, str: String, modelAssembly: ModelAssembly): Screen {
        if (GeneralConfig.TEXTURE_SCREEN_MODE.get() == GeneralConfig.TextureScreenMode.MODERN)
            return ModernPlayerTextureScreen(other, str, modelAssembly)
        return PlayerTextureScreen(other, str, modelAssembly)
    }

    open fun createModelInfoScreen(other: PlayerModelScreen, modelAssembly: ModelAssembly): Screen {
        if (GeneralConfig.MODEL_INFO_SCREEN_MODE.get() == GeneralConfig.ModelInfoScreenMode.MODERN)
            return ModernModelInfoScreen(other, modelAssembly)
        return ModelInfoScreen(other, modelAssembly)
    }

    private fun buildFilteredModelMap(): MutableMap<String, ModelAssembly> {
        val map = HashMap<String, ModelAssembly>()
        if (currentPath.isBlank()) {
            map.putAll(ClientModelManager.getModelAssemblyMap())
        }
        ClientModelManager.getModelAssemblyMap().forEach { (str, modelAssembly) ->
            if (str.startsWith(currentPath)) {
                map[str] = modelAssembly
            }
            val parentDir = FileTypeUtil.splitFileNameAndParentDir(str).right()
            if (StringUtils.isNotBlank(parentDir)) {
                ensurePackHierarchy(parentDir, modelPackMap)
            }
        }
        return map
    }

    private fun buildFilteredPackMap(): MutableMap<String, ModelPackData> {
        val map = HashMap<String, ModelPackData>()
        if (currentPath.isBlank()) {
            return HashMap(modelPackMap)
        }
        modelPackMap.forEach { (str, packData) ->
            if (str.startsWith(currentPath)) {
                map[str] = packData
            }
        }
        return map
    }

    private fun refreshModelList() {
        filteredModels = HashMap()
        filteredPacks = HashMap()
        val localPlayer = minecraft.player ?: return

        when (category) {
            Category.ALL -> {
                filteredModels = buildFilteredModelMap()
                filteredPacks = buildFilteredPackMap()
            }

            Category.AUTH -> {
                val authCap = AuthModelsCapability[localPlayer]
                if (authCap != null) {
                    for ((key, value) in ClientModelManager.getModelAssemblyMap()) {
                        if (authCap.containsModel(key) || !value.textureRegistry.isAuthModel) {
                            filteredModels[key] = value
                        }
                    }
                }
            }

            Category.STAR -> {
                val starCap = StarModelsCapability[localPlayer]
                if (starCap != null) {
                    for ((key, value) in ClientModelManager.getModelAssemblyMap()) {
                        if (starCap.containsModel(key)) {
                            filteredModels[key] = value
                        }
                    }
                }
            }
        }

        val searchLower = searchBox?.value?.lowercase(Locale.ENGLISH) ?: StringPool.EMPTY
        if (searchLower.isBlank()) {
            filteredModels.entries.removeIf { entry ->
                val pair = FileTypeUtil.splitFileNameAndParentDir(entry.key)
                hiddenModels.contains(pair.left()) || pair.right() != currentPath
            }
            filteredPacks.entries.removeIf { entry ->
                !isDirectChild(currentPath, entry.key)
            }
        } else {
            filteredModels.entries.removeIf { entry ->
                shouldFilterModel(FileTypeUtil.splitFileNameAndParentDir(entry.key).left(), entry.value, searchLower)
            }
            filteredPacks.entries.removeIf { entry ->
                shouldFilterPack(FileTypeUtil.splitFileNameAndParentDir(entry.key).left(), entry.value, searchLower)
            }
        }

        sortedModelKeys = filteredModels.keys.toMutableList().apply { sort() }
        sortedPackKeys = filteredPacks.keys.toMutableList().apply { sort() }
        maxPage = ((filteredModels.size + filteredPacks.size) - 1) / 10
    }

    private fun isDirectChild(str: String, str2: String): Boolean {
        if (str == str2) return false
        if (str.isNotBlank()) {
            if (!str2.startsWith(str)) return false
            val sub = str2.substring(str.length)
            val firstSlash = sub.indexOf('/')
            return firstSlash == sub.length - 1 && sub.lastIndexOf('/') == firstSlash
        }
        val firstSlash = str2.indexOf('/')
        return firstSlash == str2.length - 1 && str2.lastIndexOf('/') == firstSlash
    }

    private fun shouldFilterPack(str: String, packData: ModelPackData, search: String): Boolean {
        var query = search
        if (query.isBlank()) return false
        if (query.startsWith(TAG_SEARCH_PREFIX)) {
            query = query.substring(TAG_SEARCH_PREFIX.length)
        }
        if (str.lowercase(Locale.ENGLISH).contains(query)) {
            return false
        }
        if (packData.translations != null) {
            if (ModelMetadataPresenter.getLocalizedString(packData, "name", packData.name).lowercase(Locale.ENGLISH)
                    .contains(query)
            ) {
                return false
            }
            val desc = packData.description
            return desc == null || !ModelMetadataPresenter.getLocalizedString(packData, "description", desc)
                .lowercase(Locale.ENGLISH).contains(query)
        }
        return true
    }

    private fun shouldFilterModel(str: String, modelAssembly: ModelAssembly, search: String): Boolean {
        if (hiddenModels.contains(str)) return true
        if (search.isBlank()) return false
        if (search.startsWith(TAG_SEARCH_PREFIX)) return true
        if (search.startsWith(AUTHOR_SEARCH_PREFIX)) {
            val authorQuery = search.substring(AUTHOR_SEARCH_PREFIX.length)
            val metadata = modelAssembly.modelData.metadata ?: return true
            return matchesAuthorSearch(modelAssembly, authorQuery, metadata)
        }
        if (str.lowercase(Locale.ENGLISH).contains(search)) {
            return false
        }
        val metadata = modelAssembly.modelData.metadata ?: return true
        return !(ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "metadata.name", metadata.name)
            .lowercase(Locale.ENGLISH).contains(search) ||
                ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "metadata.tips", metadata.tips)
                    .lowercase(Locale.ENGLISH).contains(search)) && matchesAuthorSearch(modelAssembly, search, metadata)
    }

    private fun getParentPath(str: String?): String {
        if (str.isNullOrEmpty()) return StringPool.EMPTY
        val trimmed = if (str.endsWith("/")) str.substring(0, str.length - 1) else str
        val lastSlash = trimmed.lastIndexOf('/')
        if (lastSlash < 0) return StringPool.EMPTY
        return trimmed.substring(0, lastSlash + 1)
    }

    private fun matchesAuthorSearch(modelAssembly: ModelAssembly, query: String, metadata: Metadata): Boolean {
        for ((index, author) in metadata.authors.withIndex()) {
            if (ModelMetadataPresenter.getLocalizedModelString(
                    modelAssembly,
                    "metadata.authors.$index.name",
                    author.name
                ).lowercase(Locale.ENGLISH).contains(query)
            ) {
                return false
            }
        }
        return true
    }

    override fun init() {
        clearWidgets()
        refreshModelList()
        if (getCurrentPage() > maxPage) {
            resetCurrentPage()
        }
        guiLeft = (width - 420) / 2
        guiTop = (height - 235) / 2
        val prevSearchValue = searchBox?.value ?: StringPool.EMPTY
        val prevSearchFocused = searchBox?.isFocused ?: false

        val box = EditBox(font, guiLeft + 144, guiTop + 6, 140, 16, Component.literal("YSM Search Box"))
        box.value = prevSearchValue
        box.setTextColor(0xFFF3F0E0.toInt())
        box.isFocused = prevSearchFocused
        box.moveCursorToEnd(false)
        searchBox = box
        addWidget(box)

        addRenderableWidget(
            IconButton(guiLeft + 5, guiTop + 5, 20, 20, 80, 16) {
                val player = minecraft.player ?: return@IconButton
                val cap = PlayerCapability[player] ?: return@IconButton
                val modelAssembly = cap.getModelAssembly() ?: return@IconButton
                if (modelAssembly.modelData.metadata != null) {
                    minecraft.setScreen(createModelInfoScreen(this, modelAssembly))
                }
            }.apply { setTooltipText("gui.yes_steve_model.model.info") }
        )

        addRenderableWidget(
            IconButton(guiLeft + 28, guiTop + 5, 79, 20, 32, 16) {
                val player = minecraft.player ?: return@IconButton
                val cap = PlayerCapability[player] ?: return@IconButton
                val modelAssembly = cap.getModelAssembly() ?: return@IconButton
                minecraft.setScreen(createTextureScreen(this, cap.getModelId(), modelAssembly))
            }.apply { setTooltipText("gui.yes_steve_model.model.texture") }
        )

        addRenderableWidget(ModIconButton(guiLeft + 110, guiTop + 5))

        if (currentPath.isNotBlank()) {
            addRenderableWidget(
                IconButton(guiLeft + 110, guiTop + 27, 20, 20, 0, 32) {
                    navigateUp()
                }.apply { setTooltipText("gui.back") }
            )
        }

        addRenderableWidget(
            Checkbox.builder(Component.translatable("gui.yes_steve_model.show_model_id_first"), font)
                .pos(guiLeft + 5, guiTop - 22)
                .selected(GeneralConfig.SHOW_MODEL_ID_FIRST.get())
                .onValueChange { _, newValue ->
                    GeneralConfig.SHOW_MODEL_ID_FIRST.set(newValue)
                    GeneralConfig.SHOW_MODEL_ID_FIRST.save()
                }
                .build()
        )

        addRenderableWidget(
            IconButton(guiLeft + 328, guiTop + 5, 18, 18, 32, 0) {
                if (category != Category.ALL) {
                    category = Category.ALL
                    resetCurrentPage()
                    init()
                }
            }.apply { setTooltipText("gui.yes_steve_model.all_models") }
        )

        addRenderableWidget(
            IconButton(guiLeft + 308, guiTop + 5, 18, 18, 48, 0) {
                if (category != Category.AUTH) {
                    category = Category.AUTH
                    resetCurrentPage()
                    init()
                }
            }.apply { setTooltipText("gui.yes_steve_model.auth_models") }
        )

        addRenderableWidget(
            IconButton(guiLeft + 288, guiTop + 5, 18, 18, 0, 0) {
                if (category != Category.STAR) {
                    category = Category.STAR
                    resetCurrentPage()
                    init()
                }
            }.apply { setTooltipText("gui.yes_steve_model.star_models") }
        )

        addRenderableWidget(
            IconButton(guiLeft + 397, guiTop + 5, 18, 18, 16, 16) {
                minecraft.setScreen(ExtraPlayerConfigScreen(this))
            }.apply { setTooltipText("gui.yes_steve_model.config") }
        )

        val canUpload = ClientModelManager.isAllowUpload() && ClientModelManager.isOysmServer()
        val uploadButton = IconButton(guiLeft + 377, guiTop + 5, 18, 18, 0, 16) {
            minecraft.setScreen(ModelUploadScreen(this))
        }
        uploadButton.active = canUpload
        uploadButton.setTooltipLines(mutableListOf(Component.literal(if (canUpload) "Upload model to server" else "Server has uploads disabled, or this is not an OpenYSM server")))
        addRenderableWidget(uploadButton)

        addRenderableWidget(
            IconButton(guiLeft + 357, guiTop + 5, 18, 18, 80, 0) {
                minecraft.setScreen(OpenModelFolderScreen(this))
            }.apply { setTooltipText("gui.yes_steve_model.open_model_folder.open") }
        )

        addRenderableWidget(
            FlatColorButton(
                guiLeft + 198,
                guiTop + 215,
                52,
                14,
                Component.translatable("gui.yes_steve_model.pre_page")
            ) {
                val currentPage = getCurrentPage()
                if (currentPage > 0) {
                    setCurrentPage(currentPage - 1)
                    init()
                }
            }
        )

        addRenderableWidget(
            FlatColorButton(
                guiLeft + 308,
                guiTop + 215,
                52,
                14,
                Component.translatable("gui.yes_steve_model.next_page")
            ) {
                val currentPage = getCurrentPage()
                if (currentPage < maxPage) {
                    setCurrentPage(currentPage + 1)
                    init()
                }
            }
        )

        val player = minecraft.player ?: return
        val capability = AuthModelsCapability[player]
        for (i in 0 until 10) {
            val slotIndex = i + (getCurrentPage() * 10)
            val slotX = guiLeft + 143 + (55 * (i % 5))
            val slotY = guiTop + 28 + (93 * (i / 5))

            if (slotIndex < sortedPackKeys.size) {
                val str = sortedPackKeys[slotIndex]
                val packData = modelPackMap[str]
                if (packData != null) {
                    addRenderableWidget(
                        PackIconButton(slotX, slotY, 52, 90, packData) {
                            currentPath = str
                            resetCurrentPage()
                            init()
                        }
                    )
                }
            }

            val modelSlot = slotIndex - sortedPackKeys.size
            if (modelSlot in sortedModelKeys.indices) {
                val modelId = sortedModelKeys[modelSlot]
                val previewEntity = previewHolders[i]
                previewEntity.resetModel()
                val modelAssembly = filteredModels[modelId]
                if (modelAssembly != null) {
                    val isAuthLocked =
                        modelAssembly.textureRegistry.isAuthModel && (capability == null || !capability.containsModel(
                            modelId
                        ))
                    previewEntity.initModelWithTexture(modelId, modelAssembly.animationBundle.defaultTextureName)
                    previewEntity.getAnimationStateMachine()
                        .setCurrentAnimation(modelAssembly.modelData.modelProperties.previewAnimation)
                    addRenderableWidget(createModelButton(slotX, slotY, isAuthLocked, previewEntity, modelAssembly))
                }
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.fillGradient(guiLeft, guiTop, guiLeft + 135, guiTop + 235, -14540254, -14540254)
        guiGraphics.fillGradient(guiLeft + 138, guiTop, guiLeft + 420, guiTop + 235, -14540254, -14540254)
        guiGraphics.fillGradient(guiLeft + 351, guiTop + 7, guiLeft + 352, guiTop + 21, -790560, -790560)
        searchBox?.render(guiGraphics, mouseX, mouseY, partialTick)
        renderModelPreview(
            guiGraphics,
            mouseX,
            mouseY,
            minecraft.deltaTracker.getGameTimeDeltaPartialTick(false)
        )

        val box = searchBox
        if (box != null && box.value.isEmpty() && !box.isFocused) {
            guiGraphics.drawString(
                font,
                Component.translatable("gui.yes_steve_model.search").withStyle(ChatFormatting.ITALIC),
                guiLeft + 148,
                guiTop + 10,
                0xFF777777.toInt()
            )
        }

        val pageStr = "${getCurrentPage() + 1}/${maxPage + 1}"
        val pageX = guiLeft + 138 + ((282 - font.width(pageStr)) / 2)
        val pageY = guiTop + 223
        guiGraphics.drawString(font, pageStr, pageX, pageY - (9 / 2), 0xFFF3F0E0.toInt())

        var renderer =
            if (NativeLibLoader.isLoaded() && !GeneralConfig.USE_COMPATIBILITY_RENDERER.get()) "SIMD" else "Fallback"
        if (renderer == "SIMD" && GpuCapability.isAvailable() && GeneralConfig.USE_GPU_RENDERER.get()) {
            renderer = "GPU"
        }
        val versionStr = PlatformAPI.getModVersion(NameSpaces.MOD.invoke())
        guiGraphics.pose().pushMatrix()
        guiGraphics.pose().translate(0.0f, 0.0f)
        val darkGrayColor = ChatFormatting.DARK_GRAY.color ?: 0x555555
        guiGraphics.drawString(
            font,
            "$versionStr ($renderer)",
            guiLeft + 2,
            guiTop + 226,
            darkGrayColor or 0xFF000000.toInt()
        )
        guiGraphics.pose().popMatrix()

        if (currentPath.isNotBlank()) {
            val listSplit = font.split(Component.literal("📂 $currentPath").withStyle(ChatFormatting.GRAY), 270)
            for ((lineIndex, line) in listSplit.withIndex()) {
                guiGraphics.drawString(
                    font,
                    line,
                    guiLeft + 142,
                    guiTop + ((-(listSplit.size - lineIndex) * 10) - 2),
                    0xFFF3F0E0.toInt()
                )
            }
        }

        renderSyncStatus(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        renderables.forEach { renderable ->
            when (renderable) {
                is IconButton -> renderable.renderTooltip(guiGraphics, this, mouseX, mouseY)
                is ModelButton -> renderable.renderTooltip(guiGraphics, this, mouseX, mouseY)
                is PackIconButton -> renderable.renderDescription(guiGraphics, this, mouseX, mouseY)
            }
        }

        if (searchBox?.isHovered == true) {
            val tip = Component.translatable("gui.yes_steve_model.search.tip").withStyle(ChatFormatting.GRAY)
            guiGraphics.setTooltipForNextFrame(font, font.split(tip, 320), mouseX, mouseY)
        }
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }

    private fun renderSyncStatus(guiGraphics: GuiGraphics) {
        val currentState = ClientModelManager.getSyncStatus()
        val text = when (currentState.currentState) {
            ClientModelManager.SyncState.WAITING -> Component.translatable("gui.yes_steve_model.sync_hint.waiting")
            ClientModelManager.SyncState.LOADING -> Component.translatable("gui.yes_steve_model.sync_hint.loading")
            ClientModelManager.SyncState.PREPARING -> Component.translatable("gui.yes_steve_model.sync_hint.preparing")
            ClientModelManager.SyncState.SYNCING -> {
                if (currentState.syncedModels == 0) {
                    Component.translatable("gui.yes_steve_model.sync_hint.syncing")
                } else {
                    Component.literal("${currentState.syncedModels}/${currentState.totalModels}")
                }
            }

            else -> return
        }
        val textX = (guiLeft + 414) - font.width(text)
        val textY = guiTop + 215
        val darkGrayColor = ChatFormatting.DARK_GRAY.color ?: 0x555555
        guiGraphics.drawString(
            font,
            text,
            textX,
            textY + ((14 - 9) / 2.0f).roundToInt(),
            darkGrayColor or 0xFF000000.toInt()
        )
    }

    open fun renderModelPreview(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val localPlayer = minecraft.player ?: return
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            guiGraphics,
            guiLeft + 5,
            guiTop + 29,
            guiLeft + 130,
            guiTop + 200,
            70,
            0.0625F,
            mouseX.toFloat(),
            mouseY.toFloat(),
            localPlayer
        )
        val cap = PlayerCapability[localPlayer] ?: return
        val modelAssemblyOpt = ClientModelManager.getModelContext(cap.getModelId())
        val modelAssembly = if (modelAssemblyOpt.isPresent) modelAssemblyOpt.get() else null
        val displayName = if (modelAssembly != null && modelAssembly.modelData.metadata != null) {
            ModelMetadataPresenter.getLocalizedModelString(
                modelAssembly,
                "metadata.name",
                modelAssembly.modelData.metadata.name
            )
        } else {
            FileTypeUtil.getNameWithoutArchiveExtension(cap.getModelId())
        }
        val lines = font.split(
            FormattedText.of(displayName.ifBlank { FileTypeUtil.getNameWithoutArchiveExtension(cap.getModelId()) }),
            125
        )
        var lineY = guiTop + 205
        for (line in lines) {
            guiGraphics.drawString(font, line, guiLeft + ((135 - font.width(line)) / 2), lineY, 0xFFF3F0E0.toInt())
            lineY += 10
        }
    }

    override fun resize(width: Int, height: Int) {
        val prev = searchBox?.value ?: ""
        super.resize(width, height)
        searchBox?.value = prev
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val box = searchBox
        if (box != null && box.mouseClicked(event, doubleClick)) {
            focused = box
            return true
        }
        if (box != null && box.isFocused) {
            box.isFocused = false
        }
        var clicked = super.mouseClicked(event, doubleClick)
        if (!clicked && event.button() == 1 && currentPath.isNotBlank()) {
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            navigateUp()
            clicked = true
        }
        return clicked
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val box = searchBox ?: return false
        val prev = box.value
        if (box.charTyped(event)) {
            if (prev != box.value) {
                resetCurrentPage()
                init()
            }
            return true
        }
        return false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (handleToggleKey(event)) return true
        val hasNumeric = InputConstants.getKey(event).numericKeyValue.isPresent
        val box = searchBox
        val prev = box?.value ?: ""
        if (hasNumeric) return true
        if (box == null || !box.keyPressed(event)) {
            return (box != null && box.isFocused && box.isVisible && event.key() != 256) || super.keyPressed(event)
        }
        if (prev != box.value) {
            resetCurrentPage()
            init()
        }
        return true
    }

    private fun handleToggleKey(event: KeyEvent): Boolean {
        if (PlayerModelToggleKey.KEY_MAPPING.matches(event) && searchBox?.isFocused != true) {
            onClose()
            return true
        }
        return false
    }

    override fun insertText(text: String, overwrite: Boolean) {
        val box = searchBox ?: return
        if (overwrite) {
            box.value = text
        } else {
            box.insertText(text)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (scrollY != 0.0 && isInModelArea(mouseX, mouseY)) {
            handleScrollPage(scrollY)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun isInModelArea(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > (guiLeft + 143) && mouseX < (guiLeft + 430) && mouseY > (guiTop + 25) && mouseY < (guiTop + 235)
    }

    private fun navigateUp() {
        val parent = getParentPath(currentPath)
        if (currentPath != parent) {
            val old = currentPath
            currentPath = parent
            pageIndexMap.removeInt(old)
            init()
        }
    }

    private fun handleScrollPage(delta: Double) {
        val currentPage = getCurrentPage()
        if (delta > 0.0 && currentPage > 0) {
            setCurrentPage(currentPage - 1)
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            init()
            return
        }
        if (delta < 0.0 && currentPage < maxPage) {
            setCurrentPage(currentPage + 1)
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            init()
            return
        }
    }

    private fun getCurrentPage(): Int = pageIndexMap.getOrDefault(currentPath, 0)

    private fun setCurrentPage(page: Int) {
        pageIndexMap.put(currentPath, page)
    }

    private fun resetCurrentPage() {
        pageIndexMap.put(currentPath, 0)
    }

    override fun isPauseScreen(): Boolean = false

    override fun onModelsLoaded(map: MutableMap<String, ModelAssembly>) {
        init()
    }

    override fun onModelsUpdated(map: MutableMap<String, ModelAssembly>) {
        init()
    }

    private enum class Category {
        ALL,
        AUTH,
        STAR
    }

    companion object {
        const val AUTHOR_SEARCH_PREFIX: String = "@"
        const val TAG_SEARCH_PREFIX: String = "#"
        val previewHolders: Array<PlayerPreviewEntity> = Array(10) { PlayerPreviewEntity() }
        val pageIndexMap: Object2IntMap<String> = Object2IntOpenHashMap()
        var currentPath: String = StringPool.EMPTY

        @JvmStatic
        fun ensurePackHierarchy(str: String, map: MutableMap<String, ModelPackData>) {
            if (str.isBlank() || !str.contains("/")) return
            val split = str.split("/")
            val sb = StringBuilder()
            for (segment in split) {
                if (segment.isNotEmpty()) {
                    sb.append(segment).append("/")
                    val path = sb.toString()
                    map.putIfAbsent(
                        path,
                        ModelPackData(path, FileTypeUtil.getFinalPathSegment(path), StringPool.EMPTY, null, null)
                    )
                }
            }
        }
    }
}