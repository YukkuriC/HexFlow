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

data class FrameInfiniteLoop(
    val code: SpellList,
) : ContinuationFrame {
    override val type = TYPE
    override fun size() = code.size()
    override fun breakDownwards(stack: List<Iota>) = true to stack
    override fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult {
        return CastResult(
            ListIota(code),
            continuation
                .pushFrame(this)
                .pushFrame(FrameEvaluate(code, true)),
            harness.image.copy(
                parenCount = 0,
                parenthesized = listOf(),
                opsConsumed = harness.image.opsConsumed + 1,
            ),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH,
        )
    }

    override fun serializeToNBT() = NBTBuilder {
        "code" %= code.serializeToNBT()
    }

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameInfiniteLoop> = object : ContinuationFrame.Type<FrameInfiniteLoop> {
            override fun deserializeFromNBT(tag: CompoundTag, world: ServerLevel) = FrameInfiniteLoop(
                HexIotaTypes.LIST.deserialize(tag.getList("code", Tag.TAG_COMPOUND), world)!!.list,
            )
        }
    }
}
