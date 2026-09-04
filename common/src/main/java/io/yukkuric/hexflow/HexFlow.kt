package io.yukkuric.hexflow

import com.mojang.logging.LogUtils
import io.yukkuric.hexflow.interop.hexparse.CopyMaskParser
import io.yukkuric.yclib.YCLib
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger

object HexFlow {
    const val MOD_ID: String = "hexflow"
    val LOGGER: Logger = LogUtils.getLogger()
    fun flowModLoc(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }

    @JvmStatic
    fun commonInit() {
        YCLib.tryLoadInterop("hexparse") {
            CopyMaskParser.initSelf()
        }
    }
}