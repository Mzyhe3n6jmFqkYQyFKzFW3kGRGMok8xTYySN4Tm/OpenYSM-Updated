package com.elfmcys.yesstevemodel.command

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.command.subcommands.*
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object RootCommand {
    private const val ROOT_NAME: String = "ysm"

    @JvmStatic
    fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val root = Commands.literal(ROOT_NAME)
        root.then(ModelCommand.register())
        root.then(AuthCommand.register())
        root.then(ExportCommand.register())
        root.then(PlayAnimationCommand.register())
        root.then(MoLangCommand.register())
        root.then(PingCommand.register())
        dispatcher.register(root)
    }

    @JvmStatic
    fun registerFallbackCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val root: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(ROOT_NAME)
        root.then(Commands.argument("any", StringArgumentType.greedyString()).executes { commandContext ->
            if (commandContext.source.isPlayer) {
                YesSteveModel.getUnavailableComponent()?.let { commandContext.source.sendSystemMessage(it) }
                return@executes 1
            }
            YesSteveModel.getErrorMessage()?.let { commandContext.source.sendSystemMessage(Component.literal(it)) }
            1
        })
        dispatcher.register(root)
    }
}