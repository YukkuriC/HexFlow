package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapEvalTooMuch
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameInfiniteLoop

object OpInfiniteLoop : Action {
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        val stack = image.stack.toMutableList()
        if (stack.isEmpty()) throw MishapNotEnoughArgs(1, 0)

        // args
        val topIota = stack.removeLastOrNull()!!
        val code = if (topIota is ListIota) topIota.list
        else if (topIota.executable()) TreeList.from(listOf(topIota))
        else throw MishapInvalidIota.of(topIota, 0, "evaluatable")
        if (code.isEmpty()) throw MishapEvalTooMuch() // shortcut :)

        // cont
        val newCont = continuation
            .pushFrame(FrameInfiniteLoop(code))

        // result
        return OperationResult(
            image.copy(stack = TreeList.from(stack)),
            listOf(),
            newCont,
            HexEvalSounds.THOTH.get()
        )
    }
}