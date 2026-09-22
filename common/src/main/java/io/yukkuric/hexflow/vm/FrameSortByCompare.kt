package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.helpers.deserializeKeyToIotaList
import io.yukkuric.hexflow.helpers.deserializeToIotaList
import io.yukkuric.hexflow.helpers.serializeToNBT
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

/**
 * left & right should always be non-empty the same time
 * at least 2 in mergeQueue, or should summary before
 */
data class FrameSortByCompare(
    val compareFunc: SpellList,
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
                HexEvalSounds.MISHAP,
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
                    HexEvalSounds.THOTH,
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
            HexEvalSounds.NOTHING,
        )
    }

    override fun breakDownwards(stack: List<Iota>) = true to stack
    override fun serializeToNBT() = NBTBuilder {
        "compareFunc" %= compareFunc.serializeToNBT()
        "mergedList" %= mergedList.serializeToNBT()
        "mergingLeft" %= mergingLeft.serializeToNBT()
        "mergingRight" %= mergingRight.serializeToNBT()
        "mergeQueue" %= mergeQueue.serializeToNBT()
    }

    override fun size() = compareFunc.size() + mergedList.size + mergingLeft.size + mergingRight.size + mergeQueue.size

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameSortByCompare> = object : ContinuationFrame.Type<FrameSortByCompare> {
            override fun deserializeFromNBT(
                tag: CompoundTag,
                world: ServerLevel
            ) = FrameSortByCompare(
                tag.deserializeKeyToIotaList("compareFunc", world),
                tag.deserializeKeyToIotaList("mergedList", world).let(TreeList<*>::from),
                tag.deserializeKeyToIotaList("mergingLeft", world).let(TreeList<*>::from),
                tag.deserializeKeyToIotaList("mergingRight", world).let(TreeList<*>::from),
                tag.getList("mergeQueue", Tag.TAG_LIST)
                    .map { (it as ListTag).deserializeToIotaList(world) }
                    .map(TreeList<*>::from)
                    .let(TreeList<*>::from),
            )
        }
    }
}