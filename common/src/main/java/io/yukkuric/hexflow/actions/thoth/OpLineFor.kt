package io.yukkuric.hexflow.actions.thoth

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.getPositiveInt
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapStackSize
import io.yukkuric.hexflow.actions.base.AbstractThoth
import io.yukkuric.hexflow.helpers.attachCenter
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

// [code], pos1, pos2(, option=2(, sep=?))
// option bit: 1=has start, 2=has end, 4=attach center
/*
    (duplicate,sentinel/create/great,num_10,explode)
    get_caster,entity_pos/eye,duplicate,get_caster,get_entity_look,(300)splat,mul,add
    (2,100)splat,for_range/line
 */
class OpLineFor(override val isPure: Boolean) : AbstractThoth() {
    override fun getData(): Pair<SpellList, Int> {
        val size = stack.size
        val optionOffset = countOptionNums(2)

        // option 1
        val option = if (optionOffset == 0) 2
        else stack.getIntBetween(size - optionOffset, 0, 7, size)
        val hasStart = (option and 1) > 0
        val hasEnd = (option and 2) > 0
        val doAttach = (option and 4) > 0

        // points
        val pos1 = stack.getVec3(size - 2 - optionOffset, size)
        val pos2 = stack.getVec3(size - 1 - optionOffset, size)
        val delta = pos2.subtract(pos1)

        // option 2
        val sep = if (optionOffset == 2) stack.getPositiveInt(size - 1, size)
        else Mth.ceil(maxOf(abs(delta.x), abs(delta.y), abs(delta.z)))

        // prevent memory boom
        if (sep >= MaxDataCount) throw MishapStackSize()

        // dump all points
        val pts = ArrayList<Vec3>()
        val visited = HashSet<Vec3>()
        fun append(pt: Vec3) {
            val toAdd = if (doAttach) pt.attachCenter else pt
            if (toAdd !in visited) {
                visited.add(toAdd)
                pts.add(toAdd)
            }
        }
        if (hasStart) append(pos1)
        for (i in 1 until sep) append(pos1.add(delta.scale(i.toDouble() / sep)))
        if (hasEnd) append(pos2)

        return Pair(
            SpellList.LList(pts.map(::Vec3Iota)), 2 + optionOffset
        )
    }
}