package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota

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
        toModify[idx] = IotaType.deserialize(IotaType.serialize(orig), env.world)
        return listOf(ListIota(toModify))
    }
}