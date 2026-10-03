package rip.ysm.gui

import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo
import com.elfmcys.yesstevemodel.resource.models.Metadata
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.elfmcys.yesstevemodel.util.data.StringPair
import net.minecraft.util.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.network.chat.Component
import java.net.URI
import org.apache.commons.lang3.StringUtils
import rip.ysm.gui.components.*
import rip.ysm.gui.components.buttons.FooterButton
import rip.ysm.gui.components.groups.InfoGroup
import java.util.ArrayList
import java.util.List
import java.util.Map

open class ModernModelInfoScreen : OptionScreen() {
    @JvmField var renderContext: ModelAssembly = null
    var modelData: ServerModelInfo = null
    val avatarLocatables: MutableList<IResourceLocatable> = ArrayList()
    constructor(parent: PlayerModelScreen, modelAssembly: ModelAssembly) {
        super(Component.translatable("gui.yes_steve_model.model_info.title"), parent)
        this.renderContext = modelAssembly
        this.modelData = modelAssembly.getModelData()
        resolveAvatarTextures()
    }
    open fun resolveAvatarTextures() {
        avatarLocatables.clear()
        var authors: MutableList<AuthorInfo> = modelData.getExtraInfo().getAuthors()
        var avatars: MutableMap<String, OuterFileTexture> = renderContext.getTextureRegistry().getAuthorAvatars()
        for (author in authors) {
            var avatar: OuterFileTexture = avatars.get(author.getName())
            avatarLocatables.add(if (avatar != null) UploadManager.getOrCreateLocatable(avatar, true) else null)
        }
    }
    open fun computePanelWidth(): Int {
        return Math.min(this.width - 40, 640)
    }
    open fun computePanelHeight(): Int {
        return Math.min(this.height - 40, 380)
    }
    open fun showTabs(): Boolean {
        return false
    }
    open fun registerGroups() {
        var meta: Metadata = modelData.getExtraInfo()
        if (meta == null) {
            return
        }
        var page: InfoGroup = InfoGroup("page")
        var name: String = ModelMetadataPresenter.getLocalizedModelString(renderContext, "metadata.name", meta.getName())
        if (StringUtils.isNotBlank(name)) {
            page.add(HeaderRow(name))
        }
        var license: StringPair = meta.getLicense()
        if (license != null && StringUtils.isNotBlank(license.getFirst())) {
            var licenseValue: String = if (StringUtils.isNotBlank(license.getSecond())) license.getFirst() + "  —  " + license.getSecond() else license.getFirst()
            page.add(LabelValueRow("gui.yes_steve_model.model_info.license", licenseValue))
        }
        var tips: String = ModelMetadataPresenter.getLocalizedModelString(renderContext, "metadata.tips", meta.getTips())
        if (StringUtils.isNotBlank(tips)) {
            page.add(TipsRow(tips))
        }
        var links: OrderedStringMap<String, String> = meta.getLink()
        if (links != null && !links.isEmpty()) {
            var i = 0
            while (i < links.size()) {
                page.add(LinkRow(this, links.getKeyAt(i), links.getValueAt(i)))
                i++
            }
        }
        var authors: MutableList<AuthorInfo> = meta.getAuthors()
        var i = 0
        while (i < authors.size()) {
            page.add(AuthorRow(this, authors.get(i), i, avatarLocatables.get(i)))
            i++
        }
        if (!page.getRows().isEmpty()) {
            groups.add(page)
        }
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
    }
    open fun onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen)
        }
    }
    open fun collectBlurRegions(out: MutableList<IntArray>) {
        out.add(intArrayOf(panelLeft, panelTop, panelRight - panelLeft, 18))
        var rowScroll: Int = Math.round(rowScrollDisplay)
        for (row in activeRows) {
            var y: Int = row.getY() - rowScroll
            var yBot: Int = y + row.getHeight()
            if (yBot <= rowAreaTop || y >= rowAreaBottom) {
                continue
            }
            var top: Int = Math.max(y, rowAreaTop)
            var bot: Int = Math.min(yBot, rowAreaBottom)
            out.add(intArrayOf(row.getX(), top, row.getWidth(), bot - top))
        }
        var btn: FooterButton = saveBtn
        if (btn != null && btn.visible) {
            out.add(intArrayOf(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight()))
        }
    }
    open fun openUrlWithConfirm(url: String) {
        if (StringUtils.isBlank(url)) {
            return
        }
        Minecraft.getInstance().setScreen(ConfirmLinkScreen({ confirmed -> if (confirmed) {  }
Minecraft.getInstance().setScreen(this) }, url, true))
    }
}