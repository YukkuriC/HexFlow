package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameRecoverStack

// (comment_polluted)splat,(duplicate,bool_coerce,(stack_len,last_n_list,halt)unappend,if,eval)(1,1,4,5,null,4)pure_map
object OpPureMap : AbstractThoth() {
    override fun doThoth(code: SpellList, data: SpellList): OperationResult {
        val frameThoth = FrameForEach(data, code, listOf(), mutableListOf())
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