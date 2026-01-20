package io.yukkuric.hexflow.actions.special

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.lightPurple
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import it.unimi.dsi.fastutil.booleans.BooleanArrayList
import net.minecraft.network.chat.Component

object FishermanCopyMaskFactory : SpecialHandler.Factory<FishermanCopyMaskFactory.Copier> {
    const val PATTERN_HEAD = "aadaq"

    class Copier(val targets: BooleanArray) : Action, SpecialHandler {
        override fun act() = this
        override fun operate(
            env: CastingEnvironment,
            image: CastingImage,
            continuation: SpellContinuation
        ): OperationResult {
            val stack = image.stack
            if (stack.size < targets.size) throw MishapNotEnoughArgs(targets.size, stack.size)
            val offset = stack.size - targets.size
            val newStack = stack.toMutableList()
            for (i in 0 until targets.size) {
                if (!targets[i]) continue
                newStack.add(stack[i + offset])
            }
            return OperationResult(
                image.copy(
                    stack = newStack,
                    opsConsumed = image.opsConsumed + 1
                ),
                listOf(),
                continuation,
                HexEvalSounds.NORMAL_EXECUTE
            )
        }

        override fun getName() = Component.translatable(
            "hexcasting.special.hexflow:copy_mask",
            targets.map { pick -> if (pick) 'n' else '-' }.joinToString("")
        ).lightPurple
    }

    override fun tryMatch(pattern: HexPattern, env: CastingEnvironment): Copier? {
        val seq = pattern.anglesSignature()
        if (!seq.startsWith(PATTERN_HEAD)) return null
        var ptr = PATTERN_HEAD.length
        var line = false
        val targets = BooleanArrayList()
        while (ptr < seq.length) {
            // -: w / qd
            // n: q / ad
            val chr = seq[ptr++]
            if (chr == if (line) 'w' else 'q') {
                targets.add(false)
                line = true
            } else if (chr == if (line) 'q' else 'a') {
                if (ptr >= seq.length || seq[ptr++] != 'd') return null
                targets.add(true)
                line = false
            }
        }
        if (targets.isEmpty) return null
        return Copier(targets.toBooleanArray())
    }
}