package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel

data class FrameSortByKey(
    val data: TreeList<Iota>,
    val keyFunc: TreeList<Iota>,
    val keyData: List<Double>,
) : ContinuationFrame {
    override val type = TYPE

    override fun evaluate(
        continuation: SpellContinuation, level: ServerLevel, harness: CastingVM
    ): CastResult {
        val image = harness.image
        val stack = image.stack

        // collect first element on stack as key number
        val key: Double
        try {
            key = stack.getDouble(stack.lastIndex, stack.size)
        } catch (e: Mishap) {
            return CastResult(
                NullIota(),
                SpellContinuation.Done,
                image,
                listOf(OperatorSideEffect.DoMishap(e, Mishap.Context(null, null))),
                ResolvedPatternType.ERRORED,
                HexEvalSounds.MISHAP.get(),
            )
        }
        val newKeyData = keyData.toMutableList().also { it.add(key) }

        // assign next keyFunc & sorter
        return if (newKeyData.size < data.size) {
            // first eval keyFunc, then next sorter
            CastResult(
                NullIota(),
                continuation
                    .pushFrame(copy(keyData = newKeyData))
                    .pushFrame(FrameEvaluate(keyFunc, true)),
                image.withResetEscape().copy(
                    opsConsumed = image.opsConsumed + 1,
                    stack = TreeList.from(listOf(data[newKeyData.size])),
                ),
                listOf(),
                ResolvedPatternType.EVALUATED,
                HexEvalSounds.NOTHING.get(),
            )
        }
        // or do sorting now
        else {
            val sorted = (0 until data.size)
                .map { Pair(data[it], newKeyData[it]) }.sortedBy { it.second }
                .map { it.first }
            val result = ListIota(sorted)
            CastResult(
                NullIota(),
                continuation,
                image.withResetEscape().copy(
                    stack = TreeList.from(listOf(result)),
                ),
                listOf(),
                ResolvedPatternType.EVALUATED,
                HexEvalSounds.THOTH.get(),
            )
        }
    }

    override fun breakDownwards(stack: TreeList<Iota>) = true to stack

    override fun size() = data.size + keyFunc.size


    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameSortByKey> = object : ContinuationFrame.Type<FrameSortByKey> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameSortByKey> { inst ->
                inst.group(
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("data").forGetter { it.data },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("keyFunc").forGetter { it.keyFunc },
                    Codec.DOUBLE.listOf().fieldOf("keyData").forGetter { it.keyData },
                ).apply(inst, ::FrameSortByKey)
            }

            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()),
                FrameSortByKey::data,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()),
                FrameSortByKey::keyFunc,
                ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()),
                FrameSortByKey::keyData,
                ::FrameSortByKey
            )

            override fun codec() = CODEC

            override fun streamCodec() = STREAM_CODEC
        }
    }
}