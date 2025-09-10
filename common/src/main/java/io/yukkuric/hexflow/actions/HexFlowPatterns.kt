package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions
import io.yukkuric.hexflow.HexFlow.flowModLoc
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

class HexFlowPatterns {
    companion object {
        private val CACHED: MutableMap<ResourceLocation, ActionRegistryEntry> = HashMap()

        init {
            wrap("pure_map", "dadadad", HexDir.NORTH_EAST, OpPureMap)
            wrap("pure_reduce", "waawadadad", HexDir.NORTH_EAST, OpPureReduce)
            wrap("build_nested", "edqdeqdwewwdwqwdwwew", HexDir.SOUTH_WEST, OpBuildNested)
            wrap("nested_modify", "wdwawedqdewawdw", HexDir.SOUTH_WEST, OpNestedModify)
            wrap("mass_rotate", "edqdewawddw", HexDir.SOUTH_WEST, OpMassRotate)
            wrap("weak_escape", "qqqaww", HexDir.WEST, OpWeakEscape)
        }

        @JvmStatic
        fun registerActions() {
            val reg = HexActions.REGISTRY
            for ((key, value) in CACHED) Registry.register(reg, key, value)
        }

        private fun wrap(name: String, signature: String, dir: HexDir, action: Action?): ActionRegistryEntry {
            val pattern = HexPattern.fromAngles(signature, dir)
            val key = flowModLoc(name)
            val entry = ActionRegistryEntry(pattern, action)
            CACHED[key] = entry
            return entry
        }
    }
}