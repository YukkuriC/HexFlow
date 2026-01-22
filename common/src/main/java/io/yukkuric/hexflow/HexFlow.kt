package io.yukkuric.hexflow

import com.mojang.logging.LogUtils
import io.yukkuric.hexflow.interop.hexparse.CopyMaskParser
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
        tryLoadInterop("hexparse") {
            CopyMaskParser.initSelf()
        }
    }

    private fun tryLoadInterop(modId: String, loadFunc: () -> Any) {
        if (!API.modLoaded(modId)) return
        try {
            loadFunc()
        } catch (e: Throwable) {
            LOGGER.error("error trying to load interop of $modId; error: ${e.stackTraceToString()}")
        }
    }

    lateinit var API: IAPI

    abstract class IAPI {
        init {
            API = this
        }

        abstract fun modLoaded(id: String): Boolean
    }
}