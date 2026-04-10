package io.yukkuric.hexflow.actions

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions
import io.yukkuric.hexflow.HexFlow.flowModLoc
import io.yukkuric.hexflow.actions.base.AbstractThoth
import io.yukkuric.hexflow.actions.thoth.OpCubeFor
import io.yukkuric.hexflow.actions.thoth.OpFloodFillFor
import io.yukkuric.hexflow.actions.thoth.OpLineFor
import io.yukkuric.hexflow.actions.thoth.OpPureReduce
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

class HexFlowPatterns {
    companion object {
        private val CACHED: MutableMap<ResourceLocation, ActionRegistryEntry> = HashMap()

        init {
            wrap("pure_map", "dadadad", HexDir.NORTH_EAST, AbstractThoth())
            wrap("pure_reduce", "waawadadad", HexDir.NORTH_EAST, OpPureReduce)
            wrap("for_range/cube", "dadadqqaqqqqq", HexDir.NORTH_EAST, OpCubeFor(false))
            wrap("for_range/cube/pure", "dadadadqqaqqqqq", HexDir.NORTH_EAST, OpCubeFor(true))
            wrap("for_range/line", "dadadawwa", HexDir.NORTH_EAST, OpLineFor(false))
            wrap("for_range/line/pure", "dadadadawwa", HexDir.NORTH_EAST, OpLineFor(true))
            wrap(
                "for_range/floodfill",
                "dadadqadadwdadadwdadaddwwawwaadaddwaaddad",
                HexDir.NORTH_EAST,
                OpFloodFillFor(false)
            )
            wrap(
                "for_range/floodfill/pure",
                "dadadadqadadwdadadwdadaddwwawwaadaddwaaddad",
                HexDir.NORTH_EAST,
                OpFloodFillFor(true)
            )


            wrap("build_nested", "edqdeqdwewwdwqwdwwew", HexDir.SOUTH_WEST, OpBuildNested)
            wrap("nested_modify", "wdwawedqdewawdw", HexDir.SOUTH_WEST, OpNestedModify)
            wrap("mass_rotate", "edqdewawddw", HexDir.SOUTH_WEST, OpMassRotate)
            wrap("weak_escape", "qqqaww", HexDir.WEST, OpWeakEscape)
            wrap("call_stack", "dwdeaqqa", HexDir.SOUTH_EAST, OpCallStack)
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