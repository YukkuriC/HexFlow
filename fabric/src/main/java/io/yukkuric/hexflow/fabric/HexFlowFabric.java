package io.yukkuric.hexflow.fabric;

import io.yukkuric.hexflow.actions.HexFlowPatterns;
import io.yukkuric.hexflow.actions.special.HexFlowSpecialHandlers;
import io.yukkuric.hexflow.vm.HexFlowFrames;
import net.fabricmc.api.ModInitializer;

public final class HexFlowFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        HexFlowPatterns.registerActions();
        HexFlowSpecialHandlers.registerSpecial();
        HexFlowFrames.registerFrames();
    }
}
