package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import io.yukkuric.hexflow.helpers.deepCopy
import io.yukkuric.hexflow.mixin_interface.MutableSpellList

// ([114,514,[1,[[9[19]],810]]],[2,1,0,1,0],get_caster)splat,hexflow:nested_modify
// ([114],[2,1,0,1,0],get_caster)splat,hexflow:nested_modify
object OpNestedModify : ConstMediaAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        args.getList(0)
        val orig = args[0] as ListIota
        val listNbt = orig.deepCopy(env.world.registryAccess())
        val idxList = args.getList(1)
        val n = idxList.size()
        var setter = listNbt.list
        try {
            for (i in 0 until n) {
                var idx_iota = idxList.getAt(i)
                if (idx_iota !is DoubleIota) throw MishapInvalidIota.ofType(idx_iota, i, "double")
                var idx = Math.round(idx_iota.double).toInt()
                val size = setter.size()
                if (idx < 0) idx += setter.size()
                if (idx >= size) return listOf(args[0])
                if (i == n - 1) (setter as MutableSpellList)[idx] = args[2]
                else setter = (setter.getAt(idx) as ListIota).list
            }
        } catch (e: Mishap) {
            throw e
        } catch (e: Throwable) {
            return listOf(args[0])
        }
        return listOf(listNbt)
    }
}