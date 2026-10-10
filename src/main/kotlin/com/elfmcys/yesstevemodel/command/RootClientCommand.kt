package com.elfmcys.yesstevemodel.command

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingStruct
import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.entity.RoamingPropertyHolder
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay
import com.elfmcys.yesstevemodel.command.subcommands.client.DebugCommand
import com.elfmcys.yesstevemodel.command.subcommands.client.MoLangCommand
import com.elfmcys.yesstevemodel.command.subcommands.client.WatchCommand
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.SuggestionProviders
import rip.ysm.api.PlatformAPI

object RootClientCommand {
    private const val ROOT_NAME: String = "ysmclient"

    val VARS_SUGGESTION_PROVIDER: SuggestionProvider<CommandSourceStack> = SuggestionProviders.register(
        NameSpaces.MOD.path("vars")
    ) { context, builder ->
        if (context.source is SharedSuggestionProvider && !PlatformAPI.isServer) {
            val geo = activeGeoModel ?: return@register Suggestions.empty()
            val set = HashSet<String>()
            geo.evaluationContext.forEachPropertyName { str ->
                set.add("v.$str")
            }
            if (geo is RoamingPropertyHolder) {
                val struct = (geo as RoamingPropertyHolder).serverVarContainer
                if (struct is RoamingStruct) {
                    struct.forEachVar { str2 ->
                        set.add("v.roaming.$str2")
                    }
                }
            }
            GeckoLibCache.globalBindings.forEach { (namespace, obj) ->
                if (obj is ContextBinding) {
                    obj.keys.forEach { key ->
                        set.add("$namespace.$key")
                    }
                }
            }
            geo.modelAssembly?.expressionCache?.functions?.let {
                for (s in it.keys) {
                    set.add("fn.$s")
                }
            }
            return@register SharedSuggestionProvider.suggest(set, builder)
        }
        Suggestions.empty()
    }

    val CONTROLLERS_SUGGESTION_PROVIDER: SuggestionProvider<CommandSourceStack> = SuggestionProviders.register(
        NameSpaces.MOD.path("controllers")
    ) { commandContext, suggestionsBuilder ->
        if (commandContext.source is SharedSuggestionProvider && !PlatformAPI.isServer) {
            val geo = activeGeoModel ?: return@register Suggestions.empty()
            val controllers = HashSet<String>()
            for (controller in geo.animationData.animationControllers) {
                controllers.add(controller.name)
            }
            return@register SharedSuggestionProvider.suggest(controllers, suggestionsBuilder)
        }
        Suggestions.empty()
    }

    fun registerClientCommands(commandDispatcher: CommandDispatcher<CommandSourceStack>) {
        val root = Commands.literal(ROOT_NAME)
            .requires { commandSourceStack -> YSMMessageFormatter.isCurrentClientPlayer(commandSourceStack.entity) }
        root.then(MoLangCommand.register())
        root.then(WatchCommand.register())
        root.then(DebugCommand.register())
        commandDispatcher.register(root)
    }

    val activeGeoModel: GeoEntity<*>?
        get() {
            var geoEntity = AnimationDebugOverlay.activeModel
            if (geoEntity == null) {
                val localPlayer = Minecraft.getInstance().player
                if (localPlayer != null) {
                    geoEntity = PlayerCapability[localPlayer]
                }
            }
            return geoEntity
        }
}