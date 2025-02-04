package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameRecoverStack
import io.yukkuric.hexflow.vm.FrameReduce

// (comment_polluted,comment_stack)splat,(mul,()eval/cc,swap)(1,1,4,5,1,4)pure_reduce
object OpPureReduce : AbstractThoth() {
    override fun doThoth(code: SpellList, data: SpellList): OperationResult {
        // reduce failed
        if (data.size() < 2) {
            stack.add(ListIota(data))
            return OperationResult(
                image.copy(opsConsumed = image.opsConsumed + 1, stack = stack),
                listOf(),
                continuation,
                HexEvalSounds.THOTH
            )
        }

        val frameKeepFrame = FrameRecoverStack(stack)
        val newStack = mutableListOf<Iota>()
        newStack.add(data.car)
        val reduceData = data.cdr
        val frameThoth = FrameReduce(reduceData, code)

        val newImg = image.copy(opsConsumed = image.opsConsumed + 1, stack = newStack)
        return OperationResult(
            newImg,
            listOf(),
            continuation.pushFrame(frameKeepFrame).pushFrame(frameThoth),
            HexEvalSounds.THOTH
        )
    }
}