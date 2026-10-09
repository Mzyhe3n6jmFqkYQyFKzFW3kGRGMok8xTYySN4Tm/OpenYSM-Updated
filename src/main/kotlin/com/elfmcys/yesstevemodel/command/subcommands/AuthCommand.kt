package com.elfmcys.yesstevemodel.command.subcommands

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.event.CommandRegistry
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.model.ServerModelSelection
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.S2CSyncAuthModelsPacket
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

// TODO: Register command in kotlin way
object AuthCommand {
    private const val AUTH_NAME: String = "auth"
    private const val ADD_NAME: String = "add"
    private const val REMOVE_NAME: String = "remove"
    private const val ALL_NAME: String = "all"
    private const val CLEAR_NAME: String = "clear"
    private const val TARGETS_NAME: String = "targets"
    private const val MODEL_ID_NAME: String = "model_id"

    @JvmStatic
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val auth: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(AUTH_NAME)
            .requires { commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2) }
        val targets: RequiredArgumentBuilder<CommandSourceStack, EntitySelector> =
            Commands.argument(TARGETS_NAME, EntityArgument.players())
        val modelId: RequiredArgumentBuilder<CommandSourceStack, String> =
            Commands.argument(MODEL_ID_NAME, StringArgumentType.string())
                .suggests(CommandRegistry.MODEL_IDS)

        auth.then(targets.then(Commands.literal(ADD_NAME).then(modelId.executes(::addAuthModel))))
        auth.then(targets.then(Commands.literal(REMOVE_NAME).then(modelId.executes(::removeAuthModel))))
        auth.then(targets.then(Commands.literal(ALL_NAME).executes(::addAllAuthModel)))
        auth.then(targets.then(Commands.literal(CLEAR_NAME).executes(::executeClear)))
        return auth
    }

    @Throws(CommandSyntaxException::class)
    private fun addAuthModel(context: CommandContext<CommandSourceStack>): Int {
        val targets: Collection<ServerPlayer> = EntityArgument.getPlayers(context, TARGETS_NAME)
        val string: String = StringArgumentType.getString(context, MODEL_ID_NAME)
        if (!ServerModelManager.serverModelInfo.containsKey(string)) {
            context.source.sendSuccess(
                { Component.translatable("commands.yes_steve_model.export.not_exist", string) },
                true
            )
            return Command.SINGLE_SUCCESS
        }
        targets.forEach { player ->
            ServerModelSelection.addAuthModel(player.uuid, string)
            val authModels = ServerModelSelection.getAuthModels(player.uuid).toMutableSet()
            NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(authModels), player)
            context.source.sendSuccess({
                Component.translatable(
                    "commands.yes_steve_model.auth_model.add.info",
                    string,
                    player.scoreboardName
                )
            }, true)
        }
        return Command.SINGLE_SUCCESS
    }

    @Throws(CommandSyntaxException::class)
    private fun addAllAuthModel(context: CommandContext<CommandSourceStack>): Int {
        val targets = EntityArgument.getPlayers(context, TARGETS_NAME)
        targets.forEach { player ->
            val setKeySet = ServerModelManager.serverModelInfo.keys
            ServerModelSelection.addAllAuthModels(player.uuid, setKeySet)
            val authModels = ServerModelSelection.getAuthModels(player.uuid).toMutableSet()
            NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(authModels), player)
            context.source.sendSuccess({
                Component.translatable(
                    "commands.yes_steve_model.auth_model.all.info",
                    player.scoreboardName
                )
            }, true)
        }
        return Command.SINGLE_SUCCESS
    }

    @Throws(CommandSyntaxException::class)
    private fun removeAuthModel(context: CommandContext<CommandSourceStack>): Int {
        val targets: Collection<ServerPlayer> = EntityArgument.getPlayers(context, TARGETS_NAME)
        val modelName: String = StringArgumentType.getString(context, MODEL_ID_NAME)
        targets.forEach { player ->
            ServerModelSelection.removeAuthModel(player.uuid, modelName)
            val authModels = ServerModelSelection.getAuthModels(player.uuid).toMutableSet()
            ModelInfoCapability[player]?.let { modelIdCap ->
                if (ServerModelManager.authModels
                        .contains(modelIdCap.modelId) && !authModels.contains(modelIdCap.modelId)
                ) {
                    modelIdCap.resetToDefault()
                    ServerModelSelection.savePlayerSelection(
                        player.uuid,
                        modelIdCap.modelId,
                        modelIdCap.selectTexture
                    )
                }
            }
            NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(authModels), player)
            context.source.sendSuccess({
                Component.translatable(
                    "commands.yes_steve_model.auth_model.remove.info",
                    modelName,
                    player.scoreboardName
                )
            }, true)
        }
        return Command.SINGLE_SUCCESS
    }

    @Throws(CommandSyntaxException::class)
    private fun executeClear(context: CommandContext<CommandSourceStack>): Int {
        EntityArgument.getPlayers(context, TARGETS_NAME).forEach { player ->
            ServerModelSelection.clearAuthModels(player.uuid)
            ModelInfoCapability[player]?.let { modelIdCap ->
                if (ServerModelManager.authModels.contains(modelIdCap.modelId)) {
                    modelIdCap.resetToDefault()
                    ServerModelSelection.savePlayerSelection(
                        player.uuid,
                        modelIdCap.modelId,
                        modelIdCap.selectTexture
                    )
                }
            }
            NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(mutableSetOf()), player)
            context.source.sendSuccess({
                Component.translatable(
                    "commands.yes_steve_model.auth_model.clear.info",
                    player.scoreboardName
                )
            }, true)
        }
        return Command.SINGLE_SUCCESS
    }
}