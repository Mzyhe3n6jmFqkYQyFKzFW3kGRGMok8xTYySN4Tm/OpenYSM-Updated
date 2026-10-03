package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.client.gui.PauseScreenButtonBuilder;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    public PauseScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void init(CallbackInfo callbackInfo) {
        var buttons = PauseScreenButtonBuilder.createButtons((PauseScreen) (Object) this);
        if (buttons != null && !buttons.isEmpty()) {
            for (var button : buttons) {
                addRenderableWidget(button);
            }
        }
    }
}