package io.yukkuric.hexflow

import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger

object HexFlow {
    const val MOD_ID: String = "hexflow"
    val LOGGER: Logger = LogUtils.getLogger()
    fun flowModLoc(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }

    lateinit var API: IAPI

    abstract class IAPI {
        init {
            API = this
        }

        abstract fun modLoaded(id: String): Boolean
    }
}