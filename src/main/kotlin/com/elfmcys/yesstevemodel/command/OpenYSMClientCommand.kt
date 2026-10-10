package com.elfmcys.yesstevemodel.command

import com.elfmcys.yesstevemodel.command.subcommands.client.CacheCommand
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object OpenYSMClientCommand {
    fun registerClientCommands(commandDispatcher: CommandDispatcher<FabricClientCommandSource>) {
        val root = LiteralArgumentBuilder.literal<FabricClientCommandSource>("openysm")
            .requires { true }
        root.then(CacheCommand.register())
        commandDispatcher.register(root)
    }
}