package io.yukkuric.hexflow.helpers

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import io.netty.buffer.Unpooled
import net.minecraft.core.RegistryAccess
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

val Vec3.attachCenter
    get() = Vec3(0.5 + Mth.floor(x), 0.5 + Mth.floor(y), 0.5 + Mth.floor(z))

fun <T : Iota> T.deepCopy(regAccess: RegistryAccess): T {
    val buf = RegistryFriendlyByteBuf(Unpooled.buffer(), regAccess)
    try {
        IotaType.TYPED_STREAM_CODEC.encode(buf, this)
        buf.resetReaderIndex()
        val ret = IotaType.TYPED_STREAM_CODEC.decode(buf) as T
        buf.release()
        return ret
    } catch (e: Throwable) {
        buf.release()
        throw e
    }
}