package io.yukkuric.hexflow.actions.special

import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.xplat.IXplatAbstractions
import io.yukkuric.hexflow.HexFlow.flowModLoc
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

class HexFlowSpecialHandlers {
    companion object {
        private val CACHED: MutableMap<ResourceLocation, SpecialHandler.Factory<*>> = HashMap()

        init {
            wrap("noob_num", NoobNumFactory)
        }

        @JvmStatic
        fun registerSpecial() {
            val reg = IXplatAbstractions.INSTANCE.specialHandlerRegistry
            for ((key, value) in CACHED) Registry.register(reg, key, value)
        }

        private fun <U : SpecialHandler, T : SpecialHandler.Factory<U>> wrap(
            name: String,
            factory: T
        ) {
            val key = flowModLoc(name)
            CACHED[key] = factory
        }
    }
}