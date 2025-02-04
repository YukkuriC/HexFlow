package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveInt
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.casting.actions.stack.OpTwiddling
import kotlin.math.roundToInt

// (0,1,2,3,4)splat,\3,(0,1,2,2,0,1)mass_rotate
object OpMassRotate : Action {
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        val stack = image.stack.toMutableList()
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)
        val range = stack.getPositiveInt(stack.lastIndex - 1, stack.size)
        var rawOrders = stack.getList(stack.lastIndex, stack.size)
        repeat(2) { stack.removeLastOrNull() }
        val orders = mutableListOf<Int>()
        var idx = 0
        while (rawOrders.nonEmpty) {
            val ptr = rawOrders.car
            if (ptr !is DoubleIota) throw MishapInvalidIota.of(ptr, idx, "int")
            val order = ptr.double.roundToInt()
            if (order < 0 || order >= range) throw MishapInvalidIota.of(ptr, idx, "int.between", 0, range)
            orders.add(order)
            rawOrders = rawOrders.cdr; idx++
        }
        return OpTwiddling(range, orders.toIntArray()).operate(env, image.copy(stack = stack), continuation)
    }
}