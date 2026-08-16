package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameFSM

object OpFSM : Action {
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        val listRaw = image.stack.getList(0)
        val states = TreeList.from(listRaw.mapIndexed { idx, iota ->
            if (iota is ListIota) iota.list
            else if (iota.executable()) TreeList.from(listOf(iota))
            else throw MishapInvalidIota.of(iota, idx, "evaluatable")
        })

        var newCont = continuation
            .pushFrame(FrameFSM(states))
        if (states.isNotEmpty()) newCont = newCont.pushFrame(FrameEvaluate(states[0], true))

        return OperationResult(
            image.copy(stack = image.stack.init()),
            listOf(),
            newCont,
            HexEvalSounds.THOTH.get()
        )
    }
}
