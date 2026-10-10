@file:Suppress("unused")

package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.renderer.layer.*
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.scores.Team
import rip.ysm.compat.gun.swarfare.SWarfareCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

class CustomPlayerRenderer(context: EntityRendererProvider.Context) :
    GeoReplacedEntityRenderer<Player, CustomPlayerEntity, AvatarRenderState>(context) {

    private var currentTexture: Identifier? = null
    private var currentPlayer: Player? = null

    init {
        addLayerRenderer(CustomPlayerItemInHandLayer(Minecraft.getInstance().gameRenderer.itemInHandRenderer))
        addLayerRenderer(CustomPlayerElytraLayer(context))
        addLayerRenderer(CustomPlayerParrotLayer(context))
        addLayerRenderer(CustomPlayerArmorLayer(context))
        addLayerRenderer(CustomPlayerCarryOnLayer())
    }

    fun render(
        player: Player,
        renderState: AvatarRenderState,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        if (SWarfareCompat.isPlayerAiming(player)) return
        val capability = PlayerCapability[player] ?: return
        currentPlayer = player
        capability.tickModel()
        val renderEvent = SpecialPlayerRenderEvent(player, capability, capability.modelId, renderState, renderState.skin)
        if (SpecialPlayerRenderEvent.post(renderEvent).isFalse()) return
        currentTexture = renderEvent.textureLocation
        renderEntityWithTexture(
            capability,
            renderState,
            renderEvent.textureLocation,
            entityYaw,
            partialTick,
            poseStack,
            bufferSource,
            packedLight
        )
    }

    override fun shouldShowName(entity: Player, distance: Double): Boolean {
        val minecraft = Minecraft.getInstance()
        val localPlayer: LocalPlayer = minecraft.player ?: return false
        val nameRenderDistance = if (entity.isDiscrete) 32.0f else 64.0f
        if (distance >= (nameRenderDistance * nameRenderDistance).toDouble()) {
            return false
        }
        val isVisible = !entity.isInvisibleTo(localPlayer)
        if (entity != localPlayer) {
            val team: Team? = entity.team
            val team2: Team? = localPlayer.team
            if (team != null) {
                return when (team.nameTagVisibility) {
                    Team.Visibility.ALWAYS -> isVisible
                    Team.Visibility.NEVER -> false
                    Team.Visibility.HIDE_FOR_OTHER_TEAMS -> if (team2 == null) isVisible else team.isAlliedTo(team2) && (team.canSeeFriendlyInvisibles() || isVisible)
                    Team.Visibility.HIDE_FOR_OWN_TEAM -> if (team2 == null) isVisible else !team.isAlliedTo(team2) && isVisible
                }
            }
        }
        return Minecraft.renderNames() && entity != minecraft.cameraEntity && isVisible && !entity.isPassenger
    }

    override fun createRenderState(): AvatarRenderState {
        return AvatarRenderState()
    }

    fun getTextureLocation(player: Player): Identifier {
        return currentTexture ?: PlayerCapability[player]?.textureLocation
        ?: MissingTextureAtlasSprite.getLocation()
    }

    override fun getTextureLocation(state: AvatarRenderState): Identifier {
        val texture = currentTexture
        if (texture != null) {
            return texture
        }
        val player = currentPlayer ?: Minecraft.getInstance().player
        if (player != null) {
            return PlayerCapability[player]?.textureLocation ?: MissingTextureAtlasSprite.getLocation()
        }
        return MissingTextureAtlasSprite.getLocation()
    }

    override fun setupRotations(
        entity: Player,
        state: AvatarRenderState,
        poseStack: PoseStack,
        ageInTicks: Float,
        rotationYaw: Float,
        partialTicks: Float,
        scale: Float
    ) {
        super.setupRotations(entity, state, poseStack, ageInTicks, rotationYaw, partialTicks, scale)
        val vehicle: Entity? = entity.vehicle
        if (vehicle != null && (TouhouLittleMaidCompat.isSimplePlanesEntity(vehicle) || TouhouLittleMaidCompat.isImmersiveAircraftEntity(
                vehicle
            ))
        ) {
            poseStack.translate(0.0, 0.5, 0.0)
        }
    }
}