package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

data class FrameRecoverStack(val myStack: List<Iota>) : ContinuationFrame {
    override val type = TYPE
    override fun size() = myStack.size

    fun recoverAnyway(stack: List<Iota>): MutableList<Iota> {
        val newStack = myStack.toMutableList()
        newStack.addAll(stack)
        return newStack
    }

    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        return false to recoverAnyway(stack)
    }

    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        return CastResult(
            NullIota(),
            continuation,
            // reset escapes so they don't carry over to other iterations or out of thoth
            harness.image.copy(stack = recoverAnyway(harness.image.stack)),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.NOTHING,
        )
    }

    override fun serializeToNBT() = NBTBuilder {
        "stack" %= myStack.serializeToNBT()
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameRecoverStack> = object : ContinuationFrame.Type<FrameRecoverStack> {
            override fun deserializeFromNBT(tag: CompoundTag, world: ServerLevel) = FrameRecoverStack(
                HexIotaTypes.LIST.deserialize(
                    tag.getList("stack", Tag.TAG_COMPOUND),
                    world
                )!!.list.toMutableList()
            )
        }
    }
}