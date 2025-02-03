package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag

// ([114,514,[1,[[9[19]],810]]],[2,1,0,1,0],get_caster)splat,hexflow:nested_modify
// ([114],[2,1,0,1,0],get_caster)splat,hexflow:nested_modify
object OpNestedModify : ConstMediaAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val orig = args.get(0)
        val listNbt = IotaType.serialize(orig)
        val idxList = args.getList(1)
        val n = idxList.size()
        var setter = listNbt
        var setterList: ListTag
        try {
            for (i in 0 until n) {
                var idx_iota = idxList.getAt(i)
                if (idx_iota !is DoubleIota) throw MishapInvalidIota.ofType(idx_iota, i, "double")
                var idx = Math.round(idx_iota.double).toInt()
                setterList = setter.getList(HexIotaTypes.KEY_DATA, Tag.TAG_COMPOUND.toInt())
                if (idx < 0) idx += setterList.size
                if (idx >= setterList.size) return listOf(args[0])
                if (i == n - 1) setterList[idx] = IotaType.serialize(args[2])
                else setter = setterList.getCompound(idx)
            }
        } catch (e: Mishap) {
            throw e
        } catch (e: Throwable) {
            return listOf(args[0])
        }
        return listOf(IotaType.deserialize(listNbt, env.world))
    }
}