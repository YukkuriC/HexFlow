package io.yukkuric.hexflow.forge;

import at.petrak.hexcasting.common.lib.HexRegistries;
import io.yukkuric.hexflow.HexFlow;
import io.yukkuric.hexflow.actions.HexFlowPatterns;
import io.yukkuric.hexflow.actions.special.HexFlowSpecialHandlers;
import io.yukkuric.hexflow.vm.HexFlowFrames;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

@Mod(HexFlow.MOD_ID)
public final class HexFlowForge extends HexFlow.IAPI {
    public HexFlowForge(ModContainer modContainer) {
        var modBus = modContainer.getEventBus();
        modBus.addListener((RegisterEvent event) -> {
            var key = event.getRegistryKey();
            if (key.equals(HexRegistries.ACTION)) {
                HexFlowPatterns.registerActions();
            } else if (key.equals(HexRegistries.CONTINUATION_TYPE)) {
                HexFlowFrames.registerFrames();
            } else if (key.equals(HexRegistries.SPECIAL_HANDLER)) {
                HexFlowSpecialHandlers.registerSpecial();
            }
        });

        HexFlow.commonInit();
    }
    @Override
    public boolean modLoaded(@NotNull String id) {
        return ModList.get().isLoaded(id);
    }
}
