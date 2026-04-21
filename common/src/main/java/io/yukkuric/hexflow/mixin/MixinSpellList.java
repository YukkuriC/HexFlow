package io.yukkuric.hexflow.mixin;

import at.petrak.hexcasting.api.casting.SpellList;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.yukkuric.hexflow.mixin_interface.MutableSpellList;
import org.spongepowered.asm.mixin.*;

import java.util.List;

public interface MixinSpellList {
    @Mixin(SpellList.LList.class)
    class LList implements MutableSpellList {
        @Final
        @Shadow
        private List<Iota> list;
        @Final
        @Shadow
        private int idx;
        @Override
        public void set(int idx, Iota data) {
            this.list.set(idx + this.idx, data);
        }
    }
    @Mixin(SpellList.LPair.class)
    class LPair implements MutableSpellList {
        @Final
        @Shadow
        private SpellList cdr;
        @Final
        @Shadow
        private Iota car;
        @Override
        public void set(int idx, Iota data) {
            MutableSpellList.class.cast(cdr).set(idx - 1, data);
        }
    }
}
