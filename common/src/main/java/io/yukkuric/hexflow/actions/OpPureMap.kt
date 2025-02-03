package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameRecoverStack

// (comment_polluted)splat,(duplicate,bool_coerce,(stack_len,last_n_list,halt)unappend,if,eval)(1,1,4,5,null,4)pure_map
object OpPureMap : Action {
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        val stack = image.stack.toMutableList()

        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val instrs = stack.getList(stack.lastIndex - 1, stack.size)
        val datums = stack.getList(stack.lastIndex, stack.size)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        // build thoth
        val frameThoth = FrameForEach(datums, instrs, listOf(), mutableListOf())
        val frameKeepFrame = FrameRecoverStack(stack)

        val newImg = image.copy(opsConsumed = image.opsConsumed + 1, stack = listOf())
        return OperationResult(
            newImg,
            listOf(),
            continuation.pushFrame(frameKeepFrame).pushFrame(frameThoth),
            HexEvalSounds.THOTH
        )
    }
}