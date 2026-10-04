@file:Suppress("unused")

package com.elfmcys.yesstevemodel.event.events.client

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.player.LocalPlayer

// Copy form dev.architectury.event.events.client.ClientPlayerEvent
object ClientPlayerEvent {
    /**
     * @see ClientPlayerJoin#join(LocalPlayer)
     */
    @JvmField
    val CLIENT_PLAYER_JOIN: Event<ClientPlayerJoin> =
        EventFactory.createArrayBacked(ClientPlayerJoin::class.java) { callback ->
            ClientPlayerJoin { player ->
                callback.forEach { it.join(player) }
            }
        }

    /**
     * @see ClientPlayerQuit#quit(LocalPlayer)
     */
    @JvmField
    val CLIENT_PLAYER_QUIT: Event<ClientPlayerQuit> =
        EventFactory.createArrayBacked(ClientPlayerQuit::class.java) { callback ->
            ClientPlayerQuit { player ->
                callback.forEach { it.quit(player) }
            }
        }

    /**
     * @see ClientPlayerRespawn#respawn(LocalPlayer, LocalPlayer)
     */
    @JvmField
    val CLIENT_PLAYER_RESPAWN: Event<ClientPlayerRespawn> =
        EventFactory.createArrayBacked(ClientPlayerRespawn::class.java) { callback ->
            ClientPlayerRespawn { oldPlayer, newPlayer ->
                callback.forEach { it.respawn(oldPlayer, newPlayer) }
            }
        }

    fun interface ClientPlayerJoin {
        /**
         * Invoked whenever a client player joins a level
         *
         * @param player The player joining.
         */
        fun join(player: LocalPlayer?)
    }

    fun interface ClientPlayerQuit {
        /**
         * Invoked whenever a client player leaves a level and is cleared on the client side.
         *
         * @param player The player leaving.
         */
        fun quit(player: LocalPlayer?)
    }

    fun interface ClientPlayerRespawn {
        /**
         * Invoked whenever the player respawn packet is received by the client.
         *
         * @param oldPlayer The player before the respawn.
         * @param newPlayer The player after the respawn.
         */
        fun respawn(oldPlayer: LocalPlayer?, newPlayer: LocalPlayer?)
    }
}