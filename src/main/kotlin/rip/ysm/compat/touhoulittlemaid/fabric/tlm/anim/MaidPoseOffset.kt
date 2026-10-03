package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionChair
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionVehicle
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.animal.pig.Pig
import net.minecraft.world.entity.vehicle.boat.Boat
import org.apache.commons.lang3.StringUtils
import rip.ysm.compat.swem.SWEMCompat
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

class MaidPoseOffset {
    constructor() {
    }
    companion object {
        @JvmField var POSE_RIDE: Float = 0.90f
        @JvmField var POSE_RIDE_PIG: Float = 0.3625f
        @JvmField var POSE_BOAT: Float = -0.45f
        @JvmField var POSE_SIT: Float = -0.5f
        @JvmField var SEAT_UNMEASURED: Float = 0.0f
        @JvmStatic fun resolve(maid: EntityMaid, animatable: MaidAnimatable): Float {
            var vehicle: Entity = vanilla(maid).getVehicle()
            if (vehicle == null || !vehicle.isAlive()) {
                if (isStandbySitPlaying(maid)) POSE_SIT else 0.0f
            }
            return resolveSeated(maid, animatable, vehicle)
        }
        @JvmStatic fun resolveSeated(maid: EntityMaid, animatable: MaidAnimatable, vehicle: Entity): Float {
            var living: LivingEntity = vanilla(maid)
            if (StringUtils.isNoneBlank(SWEMCompat.getHorseGaitName(living))) {
                SEAT_UNMEASURED
            }
            var conditionManager: ConditionManager = animatable.getModelConfig()
            var conditionChair: ConditionChair = conditionManager.getChair()
            if (conditionChair != null && StringUtils.isNoneBlank(conditionChair.doTest(living))) {
                SEAT_UNMEASURED
            }
            var conditionVehicle: ConditionVehicle = conditionManager.getVehicle()
            if (conditionVehicle != null && StringUtils.isNoneBlank(conditionVehicle.doTest(living))) {
                SEAT_UNMEASURED
            }
            if (vehicle is Pig) {
                POSE_RIDE_PIG - attachmentHeight(vehicle, living)
            }
            if (vehicle is Mob && mob.isSaddled()) {
                POSE_RIDE - attachmentHeight(vehicle, living)
            }
            if (vehicle is Boat) {
                POSE_BOAT - attachmentHeight(vehicle, living)
            }
            return SEAT_UNMEASURED
        }
        @JvmStatic fun attachmentHeight(vehicle: Entity, passenger: LivingEntity): Float {
            (vehicle.getPassengerRidingPosition(passenger).y - vehicle.getY() as Float)
        }
        @JvmStatic fun isStandbySitPlaying(maid: EntityMaid): Boolean {
            if (!maid.isMaidInSittingPose()) {
                false
            }
            var living: LivingEntity = vanilla(maid)
            return !living.isDeadOrDying() && living.getPose() != Pose.SLEEPING && !living.isSwimming() && !living.onClimbable()
        }
        @JvmStatic fun vanilla(maid: EntityMaid): LivingEntity {
            maid
        }
    }
}