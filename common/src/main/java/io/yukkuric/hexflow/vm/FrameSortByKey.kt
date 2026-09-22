package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexflow.helpers.deserializeKeyToIotaList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

data class FrameSortByKey(
    val data: TreeList<Iota>,
    val keyFunc: SpellList,
    val keyData: TreeList<Double>,
) : ContinuationFrame {
    override val type = TYPE

    override fun evaluate(
        continuation: SpellContinuation, level: ServerLevel, harness: CastingVM
    ): CastResult {
        val image = harness.image
        val stack = image.stack

        // collect first element on stack as key number
        val key: Double
        try {
            key = stack.getDouble(stack.lastIndex, stack.size)
        } catch (e: Mishap) {
            return CastResult(
                NullIota(),
                SpellContinuation.Done,
                image,
                listOf(OperatorSideEffect.DoMishap(e, Mishap.Context(null, null))),
                ResolvedPatternType.ERRORED,
                HexEvalSounds.MISHAP,
            )
        }
        val newKeyData = keyData.appended(key)

        // assign next keyFunc & sorter
        return if (newKeyData.size < data.size) {
            // first eval keyFunc, then next sorter
            CastResult(
                NullIota(),
                continuation
                    .pushFrame(copy(keyData = newKeyData))
                    .pushFrame(FrameEvaluate(keyFunc, true)),
                image.withResetEscape().copy(
                    opsConsumed = image.opsConsumed + 1,
                    stack = TreeList.from(listOf(data[newKeyData.size])),
                ),
                listOf(),
                ResolvedPatternType.EVALUATED,
                HexEvalSounds.NOTHING,
            )
        }
        // or do sorting now
        else {
            val sorted = (0 until data.size)
                .map { Pair(data[it], newKeyData[it]) }.sortedBy { it.second }
                .map { it.first }
            val result = ListIota(sorted)
            CastResult(
                NullIota(),
                continuation,
                image.withResetEscape().copy(
                    stack = TreeList.from(listOf(result)),
                ),
                listOf(),
                ResolvedPatternType.EVALUATED,
                HexEvalSounds.THOTH,
            )
        }
    }

    override fun breakDownwards(stack: List<Iota>) = true to stack
    override fun serializeToNBT() = NBTBuilder {
        "data" %= data.serializeToNBT()
        "keyFunc" %= keyFunc.serializeToNBT()
        "keyData" %= ListTag().also {
            for (num in keyData) it.add(DoubleTag.valueOf(num))
        }
    }

    override fun size() = data.size + keyFunc.size()


    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameSortByKey> = object : ContinuationFrame.Type<FrameSortByKey> {
            override fun deserializeFromNBT(
                tag: CompoundTag,
                world: ServerLevel
            ) = FrameSortByKey(
                tag.deserializeKeyToIotaList("data", world).let(TreeList<*>::from),
                tag.deserializeKeyToIotaList("keyFunc", world),
                tag.getList("keyData", Tag.TAG_DOUBLE).map { it.asDouble }.let(TreeList<*>::from),
            )
        }
    }
}