package rip.ysm.gui

import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.network.chat.Component
import net.minecraft.util.Util
import rip.ysm.gui.components.*
import rip.ysm.gui.components.groups.InfoGroup
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

open class ModernModelInfoScreen(
    parent: PlayerModelScreen,
    val renderContext: ModelAssembly
) : OptionScreen(Component.translatable("gui.yes_steve_model.model_info.title"), parent) {

    private val modelData: ServerModelInfo = renderContext.modelData
    private val avatarLocatables: MutableList<IResourceLocatable?> = ArrayList()

    init {
        resolveAvatarTextures()
    }

    private fun resolveAvatarTextures() {
        avatarLocatables.clear()
        val authors = modelData.metadata?.authors ?: emptyList()
        val avatars = renderContext.textureRegistry.authorAvatars
        for ((name) in authors) {
            val avatar = avatars[name]
            avatarLocatables.add(avatar?.let { UploadManager.getOrCreateLocatable(it, true) })
        }
    }

    override fun computePanelWidth(): Int = min(width - 40, 640)

    override fun computePanelHeight(): Int = min(height - 40, 380)

    override fun showTabs(): Boolean = false

    override fun registerGroups() {
        val meta = modelData.metadata ?: return
        val page = InfoGroup("page")
        val name = ModelMetadataPresenter.getLocalizedModelString(renderContext, "metadata.name", meta.name)
        if (name.isNotBlank()) {
            page.add(HeaderRow(name))
        }
        val license = meta.license
        if (license.first.isNotBlank()) {
            val licenseValue =
                if (license.second.isNotBlank()) "${license.first}  —  ${license.second}" else license.first
            page.add(LabelValueRow("gui.yes_steve_model.model_info.license", licenseValue))
        }
        val tips = ModelMetadataPresenter.getLocalizedModelString(renderContext, "metadata.tips", meta.tips)
        if (tips.isNotBlank()) {
            page.add(TipsRow(tips))
        }
        val links = meta.link
        if (links.isNotEmpty()) {
            for (i in 0 until links.size) {
                page.add(LinkRow(this, links.getKeyAt(i), links.getValueAt(i)))
            }
        }
        val authors = meta.authors
        for (i in authors.indices) {
            page.add(AuthorRow(this, authors[i], i, avatarLocatables[i]))
        }
        if (page.rows.isNotEmpty()) {
            groups.add(page)
        }
    }

    override fun init() {
        super.init()
        applyBtn.let { removeWidget(it); it.visible = false; it.active = false }
        undoBtn.let { removeWidget(it); it.visible = false; it.active = false }
        cancelBtn.let { removeWidget(it); it.visible = false }
        saveBtn.let {
            it.message = Component.translatable("gui.yes_steve_model.config.done")
            it.x = panelRight - it.width
        }
    }

    override fun onClose() {
        minecraft.setScreen(parentScreen)
    }

    override fun collectBlurRegions(out: MutableList<IntArray>) {
        out.add(intArrayOf(panelLeft, panelTop, panelRight - panelLeft, 18))
        val rowScroll = rowScrollDisplay.roundToInt()
        for (row in activeRows) {
            val y = row.y - rowScroll
            val yBot = y + row.height
            if (yBot <= rowAreaTop || y >= rowAreaBottom) {
                continue
            }
            val top = max(y, rowAreaTop)
            val bot = min(yBot, rowAreaBottom)
            out.add(intArrayOf(row.x, top, row.width, bot - top))
        }
        val btn = saveBtn
        if (btn.visible) {
            out.add(intArrayOf(btn.x, btn.y, btn.width, btn.height))
        }
    }

    fun openUrlWithConfirm(url: String) {
        if (url.isBlank()) return
        Minecraft.getInstance().setScreen(ConfirmLinkScreen({ confirmed ->
            if (confirmed) {
                runCatching {
                    Util.getPlatform().openUri(url)
                }
            }
            Minecraft.getInstance().setScreen(this)
        }, url, true))
    }
}