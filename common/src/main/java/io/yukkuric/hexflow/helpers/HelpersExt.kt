package io.yukkuric.hexflow.helpers

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.serializeToNBT
import net.minecraft.nbt.ListTag
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

val Vec3.attachCenter
    get() = Vec3(0.5 + Mth.floor(x), 0.5 + Mth.floor(y), 0.5 + Mth.floor(z))

fun Iterable<Iterable<Iota>>.serializeToNBT() = ListTag().also {
    for (s in this) it.add(s.serializeToNBT())
}