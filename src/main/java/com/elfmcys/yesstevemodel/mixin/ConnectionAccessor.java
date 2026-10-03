package com.elfmcys.yesstevemodel.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("unused")
@Mixin(Connection.class)
public interface ConnectionAccessor {
    @Accessor("channel")
    Channel ysm$getChannel();
}
