package io.yukkuric.hexflow.actions.thoth

import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.actions.base.AbstractThoth
import io.yukkuric.hexflow.vm.FrameRecoverStack
import io.yukkuric.hexflow.vm.FrameSortByCompare

/*
\100,duplicate,duplicate,duplicate_n,last_n_list
(pop,random)thoth
(#debug,less),swap,sort_by/cmp

(1,2,3,4,5,6,7,8,9,114514)
(random,num_0.5,greater),swap,sort_by/cmp
 */
object OpSortByCompare : AbstractThoth() {
    override fun doThoth(code: TreeList<Iota>, data: TreeList<Iota>): OperationResult {
        // no data to sort
        if (data.size < 2) {
            stack.add(ListIota(data))
            return OperationResult(
                image.copy(opsConsumed = image.opsConsumed + 1, stack = TreeList.from(stack)),
                listOf(),
                continuation,
                HexEvalSounds.THOTH.get()
            )
        }

        val frameFirstEval = FrameEvaluate(code, true)
        val initQueue = data.map { TreeList.from(listOf(it)) }
        val frameSorter = FrameSortByCompare(
            code,
            TreeList.empty(), initQueue[0], initQueue[1],
            initQueue.slice(2, initQueue.size)
        )
        val frameKeepFrame = FrameRecoverStack(stack)

        return OperationResult(
            image.copy(
                opsConsumed = image.opsConsumed + 1,
                stack = TreeList.from(listOf(initQueue[0][0], initQueue[1][0]))
            ),
            listOf(),
            continuation.pushFrame(frameKeepFrame).pushFrame(frameSorter).pushFrame(frameFirstEval),
            HexEvalSounds.THOTH.get()
        )
    }
}