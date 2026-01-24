package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.vm.FrameRecoverStack

object OpCallStack : Action {
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        val stack = image.stack.toMutableList()
        if (stack.isEmpty()) throw MishapNotEnoughArgs(1, 0)

        // args
        var argsCarried = 0
        var codeIdx = 0
        if (stack.last() is DoubleIota) {
            if (stack.size <= 1) throw MishapNotEnoughArgs(2, 1)
            argsCarried = stack.getIntBetween(stack.size - 1, 0, stack.size - 2)
            stack.removeLastOrNull()
            codeIdx = 1
        }
        val topIota = stack.removeLastOrNull()!!
        val code = if (topIota is ListIota) topIota.list
        else if (topIota.executable()) SpellList.LList(listOf(topIota))
        else throw MishapInvalidIota.of(topIota, codeIdx, "evaluatable")

        // stack & cont
        val innerStack = mutableListOf<Iota>()
        for (i in 0 until argsCarried) innerStack.add(stack.removeLastOrNull()!!)
        innerStack.reverse()
        val newCont = continuation
            .pushFrame(FrameRecoverStack(stack))
            .pushFrame(FrameFinishEval)
            .pushFrame(FrameEvaluate(code, true))

        // result
        return OperationResult(
            image.copy(
                stack = innerStack,
                opsConsumed = image.opsConsumed + 1
            ),
            listOf(),
            newCont,
            HexEvalSounds.HERMES
        )
    }
}