package com.elfmcys.yesstevemodel.command.subcommands.client

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.animation.molang.MolangWatchRegistry
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay
import com.elfmcys.yesstevemodel.command.RootClientCommand
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.molang.parser.ParseException
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object MoLangCommand {
    private const val MOLANG_NAME: String = "molang"
    private const val WATCH_NAME: String = "watch"
    private const val ADD_NAME: String = "add"
    private const val PRE_NAME: String = "pre"
    private const val POST_NAME: String = "post"
    private const val CLEAR_NAME: String = "clear"
    private const val REMOVE_NAME: String = "remove"
    private const val EXP_NAME_NAME: String = "exp_name"
    private const val EXP_NAME: String = "exp"
    private const val EXECUTE_NAME: String = "execute"

    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val molang = Commands.literal(MOLANG_NAME)
        val watch = Commands.literal(WATCH_NAME)
        val add = Commands.literal(ADD_NAME)
        val pre = Commands.literal(PRE_NAME)
        val post = Commands.literal(POST_NAME)
        val clear = Commands.literal(CLEAR_NAME)
        val remove = Commands.literal(REMOVE_NAME)
        val execute = Commands.literal(EXECUTE_NAME)

        val expName = { Commands.argument(EXP_NAME_NAME, StringArgumentType.string()) }
        val exp = {
            Commands.argument(EXP_NAME, StringArgumentType.greedyString())
                .suggests(RootClientCommand.VARS_SUGGESTION_PROVIDER)
        }

        watch.then(
            add.then(
                pre.then(
                    expName().then(
                        exp().executes { commandContext ->
                            addWatch(
                                commandContext,
                                MolangWatchRegistry.EvaluationPhase.PRE_ANIMATION
                            )
                        }
                    )
                )
            ).then(
                post.then(
                    expName().then(
                        exp().executes { commandContext2 ->
                            addWatch(
                                commandContext2,
                                MolangWatchRegistry.EvaluationPhase.POST_ANIMATION
                            )
                        }
                    )
                )
            )
        ).then(remove.then(expName().executes(::removeWatch))).then(clear.executes(::clearWatch))

        molang.then(watch).then(execute.then(exp().executes(::executeExperssion)))
        return molang
    }

    fun addWatch(context: CommandContext<CommandSourceStack>, watchRegistry: MolangWatchRegistry.EvaluationPhase): Int {
        if (!isClientSide()) return Command.SINGLE_SUCCESS
        val string = StringArgumentType.getString(context, EXP_NAME_NAME)
        runCatching {
            GeckoLibCache.parseSimpleExpression(StringArgumentType.getString(context, EXP_NAME))
        }.onSuccess { value: IValue ->
            Minecraft.getInstance().execute {
                val player = Minecraft.getInstance().player ?: return@execute
                PlayerCapability[player]?.let {
                    AnimationDebugOverlay.molangWatch.addWatch(watchRegistry, string, value)
                }
            }
        }.onFailure { e ->
            if (e is ParseException) {
                context.source.sendFailure(
                    Component.translatable(
                        "message.yes_steve_model.model.debug_animation.parser_error",
                        e.message ?: ""
                    )
                )
            }
        }
        return Command.SINGLE_SUCCESS
    }

    private fun removeWatch(context: CommandContext<CommandSourceStack>): Int {
        if (!isClientSide()) return Command.SINGLE_SUCCESS
        val string = StringArgumentType.getString(context, EXP_NAME_NAME)
        Minecraft.getInstance().execute {
            val player = Minecraft.getInstance().player ?: return@execute
            PlayerCapability[player]?.let {
                AnimationDebugOverlay.molangWatch.removeWatch(string)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    private fun clearWatch(context: CommandContext<CommandSourceStack>): Int {
        if (!isClientSide()) return Command.SINGLE_SUCCESS
        Minecraft.getInstance().execute {
            val player = Minecraft.getInstance().player ?: return@execute
            PlayerCapability[player]?.let {
                AnimationDebugOverlay.molangWatch.clearAll()
            }
        }
        return Command.SINGLE_SUCCESS
    }

    private fun executeExperssion(context: CommandContext<CommandSourceStack>): Int {
        if (!isClientSide()) return Command.SINGLE_SUCCESS
        runCatching {
            GeckoLibCache.parseSimpleExpression(StringArgumentType.getString(context, EXP_NAME))
        }.onSuccess { value: IValue ->
            var geoEntity = AnimationDebugOverlay.activeModel
            if (geoEntity == null) {
                val player = Minecraft.getInstance().player
                if (player != null) {
                    geoEntity = PlayerCapability[player]
                }
            }
            geoEntity?.executeExpression(value, true, false) { str ->
                Minecraft.getInstance().player?.displayClientMessage(
                    Component.translatable(
                        "message.yes_steve_model.model.debug_animation.result",
                        str
                    ), false
                )
            }
        }.onFailure { e ->
            if (e is ParseException) {
                context.source.sendFailure(
                    Component.translatable(
                        "message.yes_steve_model.model.debug_animation.parser_error",
                        e.message ?: ""
                    )
                )
            }
        }
        return Command.SINGLE_SUCCESS
    }

    private fun isClientSide(): Boolean {
        return Minecraft.getInstance().player != null
    }
}