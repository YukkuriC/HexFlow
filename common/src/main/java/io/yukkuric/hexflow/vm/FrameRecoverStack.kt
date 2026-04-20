package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
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
            NullIota.INSTANCE,
            continuation,
            // reset escapes so they don't carry over to other iterations or out of thoth
            harness.image.copy(stack = recoverAnyway(harness.image.stack)),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.NOTHING,
        )
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameRecoverStack> = object : ContinuationFrame.Type<FrameRecoverStack> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameRecoverStack> { inst ->
                inst.group(
                    IotaType.TYPED_CODEC.listOf().fieldOf("myStack").forGetter { it.myStack }
                ).apply(inst, ::FrameRecoverStack)
            }

            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()), FrameRecoverStack::myStack,
                ::FrameRecoverStack
            )

            override fun codec() = CODEC

            override fun streamCodec() = STREAM_CODEC
        }
    }
}