package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModVersion implements Function {
    @Override
    @Nullable
    public Object evaluate(@NotNull ExecutionContext<?> context, @NotNull Function.ArgumentCollection arguments) {
        String modid = arguments.getAsString(context, 0);
        if (modid == null) {
            return null;
        }
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modid);
        if (container.isEmpty()) {
            return null;
        }
        return container.get().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
