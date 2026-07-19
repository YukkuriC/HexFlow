package io.yukkuric.hexflow.actions.base

import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

// [code], ...args -> thoth(code, getData(args))
// also default impl. for PureMap
// (comment_polluted)splat,(duplicate,bool_coerce,(stack_len,last_n_list,halt)unappend,if,eval)(1,1,4,5,null,4)pure_map,print,pop,print
open class AbstractThoth(open val isPure: Boolean = true) : ActionBound() {
    // built data & args count
    open fun getData(): Pair<TreeList<Iota>, Int> {
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

    open fun doThoth(code: TreeList<Iota>, data: TreeList<Iota>) =
        if (isPure) resultPureThoth(code, data) else resultThoth(code, data)

    fun resultThoth(code: TreeList<Iota>, data: TreeList<Iota>): OperationResult {
        val frameThoth = FrameForEach(data, code, treeStack, treeStack, TreeList.empty())
        return OperationResult(
            image.copy(opsConsumed = image.opsConsumed + 1, stack = TreeList.empty()),
            listOf(),
            continuation.pushFrame(frameThoth),
            HexEvalSounds.THOTH
        )
    }

    fun resultPureThoth(code: TreeList<Iota>, data: TreeList<Iota>): OperationResult {
        val frameThoth = FrameForEach(data, code, TreeList.empty(), TreeList.from(stack), TreeList.empty())
        return OperationResult(
            image.copy(opsConsumed = image.opsConsumed + 1, stack = TreeList.empty()),
            listOf(),
            continuation.pushFrame(frameThoth),
            HexEvalSounds.THOTH
        )
    }

    companion object {
        val MaxDataCount
            get() = HexConfig.server().maxOpCount()
    }
}