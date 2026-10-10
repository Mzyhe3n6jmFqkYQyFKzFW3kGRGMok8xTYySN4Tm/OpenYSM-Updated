package com.elfmcys.yesstevemodel.command.subcommands.client

import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object CacheCommand {
    fun register(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return LiteralArgumentBuilder.literal<FabricClientCommandSource>("cache")
            .then(
                LiteralArgumentBuilder.literal<FabricClientCommandSource>("dump")
                    .executes(::dumpCache)
            )
    }

    private fun dumpCache(context: CommandContext<FabricClientCommandSource>): Int {
        val player = Minecraft.getInstance().player ?: return 0
        player.displayClientMessage(
            YSMMessageFormatter.withPrefix(Component.literal("开始解析并导出客户端缓存模型...")),
            false
        )
        ClientModelManager.exportAllCachedModels("") { exportResult ->
            exportResult.message?.let {
                player.displayClientMessage(YSMMessageFormatter.withPrefix(it), false)
            }
            if (exportResult.success) {
                player.displayClientMessage(
                    Component.translatable(
                        "commands.yes_steve_model.export.success",
                        exportResult.filePath
                    ), false
                )
            }
        }
        return Command.SINGLE_SUCCESS
    }
}