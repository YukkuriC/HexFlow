package io.yukkuric.hexflow.actions.special

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.math.HexAngle
import at.petrak.hexcasting.api.casting.math.HexDir.*
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.utils.lightPurple
import at.petrak.hexcasting.common.casting.actions.math.SpecialHandlerNumberLiteral
import net.minecraft.network.chat.Component

object NoobNumFactory : SpecialHandler.Factory<NoobNumFactory.NoobNumberLiteral> {
    private fun ch2angle(ch: Char) = when (ch) {
        'w' -> HexAngle.FORWARD
        'q' -> HexAngle.LEFT
        'e' -> HexAngle.RIGHT
        'a' -> HexAngle.LEFT_BACK
        'd' -> HexAngle.RIGHT_BACK
        's' -> HexAngle.BACK
        else -> throw IllegalStateException()
    }

    override fun tryMatch(pat: HexPattern, env: CastingEnvironment): NoobNumberLiteral? {
        val sig = pat.anglesSignature()
        val isPos = sig.startsWith("aqawdedq")
        val isNeg = sig.startsWith("dedwaqae")
        if (!(isPos || isNeg)) return null;
        var res = 0.0
        var mode = EAST
        for (ch in sig.substring(8)) {
            mode = mode.rotatedBy(ch2angle(ch))
            when (mode) {
                NORTH_EAST -> res = res * 2 + 1
                EAST -> res *= 2
                SOUTH_EAST -> res /= 10
                else -> {}
            }
        }
        if (isNeg) res *= -1
        return NoobNumberLiteral(res)
    }

    class NoobNumberLiteral(val num: Double) : SpecialHandler {
        override fun act() = SpecialHandlerNumberLiteral.InnerAction(num)
        override fun getName() = Component.translatable(
            "hexcasting.special.hexflow:noob_num",
            Action.DOUBLE_FORMATTER.format(num)
        ).lightPurple
    }
}