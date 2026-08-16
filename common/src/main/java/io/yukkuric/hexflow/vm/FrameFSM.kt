package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import kotlin.math.abs
import kotlin.math.roundToInt

data class FrameFSM(
    val states: List<SpellList>,
) : ContinuationFrame {
    override val type = TYPE
    override fun size() = states.sumOf { it.size() }
    override fun breakDownwards(stack: List<Iota>) = true to stack
    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        val stack = harness.image.stack.toMutableList()
        // try to read a non-negative integer n from the top (last) of the stack
        if (stack.isNotEmpty()) {
            val top = stack.last()
            if (top is DoubleIota) {
                val double = top.double
                val rounded = double.roundToInt()
                if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded >= 0 && rounded < states.size) {
                    // consume stack top + run states[n]
                    val code = states[rounded]
                    stack.removeLastOrNull()
                    return CastResult(
                        ListIota(code),
                        continuation
                            .pushFrame(this)
                            .pushFrame(FrameEvaluate(code, true)),
                        harness.image.copy(
                            stack = stack,
                            parenCount = 0,
                            parenthesized = listOf(),
                            opsConsumed = harness.image.opsConsumed + 1,
                        ),
                        listOf(),
                        ResolvedPatternType.EVALUATED,
                        HexEvalSounds.THOTH,
                    )
                }
            }
        }
        // invalid: end FSM
        return CastResult(
            NullIota(),
            continuation,
            harness.image,
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.NOTHING,
        )
    }

    override fun serializeToNBT(): CompoundTag {
        val dump = ListTag()
        for (s in states) dump.add(s.serializeToNBT())
        return NBTBuilder {
            // damn, why ListTag(list, type) private???
            "states" %= dump
        }
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameFSM> = object : ContinuationFrame.Type<FrameFSM> {
            override fun deserializeFromNBT(tag: CompoundTag, world: ServerLevel) = FrameFSM(
                tag.getList("states", Tag.TAG_LIST).map { HexIotaTypes.LIST.deserialize(it, world)!!.list }
            )
        }
    }
}
