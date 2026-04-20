package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import io.yukkuric.hexflow.helpers.deepCopy

// (114,(1919),514),num_-3,build_nested
object OpBuildNested : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val orig = args.get(0)
        if (orig !is ListIota) throw MishapInvalidIota.ofType(orig, 0, "list")
        var idx = args.getInt(1)
        val n = orig.list.size()
        if (idx < 0) idx += n
        if (idx >= n) return listOf(orig)
        // do copy
        val toModify = ArrayList<Iota>()
        orig.list.forEach(toModify::add)
        toModify[idx] = orig.deepCopy(env.world.registryAccess())
        return listOf(ListIota(toModify))
    }
}