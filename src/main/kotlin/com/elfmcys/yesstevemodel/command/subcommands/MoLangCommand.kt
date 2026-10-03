package com.elfmcys.yesstevemodel.command.subcommands

import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.S2CExecuteMolangPacket
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object MoLangCommand {
    private const val MOLANG_NAME: String = "molang"
    private const val EXECUTE_NAME: String = "execute"
    private const val EXP_NAME: String = "exp"
    private const val TARGETS_NAME: String = "targets"

    @JvmStatic
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val molang: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(MOLANG_NAME)
            .requires { commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2) }
        molang.then(
            Commands.literal(EXECUTE_NAME).then(
                Commands.argument(TARGETS_NAME, EntityArgument.players()).then(
                    Commands.argument(EXP_NAME, StringArgumentType.greedyString()).executes(::executeMoLang)
                )
            )
        )
        return molang
    }

    @Throws(CommandSyntaxException::class)
    private fun executeMoLang(context: CommandContext<CommandSourceStack>): Int {
        return sendMoLangToPlayers(StringArgumentType.getString(context, EXP_NAME), EntityArgument.getPlayers(context, TARGETS_NAME))
    }

    private fun sendMoLangToPlayers(str: String, collection: Collection<ServerPlayer>): Int {
        val ids = collection.map { it.id }.toIntArray()
        NetworkHandler.sendToAll(S2CExecuteMolangPacket(ids, str))
        return Command.SINGLE_SUCCESS
    }
}