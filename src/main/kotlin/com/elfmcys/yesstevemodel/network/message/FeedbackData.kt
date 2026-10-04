package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import it.unimi.dsi.fastutil.ints.Int2FloatArrayMap
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap
import net.minecraft.network.FriendlyByteBuf

data class FeedbackData(
    val entityId: Int,
    val stringValues: Object2FloatArrayMap<String>?,
    val intValues: Int2FloatArrayMap?,
    val flags: Int
) {
    companion object {
        @JvmStatic
        fun writeToBuf(message: FeedbackData, buf: FriendlyByteBuf) {
            buf.writeInt(message.entityId)
            buf.writeVarInt(message.flags)
            val strValues = message.stringValues
            if (strValues != null) {
                buf.writeByte(strValues.size)
                strValues.object2FloatEntrySet().fastForEach { entry ->
                    buf.writeUtf(entry.key)
                    buf.writeFloat(entry.floatValue)
                }
            } else {
                buf.writeByte(0)
            }
        }

        @JvmStatic
        fun readFromBuf(buf: FriendlyByteBuf, useInternedKeys: Boolean): FeedbackData {
            val entityId = buf.readInt()
            val varInt = buf.readVarInt()
            val entryCount = buf.readByte().toInt()
            val object2FloatArrayMap: Object2FloatArrayMap<String>?
            val int2FloatArrayMap: Int2FloatArrayMap?
            if (useInternedKeys) {
                val iArr = IntArray(entryCount)
                val fArr = FloatArray(entryCount)
                for (i in 0 until entryCount) {
                    iArr[i] = StringPool.computeIfAbsent(buf.readUtf())
                    fArr[i] = buf.readFloat()
                }
                int2FloatArrayMap = Int2FloatArrayMap(iArr, fArr)
                object2FloatArrayMap = null
            } else {
                val strArr = Array(entryCount) { buf.readUtf() }
                val fArr2 = FloatArray(entryCount) { buf.readFloat() }
                object2FloatArrayMap = Object2FloatArrayMap(strArr, fArr2)
                int2FloatArrayMap = null
            }
            return FeedbackData(entityId, object2FloatArrayMap, int2FloatArrayMap, varInt)
        }
    }
}