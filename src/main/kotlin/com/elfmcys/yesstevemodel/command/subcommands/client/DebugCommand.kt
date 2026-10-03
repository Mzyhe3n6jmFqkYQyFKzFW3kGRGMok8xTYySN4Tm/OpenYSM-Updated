package com.elfmcys.yesstevemodel.command.subcommands.client

import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument

object DebugCommand {
    const val DEBUG_NAME: String = "debug"
    const val ARG_NAME: String = "target"

    @JvmStatic
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(DEBUG_NAME)
            .then(
                Commands.argument(ARG_NAME, EntityArgument.entity())
                    .executes(::debugEntity)
            )
    }

    @Throws(CommandSyntaxException::class)
    private fun debugEntity(context: CommandContext<CommandSourceStack>): Int {
        val level = Minecraft.getInstance().level ?: return 0
        val targetEntity = level.getEntity(EntityArgument.getEntity(context, ARG_NAME).id) ?: return 0
        if (AnimationDebugOverlay.tryUpdateFromEntity(targetEntity)) {
            return Command.SINGLE_SUCCESS
        }
        return 0
    }
}