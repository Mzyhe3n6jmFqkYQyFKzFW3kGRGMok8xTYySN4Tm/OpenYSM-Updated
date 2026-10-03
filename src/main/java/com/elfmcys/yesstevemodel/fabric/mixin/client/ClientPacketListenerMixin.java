package com.elfmcys.yesstevemodel.fabric.mixin.client;

import com.elfmcys.yesstevemodel.client.event.ClientPlayerCloneEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Unique private LocalPlayer ysm$oldPlayer;

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void ysm$onBeforeRespawn(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        this.ysm$oldPlayer = Minecraft.getInstance().player;
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void ysm$onAfterRespawn(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        LocalPlayer oldPlayer = this.ysm$oldPlayer;
        LocalPlayer newPlayer = Minecraft.getInstance().player;
        this.ysm$oldPlayer = null;

        if (oldPlayer != null && newPlayer != null) {
            ClientPlayerCloneEvent.onClientPlayerRespawn(oldPlayer, newPlayer);
        }
    }
}
