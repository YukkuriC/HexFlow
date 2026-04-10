package io.yukkuric.hexflow.actions.base

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameRecoverStack

// [code], ...args -> thoth(code, getData(args))
// also default impl. for PureMap
// (comment_polluted)splat,(duplicate,bool_coerce,(stack_len,last_n_list,halt)unappend,if,eval)(1,1,4,5,null,4)pure_map,print,pop,print
open class AbstractThoth : ActionBound() {
    open val isPure: Boolean = true

    // built data & args count
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

    open fun doThoth(code: SpellList, data: SpellList) =
        if (isPure) resultPureThoth(code, data) else resultThoth(code, data)

    fun resultThoth(code: SpellList, data: SpellList): OperationResult {
        val frameThoth = FrameForEach(data, code, null, mutableListOf())
        return OperationResult(
            image.copy(opsConsumed = image.opsConsumed + 1, stack = stack),
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

    companion object {
        val MaxDataCount
            get() = HexConfig.server().maxOpCount()
    }
}