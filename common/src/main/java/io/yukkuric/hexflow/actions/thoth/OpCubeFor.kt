package io.yukkuric.hexflow.actions.thoth

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapEvalTooMuch
import at.petrak.hexcasting.api.mod.HexConfig
import io.yukkuric.hexflow.actions.base.AbstractThoth
import net.minecraft.core.BlockPos
import kotlin.math.abs

// [code], pos1, pos2(, option=0)
// (print,break_block)(#my_aim,raycast,add)(vec_-1_-1_-1,vec_1_1_1)pure_map,splat,for_range/cube
// (pop)(vec,vec_114514_1919810_19260817)splat,for_range/cube
class OpCubeFor(override val isPure: Boolean) : AbstractThoth() {
    override fun getData(): Pair<SpellList, Int> {
        val size = stack.size
        val optionOffset = countOptionNums(1)
        val option = when (optionOffset) {
            1 -> stack.getIntBetween(size - 1, 0, 3, size)
            else -> 0
        }
        val pos1 = stack.getBlockPos(size - 2 - optionOffset, size)
        val pos2 = stack.getBlockPos(size - 1 - optionOffset, size)

        // pre-check cuboid size
        pos1.subtract(pos2).let {
            val preSize = abs(1f * it.x * it.y * it.z)
            if (preSize >= HexConfig.server().maxOpCount()) throw MishapEvalTooMuch()
        }

        // build & sort list
        var pts = BlockPos.betweenClosed(pos1, pos2).map(BlockPos::getCenter)
        when (option) {
            1, 2 -> {
                ((env as? CircleCastEnv)?.impetus?.blockPos?.center ?: env.castingEntity?.position())?.let { cmpSrc ->
                    val cmpDir = 1.5f - option // 1 near, 2 far
                    pts = pts.sortedBy { it.distanceToSqr(cmpSrc) * cmpDir }
                }
            }

            3 -> {
                pts = pts.shuffled()
            }
        }

        return Pair(SpellList.LList(pts.map(::Vec3Iota)), 2 + optionOffset)
    }
}