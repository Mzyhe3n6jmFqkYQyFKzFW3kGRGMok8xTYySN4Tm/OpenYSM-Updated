package com.elfmcys.yesstevemodel.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rip.ysm.api.client.event.ClientRawInputEvent;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private boolean ysm$hadScreen;

    @Inject(method = "onButton", at = @At("HEAD"))
    private void ysm$onButtonHead(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        this.ysm$hadScreen = (this.minecraft.screen != null || this.minecraft.getOverlay() != null);
    }

    @Inject(method = "onButton", at = @At("RETURN"), cancellable = true)
    private void ysm$onMouseButton(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle() && !this.ysm$hadScreen) {
            var result = ClientRawInputEvent.MOUSE_CLICKED_PRE.invoker().onMouseClick(this.minecraft, buttonInfo, action);
            if (result.isFalse()) ci.cancel();
        }
    }
}
