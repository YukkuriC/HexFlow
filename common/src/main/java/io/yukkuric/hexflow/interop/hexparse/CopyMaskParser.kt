package io.yukkuric.hexflow.interop.hexparse

import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import io.yukkuric.hexflow.actions.special.FishermanCopyMaskFactory
import io.yukkuric.hexparse.api.HexParseAPI
import io.yukkuric.hexparse.parsers.str2nbt.BaseConstParser
import net.minecraft.nbt.CompoundTag

object CopyMaskParser : BaseConstParser.Regex("^copy_mask_[-n]+$") {
    override fun parse(str: String): CompoundTag {
        val sb = StringBuilder(FishermanCopyMaskFactory.PATTERN_HEAD)
        var line = false
        for (chr in str.substring(10)) {
            when (chr) {
                '-' -> {
                    sb.append(if (line) 'w' else 'q')
                    line = true
                }

                'n' -> {
                    sb.append(if (line) "qd" else "ad")
                    line = false
                }
            }
        }
        val pat = HexPattern.fromAngles(sb.toString(), HexDir.EAST)
        return IotaType.serialize(PatternIota(pat))
    }

    fun initSelf() {
        HexParseAPI.AddForthParser(this)
        HexParseAPI.AddSpecialHandlerBackParser(FishermanCopyMaskFactory.Copier::class.java) { handler, _ ->
            "copy_mask_" + handler.targetsString
        }
    }
}