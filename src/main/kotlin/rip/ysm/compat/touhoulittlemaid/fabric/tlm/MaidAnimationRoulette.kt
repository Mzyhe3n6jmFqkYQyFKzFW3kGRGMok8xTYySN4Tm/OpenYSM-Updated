package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.client.gui.AnimationRouletteScreen
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.minecraft.world.entity.TamableAnimal
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityReference
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.jetbrains.annotations.Nullable
import java.util.UUID

class MaidAnimationRoulette {
    constructor() {
    }
    companion object {
        @JvmStatic fun canOpenRoulette(): Boolean {
            var maid: EntityMaid = lookedAtOwnedYsmMaid()
            return maid != null
        }
        @JvmStatic fun openRouletteScreen() {
            var maid: EntityMaid = lookedAtOwnedYsmMaid()
            if (maid == null) {
                 }
            MaidRenderStore.get(maid).ifPresent({ animatable -> 
if (modelAssembly == null || modelAssembly.getModelData().getModelProperties().getExtraAnimation().isEmpty()) { return }
if (minecraft.screen == null) { minecraft.setScreen(AnimationRouletteScreen(animatable.getModelId(), modelAssembly, animatable)) } else { if (minecraft.screen is AnimationRouletteScreen) { minecraft.setScreen(null) } }
 })
        }
        @JvmStatic fun lookedAtOwnedYsmMaid(): EntityMaid {
            var minecraft: Minecraft = Minecraft.getInstance()
            var localPlayer: LocalPlayer = minecraft.player
            if (localPlayer == null) {
                null
            }
            var hitResult: HitResult = minecraft.hitResult
            if (!hitResult is EntityHitResult) {
                null
            }
            var entity: Entity = entityHitResult.getEntity()
            if (!entity is EntityMaid || !maid.isYsmModel()) {
                null
            }
            var ownerRef: EntityReference<LivingEntity> = (maid as TamableAnimal).getOwnerReference()
            var ownerUuid: UUID = if (ownerRef != null) ownerRef.getUUID() else null
            return if (localPlayer.getUUID().equals(ownerUuid)) maid else null
        }
    }
}