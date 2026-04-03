package io.yukkuric.hexflow.actions.base

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameRecoverStack

// [code], ...args -> thoth(code, getData(args))
abstract class AbstractThoth : ActionBound() {
    // built data &
    open fun getData(): Pair<SpellList, Int> {
        val ret = stack.getList(stack.lastIndex, stack.size)
        return Pair(ret, 1)
    }

    override fun operateBound(): OperationResult {
        val (datums, usedArgs) = getData()
        assertArgCount(usedArgs + 1)
        val instrs = stack.getList(stack.lastIndex - usedArgs, stack.size)
        dropStack(usedArgs + 1)

        return doThoth(instrs, datums)
    }

    abstract fun doThoth(code: SpellList, data: SpellList): OperationResult

    fun resultThoth(code: SpellList, data: SpellList): OperationResult {
        val frameThoth = FrameForEach(data, code, null, mutableListOf())
        return OperationResult(
            image.withUsedOp(),
            listOf(),
            continuation.pushFrame(frameThoth),
            HexEvalSounds.THOTH
        )
    }

    fun resultPureThoth(code: SpellList, data: SpellList): OperationResult {
        val frameThoth = FrameForEach(data, code, listOf(), mutableListOf())
        val frameKeepFrame = FrameRecoverStack(stack)
        return OperationResult(
            image.copy(opsConsumed = image.opsConsumed + 1, stack = listOf()),
            listOf(),
            continuation.pushFrame(frameKeepFrame).pushFrame(frameThoth),
            HexEvalSounds.THOTH
        )
    }
}