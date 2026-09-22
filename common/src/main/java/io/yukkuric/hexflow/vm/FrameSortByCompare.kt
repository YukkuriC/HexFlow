package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel

/**
 * left & right should always be non-empty the same time
 * at least 2 in mergeQueue, or should summary before
 */
data class FrameSortByCompare(
    val compareFunc: TreeList<Iota>,
    val mergedList: TreeList<Iota>,
    val mergingLeft: TreeList<Iota>,
    val mergingRight: TreeList<Iota>,
    val mergeQueue: TreeList<TreeList<Iota>>,
) : ContinuationFrame {
    override val type = TYPE

    override fun evaluate(
        continuation: SpellContinuation, level: ServerLevel, harness: CastingVM
    ): CastResult {
        val image = harness.image
        val stack = image.stack

        var mergedMutable = mergedList
        var leftMutable = mergingLeft
        var rightMutable = mergingRight
        var queueMutable = mergeQueue

        // collect first element on stack as key number
        val addFirst: Boolean
        try {
            addFirst = stack.getBool(stack.lastIndex, stack.size)
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

        // add selected
        var mergeEarlyEnd: Boolean
        if (addFirst) {
            mergedMutable = mergedMutable.appended(leftMutable.head())
            leftMutable = leftMutable.tail()
            mergeEarlyEnd = leftMutable.isEmpty()
        } else {
            mergedMutable = mergedMutable.appended(rightMutable.head())
            rightMutable = rightMutable.tail()
            mergeEarlyEnd = rightMutable.isEmpty()
        }

        // early end: add all & next turn
        if (mergeEarlyEnd) {
            mergedMutable = mergedMutable.appendedAll(leftMutable).appendedAll(rightMutable)
            // leftMutable = TreeList.empty()
            // rightMutable = TreeList.empty()

            // summary now if only 1 in queue
            if (queueMutable.isEmpty()) {
                return CastResult(
                    NullIota(),
                    continuation,
                    image.withResetEscape().copy(
                        stack = TreeList.from(listOf(ListIota(mergedMutable))),
                    ),
                    listOf(),
                    ResolvedPatternType.EVALUATED,
                    HexEvalSounds.THOTH.get(),
                )
            }

            queueMutable = queueMutable.appended(mergedMutable)
            mergedMutable = TreeList.empty()
            leftMutable = queueMutable[0]
            rightMutable = queueMutable[1]
            queueMutable = queueMutable.slice(2, queueMutable.size)
        }

        // first eval keyFunc, then next sorter
        return CastResult(
            NullIota(),
            continuation.pushFrame(
                copy(
                    mergedList = mergedMutable,
                    mergingLeft = leftMutable,
                    mergingRight = rightMutable,
                    mergeQueue = queueMutable,
                )
            ).pushFrame(FrameEvaluate(compareFunc, true)),
            image.withResetEscape().copy(
                opsConsumed = image.opsConsumed + 1,
                stack = TreeList.from(listOf(leftMutable[0], rightMutable[0])),
            ),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.NOTHING.get(),
        )
    }

    override fun breakDownwards(stack: TreeList<Iota>) = true to stack

    override fun size() = compareFunc.size + mergedList.size + mergingLeft.size + mergingRight.size + mergeQueue.size

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameSortByCompare> = object : ContinuationFrame.Type<FrameSortByCompare> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameSortByCompare> { inst ->
                inst.group(
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("compareFunc").forGetter { it.compareFunc },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("mergedList").forGetter { it.mergedList },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("mergingLeft").forGetter { it.mergingLeft },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("mergingRight").forGetter { it.mergingRight },
                    TreeList.codecOf(TreeList.codecOf(IotaType.TYPED_CODEC))
                        .fieldOf("mergeQueue").forGetter { it.mergeQueue },
                ).apply(inst, ::FrameSortByCompare)
            }

            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()),
                FrameSortByCompare::compareFunc,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()),
                FrameSortByCompare::mergedList,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()),
                FrameSortByCompare::mergingLeft,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()),
                FrameSortByCompare::mergingRight,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()).apply(TreeList.streamCodecOp()),
                FrameSortByCompare::mergeQueue,
                ::FrameSortByCompare
            )

            override fun codec() = CODEC

            override fun streamCodec() = STREAM_CODEC
        }
    }
}