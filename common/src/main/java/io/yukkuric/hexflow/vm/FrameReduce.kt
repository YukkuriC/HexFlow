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

data class FrameReduce(
    val data: TreeList<Iota>,
    val code: TreeList<Iota>,
) : ContinuationFrame {
    override val type = TYPE
    override fun size() = data.size + code.size

    override fun breakDownwards(stack: TreeList<Iota>): Pair<Boolean, TreeList<Iota>> {
        // dump sub-stack as a list
        return true to stack
    }

    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        var stack = harness.image.stack.toMutableList()

        // If we still have data to process...
        val (newImage, newCont) = if (data.isNotEmpty()) {
            val cont2 = continuation
                .pushFrame(FrameReduce(data.tail(), code))
                .pushFrame(FrameEvaluate(code, true))
            stack.add(data.head()) // add next reducer
            Pair(harness.image.withUsedOp(), cont2)
        } else {
            // pack whole stack finally
            stack = mutableListOf(ListIota(stack))
            Pair(harness.image, continuation)
        }
        return CastResult(
            ListIota(code),
            newCont,
            newImage.withResetEscape().copy(stack = TreeList.from(stack)),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH.get(),
        )
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameReduce> = object : ContinuationFrame.Type<FrameReduce> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameReduce> { inst ->
                inst.group(
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("data").forGetter { it.data },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("code").forGetter { it.code },
                ).apply(inst, ::FrameReduce)
            }

            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameReduce::data,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameReduce::code,
                ::FrameReduce
            )

            override fun codec() = CODEC

            override fun streamCodec() = STREAM_CODEC
        }
    }
}
