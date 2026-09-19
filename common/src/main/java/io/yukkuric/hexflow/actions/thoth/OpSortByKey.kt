package io.yukkuric.hexflow.actions.thoth

import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.actions.base.AbstractThoth
import io.yukkuric.hexflow.vm.FrameRecoverStack
import io.yukkuric.hexflow.vm.FrameSortByKey

/*
(print)(1,1,4,5,1,4)sort_by/key,print
(num_-1,mul)(1,1,4,5,1,4)sort_by/key,print
(abs)([0,1,2,3],-7355608,vec_123_456_789)sort_by/key,print
([get_caster][])splat,sort_by/key,print // empty list
(pop,get_caster)(1,2,3)sort_by/key,print // and wrong key
 */
object OpSortByKey : AbstractThoth() {
    override fun doThoth(code: TreeList<Iota>, data: TreeList<Iota>): OperationResult {
        // no data to sort
        if (data.isEmpty()) {
            stack.add(ListIota(listOf()))
            return OperationResult(
                image.copy(opsConsumed = image.opsConsumed + 1, stack = TreeList.from(stack)),
                listOf(),
                continuation,
                HexEvalSounds.THOTH.get()
            )
        }

        val frameFirstEval = FrameEvaluate(code, true)
        val frameSorter = FrameSortByKey(data, code, listOf())
        val frameKeepFrame = FrameRecoverStack(stack)

        return OperationResult(
            image.copy(opsConsumed = image.opsConsumed + 1, stack = TreeList.from(listOf(data[0]))),
            listOf(),
            continuation.pushFrame(frameKeepFrame).pushFrame(frameSorter).pushFrame(frameFirstEval),
            HexEvalSounds.THOTH.get()
        )
    }
}