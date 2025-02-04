package io.yukkuric.hexflow.vm

import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.common.lib.hex.HexContinuationTypes
import io.yukkuric.hexflow.HexFlow.flowModLoc
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

class HexFlowFrames {
    companion object {
        private val FRAMES: MutableMap<ResourceLocation, ContinuationFrame.Type<*>> = HashMap()

        init {
            wrap("recover_stack", FrameRecoverStack.TYPE)
            wrap("reduce", FrameReduce.TYPE)
        }

        @JvmStatic
        fun registerFrames() {
            val reg = HexContinuationTypes.REGISTRY
            for ((key, value) in FRAMES) Registry.register(reg, key, value)
        }

        private fun <U : ContinuationFrame, T : ContinuationFrame.Type<U>> wrap(name: String, continuation: T) {
            val key = flowModLoc(name)
            FRAMES[key] = continuation
        }
    }
}