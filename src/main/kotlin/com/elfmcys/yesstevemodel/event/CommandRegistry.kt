package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.command.OpenYSMClientCommand
import com.elfmcys.yesstevemodel.command.RootClientCommand
import com.elfmcys.yesstevemodel.command.RootCommand
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.SuggestionProviders
import rip.ysm.api.PlatformAPI

object CommandRegistry {
    @JvmField
    val MODEL_IDS: SuggestionProvider<CommandSourceStack> =
        SuggestionProviders.register(NameSpaces.MOD.path("models")) { commandContext, suggestionsBuilder ->
            if (commandContext.source is SharedSuggestionProvider) {
                if (PlatformAPI.isServer()) {
                    return@register SharedSuggestionProvider.suggest(
                        ServerModelManager.getServerModelInfo().keys.map(::escapeIfRequired),
                        suggestionsBuilder
                    )
                }
                return@register SharedSuggestionProvider.suggest(
                    ClientModelManager.getModelAssemblyMap().keys.map(::escapeIfRequired),
                    suggestionsBuilder
                )
            }
            Suggestions.empty()
        }

    @JvmField
    val ANIMATION_NAMES: SuggestionProvider<CommandSourceStack> =
        SuggestionProviders.register(NameSpaces.MOD.path("animations")) { commandContext, suggestionsBuilder ->
            if (commandContext.source is SharedSuggestionProvider) {
                if (PlatformAPI.isServer()) {
                    return@register Suggestions.empty()
                }
                val map = ClientModelManager.getLocalModelContext().animationBundle.mainAnimations
                val set = (map.keys.map(::escapeIfRequired) + "stop").toSet()
                return@register SharedSuggestionProvider.suggest(set, suggestionsBuilder)
            }
            Suggestions.empty()
        }

    @JvmField
    val TEXTURE_IDS: SuggestionProvider<CommandSourceStack> =
        SuggestionProviders.register(NameSpaces.MOD.path("textures")) { commandContext, suggestionsBuilder ->
            if (commandContext.source is SharedSuggestionProvider) {
                val str = commandContext.getArgument("model_id", String::class.java)
                if (PlatformAPI.isServer()) {
                    ServerModelManager.getServerModelInfo()[str]?.let { serverModelInfo ->
                        val list = mutableListOf("-").apply {
                            addAll(serverModelInfo.modelInfo.textures.map(::escapeIfRequired))
                        }
                        return@register SharedSuggestionProvider.suggest(list, suggestionsBuilder)
                    }
                } else if (ClientModelManager.getModelAssemblyMap().containsKey(str)) {
                    val list = mutableListOf("-")
                    ClientModelManager.getModelContext(str)?.let { context ->
                        list.addAll(context.animationBundle.textures.keys.map(::escapeIfRequired))
                    }
                    return@register SharedSuggestionProvider.suggest(list, suggestionsBuilder)
                }
            }
            Suggestions.empty()
        }

    private fun escapeIfRequired(str: String): String {
        if (str.all { StringReader.isAllowedInUnquotedString(it) }) {
            return str
        }
        return "\"${str.replace("\"", "\\\"").replace("'", "\\'")}\""
    }

    init {
        if (!PlatformAPI.isServer()) {
            ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
                if (!YesSteveModel.isAvailable()) {
                    return@ClientCommandRegistrationCallback
                }
                OpenYSMClientCommand.registerClientCommands(dispatcher)
            })
        }
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            if (!YesSteveModel.isAvailable()) {
                RootCommand.registerFallbackCommands(dispatcher)
                return@CommandRegistrationCallback
            }
            RootCommand.registerCommands(dispatcher)
            if (!PlatformAPI.isServer()) {
                RootClientCommand.registerClientCommands(dispatcher)
            }
        })
    }
}
