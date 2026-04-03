package io.yukkuric.hexflow.actions.thoth

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.mod.HexConfig
import io.yukkuric.hexflow.actions.base.AbstractThoth
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import java.util.function.Consumer
import kotlin.math.abs

// [code], pos(, option)
// (print,break_block)#my_aim,raycast,for_range/floodfill,print
class OpFloodFillFor(override val isPure: Boolean) : AbstractThoth() {
    companion object {
        private val offsetsFace: List<Vec3i>
        private val offsetsEdge: List<Vec3i>
        private val offsetsCorner: List<Vec3i>
        private val offsetsByOption: MutableMap<Int, List<Vec3i>> = mutableMapOf()

        init {
            val l1 = mutableListOf<Vec3i>()
            val l2 = mutableListOf<Vec3i>()
            val l3 = mutableListOf<Vec3i>()
            for (x in -1..1) {
                val dx = abs(x)
                for (y in -1..1) {
                    val dy = abs(y)
                    for (z in -1..1) {
                        when (dx + dy + abs(z)) {
                            1 -> l1.add(Vec3i(x, y, z))
                            2 -> l2.add(Vec3i(x, y, z))
                            3 -> l3.add(Vec3i(x, y, z))
                        }
                    }
                }
            }

            offsetsFace = l1
            offsetsEdge = l2
            offsetsCorner = l3
        }

        fun buildOffsetCallback(option: Int, callback: (BlockPos) -> Unit): ((BlockPos, Consumer<BlockPos>) -> Unit) {
            val offsetList = offsetsByOption.computeIfAbsent(option) {
                val ret = mutableListOf<Vec3i>()
                if ((it and 1) > 0) ret.addAll(offsetsFace)
                if ((it and 2) > 0) ret.addAll(offsetsEdge)
                if ((it and 4) > 0) ret.addAll(offsetsCorner)
                ret
            }
            return { src: BlockPos, addFunc: Consumer<BlockPos> ->
                callback(src)
                offsetList.forEach { addFunc.accept(src.offset(it)) }
            }
        }
    }

    override fun getData(): Pair<SpellList, Int> {
        val size = stack.size

        // pick args
        var usedArgc = 1
        var lastPos = size - 1
        var option = (stack.lastOrNull() as? DoubleIota)?.let {
            usedArgc++
            stack.getIntBetween(lastPos--, 1, 7, size)
        } ?: 1 // only 6 faces by default
        val startPos = stack.getBlockPos(lastPos, size)
        env.assertPosInRange(startPos)
        val startBlock = env.world.getBlockState(startPos).block

        // BFS
        val maxIter = HexConfig.server().maxOpCount()
        val ret = mutableListOf<Vec3Iota>()
        BlockPos.breadthFirstTraversal(
            startPos, maxIter, maxIter,
            buildOffsetCallback(option) { ret.add(Vec3Iota(it.center)) }) {
            if (!env.isVecInAmbit(it.center)) return@breadthFirstTraversal false
            val newState = env.world.getBlockState(it)
            newState.`is`(startBlock)
        }
        return Pair(SpellList.LList(ret), usedArgc)
    }
}