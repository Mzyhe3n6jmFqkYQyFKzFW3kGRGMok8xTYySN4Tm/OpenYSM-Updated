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

object WatchCommand {
    private const val WATCH_NAME: String = "watch"
    private const val VAR_NAME: String = "var"
    private const val STATE_NAME: String = "state"
    private const val CLEAR_NAME: String = "clear"
    private const val EXP_NAME: String = "exp"
    private const val CONTROLLER_NAME: String = "controller"

    @JvmStatic
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val watch: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(WATCH_NAME)
        val varLiteral: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(VAR_NAME)
        val state: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(STATE_NAME)
        val clear: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(CLEAR_NAME)

        val exp = {
            Commands.argument(EXP_NAME, StringArgumentType.greedyString())
                .suggests(RootClientCommand.VARS_SUGGESTION_PROVIDER)
        }
        val controller = {
            Commands.argument(CONTROLLER_NAME, StringArgumentType.greedyString())
                .suggests(RootClientCommand.CONTROLLERS_SUGGESTION_PROVIDER)
        }

        watch.then(varLiteral.then(exp().executes(::watchVar)))
        watch.then(state.then(controller().executes(::watchState)))
        watch.then(clear.executes(::watchClear))
        return watch
    }

    private fun watchVar(context: CommandContext<CommandSourceStack>): Int {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS
        }
        val minecraft: Minecraft = Minecraft.getInstance()
        val string: String = StringArgumentType.getString(context, EXP_NAME)
        runCatching {
            GeckoLibCache.parseSimpleExpression(string)
        }.onSuccess { value: IValue ->
            minecraft.execute {
                val player = minecraft.player ?: return@execute
                PlayerCapability[player]?.let {
                    AnimationDebugOverlay.getMolangWatch()
                        .addWatch(MolangWatchRegistry.EvaluationPhase.POST_ANIMATION, string, value)
                    if (!AnimationDebugOverlay.isDebugActive()) {
                        AnimationDebugOverlay.tryUpdateFromLocalPlayer()
                    }
                }
            }
        }.onFailure {
            if (it is ParseException) {
                context.source.sendFailure(
                    Component.translatable(
                        "message.yes_steve_model.model.debug_animation.parser_error",
                        it.message ?: ""
                    )
                )
            }
        }
        return Command.SINGLE_SUCCESS
    }

    private fun watchClear(context: CommandContext<CommandSourceStack>): Int {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS
        }
        val minecraft: Minecraft = Minecraft.getInstance()
        minecraft.execute {
            val player = minecraft.player ?: return@execute
            PlayerCapability[player]?.let {
                AnimationDebugOverlay.getMolangWatch().clearAll()
            }
        }
        AnimationDebugOverlay.clearDebugLines()
        return Command.SINGLE_SUCCESS
    }

    private fun watchState(context: CommandContext<CommandSourceStack>): Int {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS
        }
        AnimationDebugOverlay.addDebugLine(StringArgumentType.getString(context, CONTROLLER_NAME))
        if (!AnimationDebugOverlay.isDebugActive()) {
            AnimationDebugOverlay.tryUpdateFromLocalPlayer()
            return Command.SINGLE_SUCCESS
        }
        return Command.SINGLE_SUCCESS
    }

    private fun isClientSide(): Boolean {
        return Minecraft.getInstance().player != null
    }
}