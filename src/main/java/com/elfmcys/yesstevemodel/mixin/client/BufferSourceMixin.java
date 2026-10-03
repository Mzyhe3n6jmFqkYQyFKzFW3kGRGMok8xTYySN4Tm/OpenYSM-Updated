package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.util.accessors.BufferSourceAccessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.SequencedMap;

@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceMixin implements BufferSourceAccessor {
    @Shadow
    @Final
    protected SequencedMap<RenderType, BufferBuilder> fixedBuffers;

    @Override
    @Unique
    public void ysm$initialize() {
        for (var renderType : fixedBuffers.keySet()) {
            ((MultiBufferSource.BufferSource) (Object) this).endBatch(renderType);
        }
    }
}