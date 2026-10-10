package com.elfmcys.yesstevemodel.network

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.access.ServerCommonPacketListenerImplAccessor
import com.elfmcys.yesstevemodel.network.message.*
import io.netty.util.AttributeKey
import net.minecraft.client.Minecraft
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import rip.ysm.api.network.PacketDirection
import rip.ysm.api.network.YSMChannel

object NetworkHandler {
    const val VERSION: String = "2.6.0"

    val CHANNEL_ID: Identifier = NameSpaces.MOD.path(VERSION.replace('.', '_'))

    val CHANNEL_VERSION_KEY: AttributeKey<String> = AttributeKey.valueOf("yes_steve_model_channel_version")

    var clientHandshakeComplete: Boolean = false

    fun setChannelVersion(connection: Connection, str: String): Boolean =
        connection.channel.attr(CHANNEL_VERSION_KEY).compareAndSet(null, str)

    fun markClientHandshakeComplete() {
        clientHandshakeComplete = true
    }

    fun resetClientHandshake() {
        clientHandshakeComplete = false
    }

    fun isPlayerConnected(serverPlayer: ServerPlayer): Boolean {
        return isConnectionValid((serverPlayer.connection as ServerCommonPacketListenerImplAccessor).`ysm$getConnection`())
    }

    fun isClientConnected(): Boolean {
        if (clientHandshakeComplete) {
            return true
        }
        val connection = Minecraft.getInstance().connection ?: return false
        return isConnectionValid(connection.connection)
    }

    fun isConnectionValid(connection: Connection?): Boolean =
        connection?.channel != null && VERSION == connection.channel.attr(CHANNEL_VERSION_KEY).get()

    init {
        YSMChannel.init(CHANNEL_ID, VERSION)
        YSMChannel.register(
            1,
            S2CModelSyncPayload::class.java,
            S2CModelSyncPayload::encode,
            S2CModelSyncPayload::decode,
            S2CModelSyncPayload::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            2,
            C2SModelSyncPayload::class.java,
            C2SModelSyncPayload::encode,
            C2SModelSyncPayload::decode,
            C2SModelSyncPayload::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            3,
            S2CExecuteMolangPacket::class.java,
            S2CExecuteMolangPacket::encode,
            S2CExecuteMolangPacket::decode,
            S2CExecuteMolangPacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            4,
            S2CSetModelAndTexturePacket::class.java,
            S2CSetModelAndTexturePacket::encode,
            S2CSetModelAndTexturePacket::decode,
            S2CSetModelAndTexturePacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            5,
            C2SRequestSwitchModelPacket::class.java,
            C2SRequestSwitchModelPacket::encode,
            C2SRequestSwitchModelPacket::decode,
            C2SRequestSwitchModelPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            6,
            S2CSyncAuthModelsPacket::class.java,
            S2CSyncAuthModelsPacket::encode,
            S2CSyncAuthModelsPacket::decode,
            S2CSyncAuthModelsPacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            7,
            C2SPlayAnimationPacket::class.java,
            C2SPlayAnimationPacket::encode,
            C2SPlayAnimationPacket::decode,
            C2SPlayAnimationPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            15,
            C2SCompleteFeedbackPacket::class.java,
            C2SCompleteFeedbackPacket::encode,
            C2SCompleteFeedbackPacket::decode,
            C2SCompleteFeedbackPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            16,
            S2CSyncProjectileModelPacket::class.java,
            S2CSyncProjectileModelPacket::encode,
            S2CSyncProjectileModelPacket::decode,
            S2CSyncProjectileModelPacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            17,
            C2SRequestExecuteMolangPacket::class.java,
            C2SRequestExecuteMolangPacket::encode,
            C2SRequestExecuteMolangPacket::decode,
            C2SRequestExecuteMolangPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            18,
            C2SSyncAnimationExpressionPacket::class.java,
            C2SSyncAnimationExpressionPacket::encode,
            C2SSyncAnimationExpressionPacket::decode,
            C2SSyncAnimationExpressionPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            19,
            S2CSyncAnimationExpressionPacket::class.java,
            S2CSyncAnimationExpressionPacket::encode,
            S2CSyncAnimationExpressionPacket::decode,
            S2CSyncAnimationExpressionPacket::handleCapability,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            21,
            S2CSyncPlayerStatePacket::class.java,
            S2CSyncPlayerStatePacket::encode,
            S2CSyncPlayerStatePacket::decode,
            S2CSyncPlayerStatePacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            22,
            S2CSyncVehicleModelPacket::class.java,
            S2CSyncVehicleModelPacket::encode,
            S2CSyncVehicleModelPacket::decode,
            S2CSyncVehicleModelPacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            23,
            C2SSwingArmPacket::class.java,
            C2SSwingArmPacket::encode,
            C2SSwingArmPacket::decode,
            C2SSwingArmPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            51,
            S2CVersionCheckPacket::class.java,
            S2CVersionCheckPacket::encode,
            S2CVersionCheckPacket::decode,
            S2CVersionCheckPacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            52,
            C2SVersionCheckPacket::class.java,
            C2SVersionCheckPacket::encode,
            C2SVersionCheckPacket::decode,
            C2SVersionCheckPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            70,
            C2SModelUploadStartPacket::class.java,
            C2SModelUploadStartPacket::encode,
            C2SModelUploadStartPacket::decode,
            C2SModelUploadStartPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            71,
            S2CModelUploadStartPacket::class.java,
            S2CModelUploadStartPacket::encode,
            S2CModelUploadStartPacket::decode,
            S2CModelUploadStartPacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
        YSMChannel.register(
            72,
            C2SModelUploadChunkPacket::class.java,
            C2SModelUploadChunkPacket::encode,
            C2SModelUploadChunkPacket::decode,
            C2SModelUploadChunkPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            73,
            C2SModelUploadFinishPacket::class.java,
            C2SModelUploadFinishPacket::encode,
            C2SModelUploadFinishPacket::decode,
            C2SModelUploadFinishPacket::handle,
            PacketDirection.PLAY_TO_SERVER
        )
        YSMChannel.register(
            74,
            S2CModelUploadResultPacket::class.java,
            S2CModelUploadResultPacket::encode,
            S2CModelUploadResultPacket::decode,
            S2CModelUploadResultPacket::handle,
            PacketDirection.PLAY_TO_CLIENT
        )
    }

    fun sendToServer(obj: Any) {
        if (isClientConnected()) {
            YSMChannel.sendToServer(obj)
        }
    }

    fun sendVersionCheck(connection: Connection?) {
        if (connection != null && connection.isConnected) {
            connection.send(toServerboundPacket(C2SVersionCheckPacket()))
        }
    }

    fun sendToClientPlayer(obj: Any, player: Player) {
        YSMChannel.sendToClientPlayer(obj, player as ServerPlayer)
    }

    fun sendToAll(obj: Any) {
        YSMChannel.sendToAll(obj)
    }

    fun sendToTrackingEntity(obj: Any, entity: Entity) {
        YSMChannel.sendToTrackingEntity(obj, entity)
    }

    fun sendToTrackingEntityAndSelf(obj: Any, player: Player) {
        YSMChannel.sendToTrackingEntityAndSelf(obj, player)
    }

    fun toClientboundPacket(obj: Any): Packet<*> {
        return YSMChannel.toClientboundPacket(obj)
    }

    fun toServerboundPacket(obj: Any): Packet<*> {
        return YSMChannel.toServerboundPacket(obj)
    }
}