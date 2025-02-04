package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs

abstract class AbstractThoth : Action {
    protected lateinit var image: CastingImage
    protected lateinit var env: CastingEnvironment
    protected lateinit var continuation: SpellContinuation
    protected lateinit var stack: MutableList<Iota>

    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        // cache inside this operate
        this.env = env
        this.image = image
        this.continuation = continuation
        stack = image.stack.toMutableList()

        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val instrs = stack.getList(stack.lastIndex - 1, stack.size)
        val datums = stack.getList(stack.lastIndex, stack.size)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        return doThoth(instrs, datums)
    }

    abstract fun doThoth(code: SpellList, data: SpellList): OperationResult
}