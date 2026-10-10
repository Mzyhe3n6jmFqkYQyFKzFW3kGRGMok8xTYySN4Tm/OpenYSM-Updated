package com.elfmcys.yesstevemodel.command.subcommands

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.event.CommandRegistry
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.model.ServerModelSelection
import com.elfmcys.yesstevemodel.model.format.ServerModelData
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
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
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.StopWatch
import rip.ysm.api.PlatformAPI
import java.util.concurrent.TimeUnit

object ModelCommand {
    @JvmField
    val GSON: Gson = GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create()

    private const val MODEL_NAME: String = "model"
    private const val LITERAL_RELOAD: String = "reload"
    private const val SET_NAME: String = "set"
    private const val DISABLE_NAME: String = "disable"
    private const val TARGETS_NAME: String = "targets"
    private const val MODEL_ID_NAME: String = "model_id"
    private const val TEXTURE_ID_NAME: String = "texture_id"
    private const val IGNORE_AUTH_NAME: String = "ignore_auth"
    private const val PLAYERS_NAME: String = "players"
    private const val ARG_VALUE: String = "value"

    @JvmStatic
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val model: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(MODEL_NAME)
            .requires { commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2) }
        model.then(Commands.literal(LITERAL_RELOAD).executes(::reloadAllPack))
        model.then(
            Commands.literal(DISABLE_NAME).then(
                Commands.argument(PLAYERS_NAME, EntityArgument.players())
                    .then(Commands.argument(ARG_VALUE, BoolArgumentType.bool()).executes(::disableModel))
            )
        )
        val set: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(SET_NAME)
        val targets: RequiredArgumentBuilder<CommandSourceStack, EntitySelector> =
            Commands.argument(TARGETS_NAME, EntityArgument.players())
        val modelId: RequiredArgumentBuilder<CommandSourceStack, String> =
            Commands.argument(MODEL_ID_NAME, StringArgumentType.string())
                .suggests(CommandRegistry.MODEL_IDS)
        val textureId: RequiredArgumentBuilder<CommandSourceStack, String> =
            Commands.argument(TEXTURE_ID_NAME, StringArgumentType.string())
                .suggests(CommandRegistry.TEXTURE_IDS)
        val ignoreAuth: RequiredArgumentBuilder<CommandSourceStack, Boolean> =
            Commands.argument(IGNORE_AUTH_NAME, BoolArgumentType.bool())

        model.then(set.then(targets.then(modelId.then(textureId.executes { commandContext ->
            setModel(
                commandContext,
                false
            )
        }))))
        model.then(set.then(targets.then(modelId.then(textureId.then(ignoreAuth.executes(::setModelIgnoreAuth))))))
        return model
    }

    @Throws(CommandSyntaxException::class)
    private fun setModelIgnoreAuth(context: CommandContext<CommandSourceStack>): Int {
        return setModel(context, BoolArgumentType.getBool(context, IGNORE_AUTH_NAME))
    }

    @Throws(CommandSyntaxException::class)
    fun setModel(context: CommandContext<CommandSourceStack>, ignoreAuth: Boolean): Int {
        val targets: Collection<ServerPlayer> = EntityArgument.getPlayers(context, TARGETS_NAME)
        val modelName: String = StringArgumentType.getString(context, MODEL_ID_NAME)
        var textureName: String = StringArgumentType.getString(context, TEXTURE_ID_NAME)
        val info: ServerModelData? = ServerModelManager.serverModelInfo[modelName]
        if (info == null) {
            context.source.sendSuccess({
                Component.translatable(
                    "commands.yes_steve_model.export.not_exist",
                    modelName
                )
            }, true)
            return Command.SINGLE_SUCCESS
        }
        if (textureName == "-") {
            textureName = info.loadedModelData.modelProperties.defaultTexture
            if (StringUtils.isBlank(textureName) || !info.modelInfo.textures.contains(textureName)) {
                textureName = if (info.modelInfo.textures.isEmpty()) "" else info.modelInfo.textures[0]
            }
        }
        if (info.modelInfo.textures.isEmpty()) {
            return Command.SINGLE_SUCCESS
        }
        val finalTextureName: String = textureName
        if (ignoreAuth) {
            targets.forEach { player ->
                ModelInfoCapability[player]?.let { cap ->
                    cap.setModelAndTexture(modelName, finalTextureName)
                    cap.setMandatory(true)
                    ServerModelSelection.savePlayerSelection(player.uuid, modelName, finalTextureName)
                    context.source.sendSuccess({
                        Component.translatable(
                            "message.yes_steve_model.model.set.success",
                            modelName,
                            player.scoreboardName
                        )
                    }, true)
                }
            }
            return Command.SINGLE_SUCCESS
        }
        targets.forEach { player ->
            ModelInfoCapability[player]?.let { cap ->
                if (!ServerModelManager.authModels.contains(modelName) || ServerModelSelection.hasAuthModel(
                        player.uuid,
                        modelName
                    )
                ) {
                    cap.setModelAndTexture(modelName, finalTextureName)
                    cap.setMandatory(true)
                    ServerModelSelection.savePlayerSelection(player.uuid, modelName, finalTextureName)
                    context.source.sendSuccess({
                        Component.translatable(
                            "message.yes_steve_model.model.set.success",
                            modelName,
                            player.scoreboardName
                        )
                    }, true)
                    return@let
                }
                context.source.sendSuccess({
                    Component.translatable(
                        "message.yes_steve_model.model.set.need_auth",
                        modelName,
                        player.scoreboardName
                    )
                }, true)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    private fun reloadAllPack(context: CommandContext<CommandSourceStack>): Int {
        context.source.sendSuccess({ Component.translatable("message.yes_steve_model.model.reload.start") }, true)
        val watch: StopWatch = StopWatch.createStarted()
        if (!ServerModelManager.loadModels({ result ->
                result.errorMessage?.let {
                    YSMMessageFormatter.sendServerMessage(context.source, YSMMessageFormatter.withPrefix(it), true)
                }
                if (result.isSuccess) {
                    YSMMessageFormatter.sendServerMessage(
                        context.source,
                        Component.translatable(
                            "message.yes_steve_model.model.reload.complete",
                            watch.getTime(TimeUnit.MICROSECONDS) / 1000.0
                        ),
                        true
                    )
                    watch.reset()
                    watch.start()
                }
            }, { data ->
                watch.stop()
                if (!data.isEnabled) {
                    data.displayComponent?.let { comp ->
                        YSMMessageFormatter.sendServerMessage(
                            context.source,
                            YSMMessageFormatter.withPrefix(comp),
                            true
                        )
                    }
                    return@loadModels
                }
                val map = data.uuidComponentMap
                if (!map.isNullOrEmpty()) {
                    for (component in map.values) {
                        YSMMessageFormatter.sendServerMessage(
                            context.source,
                            YSMMessageFormatter.withPrefix(component),
                            true
                        )
                    }
                    if (PlatformAPI.isServer) {
                        YSMMessageFormatter.sendServerMessage(
                            context.source,
                            Component.translatable(
                                "message.yes_steve_model.model.sync.complete",
                                watch.getTime(TimeUnit.MICROSECONDS) / 1000.0
                            ),
                            true
                        )
                    }
                }
            })) {
            context.source.sendFailure(Component.translatable("message.yes_steve_model.model.reload.in_progress"))
            return Command.SINGLE_SUCCESS
        }
        return Command.SINGLE_SUCCESS
    }

    @Throws(CommandSyntaxException::class)
    private fun disableModel(context: CommandContext<CommandSourceStack>): Int {
        val targets: Collection<ServerPlayer> = EntityArgument.getPlayers(context, PLAYERS_NAME)
        val bool: Boolean = BoolArgumentType.getBool(context, ARG_VALUE)
        val strKey =
            if (bool) "message.yes_steve_model.model.disable.true" else "message.yes_steve_model.model.disable.false"
        targets.forEach { player ->
            ModelInfoCapability[player]?.let { cap ->
                cap.setDisabled(bool)
                context.source.sendSuccess({ Component.translatable(strKey, player.scoreboardName) }, true)
            }
        }
        return Command.SINGLE_SUCCESS
    }
}