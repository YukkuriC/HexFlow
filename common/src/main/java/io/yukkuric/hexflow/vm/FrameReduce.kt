package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel

data class FrameReduce(
    val data: SpellList,
    val code: SpellList,
) : ContinuationFrame {
    override val type = TYPE
    override fun size() = data.size() + code.size()

    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        // dump sub-stack as a list
        return true to mutableListOf(ListIota(stack))
    }

    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        var stack = harness.image.stack.toMutableList()

        // If we still have data to process...
        val (newImage, newCont) = if (data.nonEmpty) {
            val cont2 = continuation
                .pushFrame(FrameReduce(data.cdr, code))
                .pushFrame(FrameEvaluate(code, true))
            stack.add(data.car) // add next reducer
            Pair(harness.image.withUsedOp(), cont2)
        } else {
            // pack whole stack finally
            stack = mutableListOf(ListIota(stack))
            Pair(harness.image, continuation)
        }
        return CastResult(
            ListIota(code),
            newCont,
            newImage.withResetEscape().copy(stack = stack),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH,
        )
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameReduce> = object : ContinuationFrame.Type<FrameReduce> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameReduce> { inst ->
                inst.group(
                    SpellList.CODEC.fieldOf("data").forGetter { it.data },
                    SpellList.CODEC.fieldOf("code").forGetter { it.code },
                ).apply(inst, ::FrameReduce)
            }

            val STREAM_CODEC = StreamCodec.composite(
                SpellList.STREAM_CODEC, FrameReduce::data,
                SpellList.STREAM_CODEC, FrameReduce::code,
                ::FrameReduce
            )

            override fun codec() = CODEC

            override fun streamCodec() = STREAM_CODEC
        }
    }
}
