package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.*
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import kotlin.math.abs
import kotlin.math.roundToInt

data class FrameFSM(
    val states: TreeList<TreeList<Iota>>,
) : ContinuationFrame {
    override val type = TYPE
    override fun size() = states.sumOf { it.size }
    override fun breakDownwards(stack: TreeList<Iota>) = true to stack
    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        val stack = harness.image.stack
        // try to read a non-negative integer n from the top (last) of the stack
        if (stack.isNotEmpty()) {
            val top = stack.last()
            if (top is DoubleIota) {
                val double = top.double
                val rounded = double.roundToInt()
                if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded >= 0 && rounded < states.size) {
                    // consume stack top + run states[n]
                    val code = states[rounded]
                    return CastResult(
                        ListIota(code),
                        continuation
                            .pushFrame(this)
                            .pushFrame(FrameEvaluate(code, true)),
                        harness.image.copy(
                            stack = stack.init(),
                            parenCount = 0,
                            parenthesized = TreeList.empty(),
                            opsConsumed = harness.image.opsConsumed + 1,
                        ),
                        listOf(),
                        ResolvedPatternType.EVALUATED,
                        HexEvalSounds.THOTH.get(),
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
            HexEvalSounds.NOTHING.get(),
        )
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameFSM> = object : ContinuationFrame.Type<FrameFSM> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameFSM> { inst ->
                inst.group(
                    TreeList.codecOf(TreeList.codecOf(IotaType.TYPED_CODEC)).fieldOf("states").forGetter { it.states },
                ).apply(inst, ::FrameFSM)
            }

            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()).apply(TreeList.streamCodecOp()),
                FrameFSM::states,
                ::FrameFSM
            )

            override fun codec() = CODEC

            override fun streamCodec() = STREAM_CODEC
        }
    }
}
