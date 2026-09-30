package com.elfmcys.yesstevemodel.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rip.ysm.api.client.event.ClientRawInputEvent;
import rip.ysm.api.event.EventResult;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;
    @Unique private boolean ysm$hadScreen;

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void ysm$onKeyPressHead(long window, int action, KeyEvent event, CallbackInfo ci) {
        this.ysm$hadScreen = (this.minecraft.screen != null || this.minecraft.getOverlay() != null);
    }

    @Inject(method = "keyPress", at = @At("RETURN"), cancellable = true)
    private void ysm$onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle() && !this.ysm$hadScreen) {
            EventResult result = ClientRawInputEvent.KEY_PRESSED.invoker().onKey(this.minecraft, action, event);
            if (result != null && result.isFalse()) {
                ci.cancel();
            }
        }
    }
}
