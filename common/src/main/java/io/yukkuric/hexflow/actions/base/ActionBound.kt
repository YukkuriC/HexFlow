package io.yukkuric.hexflow.actions.base

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs

abstract class ActionBound : Action {
    protected lateinit var image: CastingImage
    protected lateinit var env: CastingEnvironment
    protected lateinit var continuation: SpellContinuation
    protected lateinit var stack: MutableList<Iota>

    abstract fun operateBound(): OperationResult

    @Synchronized
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
        return operateBound()
    }

    // helpers
    fun assertArgCount(count: Int) {
        if (stack.size < count)
            throw MishapNotEnoughArgs(count, stack.size)
    }

    fun dropStack(count: Int) {
        for (i in 0 until count) stack.removeLastOrNull()
    }
}