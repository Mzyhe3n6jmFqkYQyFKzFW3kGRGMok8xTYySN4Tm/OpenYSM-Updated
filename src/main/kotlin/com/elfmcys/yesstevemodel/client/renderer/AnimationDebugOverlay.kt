package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.elfmcys.yesstevemodel.client.animation.molang.MolangWatchRegistry
import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import rip.ysm.api.client.HudOverlay
import rip.ysm.compat.touhoulittlemaid.MaidCapabilityBridge
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat
import java.lang.ref.WeakReference

object AnimationDebugOverlay {
    @JvmField
    val MOLANG_WATCH: MolangWatchRegistry = MolangWatchRegistry()

    @JvmField
    val DEBUG_LINES: ReferenceArrayList<String> = ReferenceArrayList()

    @JvmField
    var activeModel: WeakReference<GeoEntity<*>>? = null

    @JvmStatic
    fun createOverlay(): HudOverlay {
        return HudOverlay { guiGraphics, font, _, screenWidth, screenHeight ->
            renderOverlay(font, guiGraphics, screenWidth, screenHeight)
        }
    }

    @JvmStatic
    fun getMolangWatch(): MolangWatchRegistry {
        return MOLANG_WATCH
    }

    @JvmStatic
    fun isDebugActive(): Boolean {
        return getActiveModel() != null
    }

    @JvmStatic
    fun tryUpdateFromHitResult(): Boolean {
        val hitResult: HitResult? = Minecraft.getInstance().hitResult
        if (hitResult is EntityHitResult) {
            return tryUpdateFromEntity(hitResult.entity)
        }
        return tryUpdateFromLocalPlayer()
    }

    @JvmStatic
    fun tryUpdateFromLocalPlayer(): Boolean {
        val localPlayer: LocalPlayer? = Minecraft.getInstance().player
        if (localPlayer != null) {
            val cap = PlayerCapability[localPlayer]
            if (cap != null) {
                setActiveModel(cap)
                return true
            }
        }
        clearActiveModel()
        return false
    }

    @JvmStatic
    fun tryUpdateFromEntity(entity: Entity): Boolean {
        val capability: Any? = when {
            entity is Player -> PlayerCapability[entity]
            TouhouLittleMaidCompat.isMaidEntity(entity) -> MaidCapabilityBridge[entity]
            entity is Projectile -> ProjectileCapability[entity]
            else -> VehicleCapability[entity]
        }
        if (capability is GeoEntity<*>) {
            setActiveModel(capability)
            return true
        }
        clearActiveModel()
        return false
    }

    @JvmStatic
    fun setActiveModel(geoEntity: GeoEntity<*>) {
        clearActiveModel()
        activeModel = WeakReference(geoEntity)
        geoEntity.setBoneLookup(MOLANG_WATCH)
        val entity: Entity = geoEntity.entity
        val localPlayer: LocalPlayer? = Minecraft.getInstance().player
        if (localPlayer != null) {
            val mutableComponentAppend: MutableComponent =
                Component.translatable("message.yes_steve_model.model.debug_animation.true").append(" -> ")
            val customName: Component? = entity.customName
            val displayName = customName ?: entity.displayName
            localPlayer.displayClientMessage(
                mutableComponentAppend.append(displayName),
                false
            )
        }
    }

    @JvmStatic
    fun clearActiveModel() {
        val currentModel = activeModel
        if (currentModel != null) {
            val geoEntity: GeoEntity<*>? = currentModel.get()
            if (geoEntity != null) {
                geoEntity.setBoneLookup(null)
            }
            activeModel = null
            val localPlayer: LocalPlayer? = Minecraft.getInstance().player
            if (localPlayer != null) {
                localPlayer.displayClientMessage(
                    Component.translatable("message.yes_steve_model.model.debug_animation.false"),
                    false
                )
            }
        }
    }

    @JvmStatic
    fun addDebugLine(str: String) {
        DEBUG_LINES.add(0, str)
    }

    @JvmStatic
    fun clearDebugLines() {
        DEBUG_LINES.clear()
    }

    @JvmStatic
    fun getActiveModel(): GeoEntity<*>? {
        val currentModel = activeModel
        if (currentModel != null) {
            val geoEntity: GeoEntity<*>? = currentModel.get()
            if (geoEntity != null && geoEntity.isDebugMode()) {
                return geoEntity
            }
            clearActiveModel()
            return null
        }
        return null
    }

    @JvmStatic
    fun renderOverlay(font: Font, guiGraphics: GuiGraphics, screenWidth: Int, screenHeight: Int) {
        val geoEntity: GeoEntity<*> = getActiveModel() ?: return
        val currentY = intArrayOf(5)
        MOLANG_WATCH.forEachEntry { molangKey, molangValue ->
            renderDebugOverlay(font, guiGraphics, currentY, molangKey, molangValue, screenWidth, screenHeight)
        }
        DEBUG_LINES.forEach { str3 ->
            val controller: IAnimationController<*>? = geoEntity.getAnimationData().getAnimationControllerByName(str3)
            renderDebugOverlay(
                font,
                guiGraphics,
                currentY,
                str3,
                controller?.getCurrentAnimation() ?: "(N/A)",
                screenWidth,
                screenHeight
            )
        }
    }

    @JvmStatic
    fun renderDebugOverlay(
        font: Font,
        guiGraphics: GuiGraphics,
        currentY: IntArray,
        key: String,
        value: String,
        screenWidth: Int,
        screenHeight: Int
    ) {
        if ((currentY[0] - 5) % 20 == 0) {
            guiGraphics.fill(2, currentY[0] - 1, screenWidth, currentY[0] + 9, -1068478384)
        } else {
            guiGraphics.fill(2, currentY[0] - 1, screenWidth, currentY[0] + 9, -1068474288)
        }
        guiGraphics.drawString(font, key, 5, currentY[0], -1)
        guiGraphics.drawString(font, value, screenWidth / 2, currentY[0], -1)
        currentY[0] = currentY[0] + 10
    }
}