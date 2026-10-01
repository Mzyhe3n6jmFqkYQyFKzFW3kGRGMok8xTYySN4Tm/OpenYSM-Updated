package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.NameSpaces;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class IconButton extends FlatColorButton {

    private static final Identifier ICON_TEXTURE = NameSpaces.MOD.path("texture/icon.png");

    private final int iconU;

    private final int iconV;

    public IconButton(int x, int y, int width, int height, int iconU, int iconV, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress);
        this.iconU = iconU;
        this.iconV = iconV;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, getX() + ((this.width - 16) / 2), getY() + ((this.height - 16) / 2), this.iconU, this.iconV, 16, 16, 256, 256);
    }
}