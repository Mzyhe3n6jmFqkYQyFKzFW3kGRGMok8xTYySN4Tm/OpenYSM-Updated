package com.elfmcys.yesstevemodel.command.subcommands

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.S2CVersionCheckPacket
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import rip.ysm.api.PlatformAPI

object PingCommand {
    private const val PING_NAME: String = "ping"

    @JvmStatic
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(PING_NAME).executes(::executePing)
    }

    @Throws(CommandSyntaxException::class)
    private fun executePing(context: CommandContext<CommandSourceStack>): Int {
        val playerOrException: ServerPlayer = context.source.playerOrException
        playerOrException.sendSystemMessage(
            Component.translatable(
                "message.yes_steve_model.client.ping_result",
                PlatformAPI.getModVersion(NameSpaces.MOD())
            )
        )
        if (!NetworkHandler.isPlayerConnected(playerOrException)) {
            NetworkHandler.sendToClientPlayer(S2CVersionCheckPacket(), playerOrException)
            return Command.SINGLE_SUCCESS
        }
        return Command.SINGLE_SUCCESS
    }
}