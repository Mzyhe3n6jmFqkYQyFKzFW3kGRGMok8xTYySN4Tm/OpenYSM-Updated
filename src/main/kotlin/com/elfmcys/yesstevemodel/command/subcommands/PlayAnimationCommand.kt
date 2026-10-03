package com.elfmcys.yesstevemodel.command.subcommands

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.event.CommandRegistry
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

object PlayAnimationCommand {
    private const val PLAY_NAME: String = "play"
    private const val TARGETS_NAME: String = "targets"
    private const val ANIMATION_NAME: String = "animation"
    const val STOP: String = "stop"

    @JvmStatic
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val play: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(PLAY_NAME)
            .requires { commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2) }
        play.then(
            Commands.argument(TARGETS_NAME, EntityArgument.players()).then(
                Commands.argument(ANIMATION_NAME, StringArgumentType.string())
                    .suggests(CommandRegistry.ANIMATION_NAMES)
                    .executes(::playAnimation)
            )
        )
        return play
    }

    @Throws(CommandSyntaxException::class)
    private fun playAnimation(context: CommandContext<CommandSourceStack>): Int {
        val players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, TARGETS_NAME)
        val animation: String = StringArgumentType.getString(context, ANIMATION_NAME)
        players.forEach { player ->
            ModelInfoCapability[player]?.let { cap ->
                if (animation == STOP) {
                    cap.stopAnimation(player)
                } else {
                    cap.playAnimation(player, animation)
                }
            }
        }
        return Command.SINGLE_SUCCESS
    }
}