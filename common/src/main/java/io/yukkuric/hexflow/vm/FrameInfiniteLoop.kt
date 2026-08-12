package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel

data class FrameInfiniteLoop(
    val code: TreeList<Iota>,
) : ContinuationFrame {
    override val type = TYPE
    override fun size() = code.size
    override fun breakDownwards(stack: TreeList<Iota>) = true to stack
    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        return CastResult(
            ListIota(code),
            continuation
                .pushFrame(this)
                .pushFrame(FrameEvaluate(code, true)),
            harness.image.copy(
                parenCount = 0,
                parenthesized = TreeList.empty(),
                opsConsumed = harness.image.opsConsumed + 1,
            ),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH.get(),
        )
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameInfiniteLoop> = object : ContinuationFrame.Type<FrameInfiniteLoop> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameInfiniteLoop> { inst ->
                inst.group(
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("code").forGetter { it.code },
                ).apply(inst, ::FrameInfiniteLoop)
            }

            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameInfiniteLoop::code,
                ::FrameInfiniteLoop
            )

            override fun codec() = CODEC

            override fun streamCodec() = STREAM_CODEC
        }
    }
}
