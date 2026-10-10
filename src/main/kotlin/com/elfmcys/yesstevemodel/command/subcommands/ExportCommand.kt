package com.elfmcys.yesstevemodel.command.subcommands

import com.elfmcys.yesstevemodel.event.CommandRegistry
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object ExportCommand {
    private const val EXPORT_NAME: String = "export"
    private const val MODEL_ID_NAME: String = "model_id"
    private const val EXTRA_NAME: String = "extra"

    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val export: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(EXPORT_NAME)
            .requires { commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2) }
        val modelId: RequiredArgumentBuilder<CommandSourceStack, String> =
            Commands.argument(MODEL_ID_NAME, StringArgumentType.string())
                .suggests(CommandRegistry.MODEL_IDS)
        val extra: RequiredArgumentBuilder<CommandSourceStack, String> =
            Commands.argument(EXTRA_NAME, StringArgumentType.greedyString())
        export.then(modelId.executes(::executeExport))
        export.then(modelId.then(extra.executes(::executeExportWithExtra)))
        return export
    }

    private fun executeExport(context: CommandContext<CommandSourceStack>): Int {
        handleExport(context.source, StringArgumentType.getString(context, MODEL_ID_NAME), null)
        return Command.SINGLE_SUCCESS
    }

    private fun executeExportWithExtra(context: CommandContext<CommandSourceStack>): Int {
        handleExport(
            context.source,
            StringArgumentType.getString(context, MODEL_ID_NAME),
            StringArgumentType.getString(context, EXTRA_NAME)
        )
        return Command.SINGLE_SUCCESS
    }

    private fun handleExport(sourceStack: CommandSourceStack, str: String, str2: String?) {
        ServerModelManager.nativeExportModel(str, str2) { exportResult ->
            exportResult.message?.let {
                YSMMessageFormatter.sendServerMessage(sourceStack, YSMMessageFormatter.withPrefix(it), false)
            }
            if (exportResult.success) {
                YSMMessageFormatter.sendServerMessage(
                    sourceStack,
                    Component.translatable("commands.yes_steve_model.export.success", exportResult.filePath),
                    false
                )
            }
        }
    }
}