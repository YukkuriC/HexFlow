package io.yukkuric.hexflow.mixin_interface;

import at.petrak.hexcasting.api.casting.iota.Iota;

public interface MutableSpellList {
    void set(int idx, Iota data);
}
