package io.yukkuric.hexflow.helpers

import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

val Vec3.attachCenter
    get() = Vec3(0.5 + Mth.floor(x), 0.5 + Mth.floor(y), 0.5 + Mth.floor(z))