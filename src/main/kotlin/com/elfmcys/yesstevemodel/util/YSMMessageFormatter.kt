package com.elfmcys.yesstevemodel.util

import dev.architectury.utils.GameInstance
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import rip.ysm.api.PlatformAPI

object YSMMessageFormatter {
    private const val PREFIX = "§6§l【§aYSM§6§l】§r"

    @JvmStatic
    fun withPrefix(component: Component): Component {
        return Component.literal(PREFIX).append(component)
    }

    @JvmStatic
    fun isCurrentClientPlayer(entity: Entity?): Boolean {
        return entity != null && !PlatformAPI.isServer() && entity.uuid == Minecraft.getInstance().user.profileId
    }

    private fun permissionFor(level: Int): Permission? {
        return when (level) {
            0 -> null
            1 -> Permissions.COMMANDS_MODERATOR
            2 -> Permissions.COMMANDS_GAMEMASTER
            3 -> Permissions.COMMANDS_ADMIN
            else -> Permissions.COMMANDS_OWNER
        }
    }

    @JvmStatic
    fun hasPermission(entity: Entity?, level: Int): Boolean {
        if (entity == null) {
            return false
        }
        val permission = permissionFor(level)
        return (entity is Player && (permission == null || entity.permissions().hasPermission(permission))) || isCurrentClientPlayer(entity)
    }

    @JvmStatic
    fun hasCommandPermission(commandSourceStack: CommandSourceStack, level: Int): Boolean {
        val permission = permissionFor(level)
        if (permission == null || commandSourceStack.permissions().hasPermission(permission)) {
            return true
        }
        return commandSourceStack.entity != null && isCurrentClientPlayer(commandSourceStack.entity)
    }

    @JvmStatic
    fun sendServerMessage(commandSourceStack: CommandSourceStack?, component: Component, broadcastToOps: Boolean) {
        val currentServer: MinecraftServer? = GameInstance.getServer() ?: return
        currentServer?.execute {
            var sourceStack: CommandSourceStack? = null
            val entity = commandSourceStack?.entity
            if (entity is ServerPlayer) {
                val player = currentServer.playerList.getPlayer(entity.uuid)
                if (player != null) {
                    sourceStack = player.createCommandSourceStack()
                }
            }
            if (sourceStack == null) {
                sourceStack = currentServer.createCommandSourceStack()
            }
            sourceStack.sendSuccess({ component }, broadcastToOps)
        }
    }
}
