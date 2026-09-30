package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.command.OpenYSMClientCommand
import com.elfmcys.yesstevemodel.command.RootClientCommand
import com.elfmcys.yesstevemodel.command.RootCommand
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.SuggestionProviders
import net.minecraft.resources.Identifier
import rip.ysm.api.PlatformAPI
import java.util.stream.Collectors

object CommandRegistry {
    @JvmField
    val MODEL_IDS: SuggestionProvider<CommandSourceStack> = SuggestionProviders.register(
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "models")
    ) { commandContext, suggestionsBuilder ->
        if (commandContext.source is SharedSuggestionProvider) {
            if (PlatformAPI.isServer()) {
                return@register SharedSuggestionProvider.suggest(
                    ServerModelManager.getServerModelInfo().keys.stream().map { escapeIfRequired(it) }.toList(),
                    suggestionsBuilder
                )
            }
            return@register SharedSuggestionProvider.suggest(
                ClientModelManager.getModelAssemblyMap().keys.stream().map { escapeIfRequired(it) }.toList(),
                suggestionsBuilder
            )
        }
        Suggestions.empty()
    }

    @JvmField
    val ANIMATION_NAMES: SuggestionProvider<CommandSourceStack> = SuggestionProviders.register(
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "animations")
    ) { commandContext, suggestionsBuilder ->
        if (commandContext.source is SharedSuggestionProvider) {
            if (PlatformAPI.isServer()) {
                return@register Suggestions.empty()
            }
            val map = ClientModelManager.getLocalModelContext().animationBundle.mainAnimations
            val set = Sets.newHashSet<String>()
            set.addAll(map.keys.stream().map { escapeIfRequired(it) }.toList())
            set.add("stop")
            return@register SharedSuggestionProvider.suggest(set, suggestionsBuilder)
        }
        Suggestions.empty()
    }

    @JvmField
    val TEXTURE_IDS: SuggestionProvider<CommandSourceStack> = SuggestionProviders.register(
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "textures")
    ) { commandContext, suggestionsBuilder ->
        if (commandContext.source is SharedSuggestionProvider) {
            val str = commandContext.getArgument("model_id", String::class.java)
            if (PlatformAPI.isServer()) {
                if (ServerModelManager.getServerModelInfo().containsKey(str)) {
                    val list = ServerModelManager.getServerModelInfo()[str]?.modelInfo?.textures?.stream()
                        ?.map { escapeIfRequired(it) }
                        ?.collect(Collectors.toList()) ?: Lists.newArrayList()
                    list.add(0, "-")
                    return@register SharedSuggestionProvider.suggest(list, suggestionsBuilder)
                }
            } else if (ClientModelManager.getModelAssemblyMap().containsKey(str)) {
                val list2 = ClientModelManager.getModelContext(str)
                    .map { context ->
                        context.animationBundle.textures.keys.stream()
                            .map { escapeIfRequired(it) }
                            .collect(Collectors.toList())
                    }.orElseGet { Lists.newArrayList() }
                list2.add(0, "-")
                return@register SharedSuggestionProvider.suggest(list2, suggestionsBuilder)
            }
        }
        Suggestions.empty()
    }

    @JvmStatic
    fun register() {
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

    private fun escapeIfRequired(str: String): String {
        if (str.chars().allMatch { i -> StringReader.isAllowedInUnquotedString(i.toChar()) }) {
            return str
        }
        return "\"${str.replace("\"", "\\\"").replace("'", "\\'")}\""
    }
}
