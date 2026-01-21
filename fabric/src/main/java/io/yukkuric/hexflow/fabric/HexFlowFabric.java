package io.yukkuric.hexflow.fabric;

import io.yukkuric.hexflow.HexFlow;
import io.yukkuric.hexflow.actions.HexFlowPatterns;
import io.yukkuric.hexflow.actions.special.HexFlowSpecialHandlers;
import io.yukkuric.hexflow.vm.HexFlowFrames;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public final class HexFlowFabric extends HexFlow.IAPI implements ModInitializer {
    @Override
    public void onInitialize() {
        HexFlowPatterns.registerActions();
        HexFlowSpecialHandlers.registerSpecial();
        HexFlowFrames.registerFrames();
    }
    @Override
    public boolean modLoaded(@NotNull String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
