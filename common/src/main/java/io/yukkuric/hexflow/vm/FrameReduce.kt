package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

data class FrameReduce(
    val data: SpellList,
    val code: SpellList,
) : ContinuationFrame {
    override val type = TYPE
    override fun size() = data.size() + code.size()

    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        // dump sub-stack as a list
        return true to mutableListOf(ListIota(stack))
    }

    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        var stack = harness.image.stack.toMutableList()

        // If we still have data to process...
        val (newImage, newCont) = if (data.nonEmpty) {
            val cont2 = continuation
                .pushFrame(FrameReduce(data.cdr, code))
                .pushFrame(FrameEvaluate(code, true))
            stack.add(data.car) // add next reducer
            Pair(harness.image.withUsedOp(), cont2)
        } else {
            // pack whole stack finally
            stack = mutableListOf(ListIota(stack))
            Pair(harness.image, continuation)
        }
        return CastResult(
            ListIota(code),
            newCont,
            newImage.withResetEscape().copy(stack = stack),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH,
        )
    }

    override fun serializeToNBT() = NBTBuilder {
        "data" %= data.serializeToNBT()
        "code" %= code.serializeToNBT()
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameReduce> = object : ContinuationFrame.Type<FrameReduce> {
            override fun deserializeFromNBT(tag: CompoundTag, world: ServerLevel) = FrameReduce(
                HexIotaTypes.LIST.deserialize(tag.getList("data", Tag.TAG_COMPOUND), world)!!.list,
                HexIotaTypes.LIST.deserialize(tag.getList("code", Tag.TAG_COMPOUND), world)!!.list,
            )
        }
    }
}
