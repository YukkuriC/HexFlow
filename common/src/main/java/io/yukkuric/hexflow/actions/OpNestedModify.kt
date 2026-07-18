package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.TreeList
import kotlin.math.roundToInt

// ([114,514,[1,[[9[19]],810]]],[2,1,0,1,0],get_caster)splat,hexflow:nested_modify
// ([114],[2,1,0,1,0],get_caster)splat,hexflow:nested_modify
object OpNestedModify : ConstMediaAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val theList = args.getList(0)
        val idxList = args.getList(1)
        try {
            val modified = theRealNestedModify(theList, idxList, args[2], 0)
                ?: return listOf(args[0])
            return listOf(ListIota(modified))
        } catch (e: Mishap) {
            throw e
        } catch (e: Throwable) {
            return listOf(args[0])
        }
    }

    // return null for not modified
    fun theRealNestedModify(theList: List<Iota>, idxList: List<Iota>, setData: Iota, depth: Int): TreeList<Iota>? {
        val idxIota = idxList[depth]
        if (idxIota !is DoubleIota) throw MishapInvalidIota.ofType(idxIota, depth, "double")
        var idx = idxIota.double.roundToInt()
        val setter = theList.toMutableList()
        val size = setter.size
        if (idx < 0) idx += size
        if (idx >= size) return null
        if (depth == idxList.size - 1) {
            setter[idx] = setData
        } else {
            val innerIota = setter[idx]
            if (innerIota !is ListIota) throw MishapInvalidIota.ofType(innerIota, depth, "list")
            val inner = theRealNestedModify(innerIota.list, idxList, setData, depth + 1)
                ?: return null
            setter[idx] = ListIota(inner)
        }
        return TreeList.from(setter)
    }
}