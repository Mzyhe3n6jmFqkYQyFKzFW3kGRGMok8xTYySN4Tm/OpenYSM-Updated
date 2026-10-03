package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.capability.VehicleModelCapability
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import rip.ysm.api.network.PacketContext
import rip.ysm.compat.touhoulittlemaid.TouhouMaidCompat

@JvmRecord
data class C2SCompleteFeedbackPacket(val feedbackData: FeedbackData) {
    companion object {
        @JvmStatic
        fun encode(message: C2SCompleteFeedbackPacket, buf: FriendlyByteBuf) {
            FeedbackData.writeToBuf(message.feedbackData, buf)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SCompleteFeedbackPacket {
            return C2SCompleteFeedbackPacket(FeedbackData.readFromBuf(buf, false))
        }

        @JvmStatic
        fun handle(message: C2SCompleteFeedbackPacket, ctx: PacketContext) {
            val sender = ctx.sender
            if (ctx.isServerSide() && sender != null) {
                ctx.enqueueWork {
                    handleOnServer(message, sender.level() as ServerLevel)
                }
            }
        }

        @JvmStatic
        fun handleOnServer(message: C2SCompleteFeedbackPacket, serverLevel: ServerLevel) {
            val entity = serverLevel.getEntity(message.feedbackData.flags) ?: return
            if (TouhouMaidCompat.isMaidEntity(entity)) {
                TouhouMaidCompat.applyFeedback(entity, message.feedbackData)
            } else if (entity is ServerPlayer) {
                ModelInfoCapability[entity]?.let { cap ->
                    cap.applyFeedback(entity, message.feedbackData)
                    val vehicle = entity.vehicle
                    if (vehicle != null && vehicle.firstPassenger == entity) {
                        VehicleModelCapability[vehicle]?.let { vehicleCap ->
                            cap.getMolangVars()?.let { map ->
                                vehicleCap.setModel(cap.getModelId(), map)
                            }
                        }
                    }
                }
            }
        }
    }
}