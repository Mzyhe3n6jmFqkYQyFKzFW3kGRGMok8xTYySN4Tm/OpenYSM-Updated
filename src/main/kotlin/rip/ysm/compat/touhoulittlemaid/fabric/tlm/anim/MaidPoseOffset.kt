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

@Environment(EnvType.CLIENT)
object MaidPoseOffset {
    private const val POSE_RIDE: Float = 0.90f
    private const val POSE_RIDE_PIG: Float = 0.3625f
    private const val POSE_BOAT: Float = -0.45f
    private const val POSE_SIT: Float = -0.5f
    private const val SEAT_UNMEASURED: Float = 0.0f

    fun resolve(maid: EntityMaid, animatable: MaidAnimatable): Float {
        val vehicle = vanilla(maid).vehicle
        if (vehicle == null || !vehicle.isAlive) {
            return if (isStandbySitPlaying(maid)) POSE_SIT else 0.0f
        }
        return resolveSeated(maid, animatable, vehicle)
    }

    private fun resolveSeated(maid: EntityMaid, animatable: MaidAnimatable, vehicle: Entity): Float {
        val living: LivingEntity = vanilla(maid)
        if (StringUtils.isNoneBlank(SWEMCompat.getHorseGaitName(living))) return SEAT_UNMEASURED
        val conditionManager: ConditionManager? = animatable.modelConfig
        val conditionChair: ConditionChair? = conditionManager?.chair
        if (conditionChair != null && StringUtils.isNoneBlank(conditionChair.doTest(living))) return SEAT_UNMEASURED
        val conditionVehicle: ConditionVehicle? = conditionManager?.vehicle
        if (conditionVehicle != null && StringUtils.isNoneBlank(conditionVehicle.doTest(living))) return SEAT_UNMEASURED
        return when (vehicle) {
            is Pig -> POSE_RIDE_PIG - attachmentHeight(vehicle, living)
            is Mob if vehicle.isSaddled -> POSE_RIDE - attachmentHeight(vehicle, living)
            is Boat -> POSE_BOAT - attachmentHeight(vehicle, living)
            else -> SEAT_UNMEASURED
        }
    }

    private fun attachmentHeight(vehicle: Entity, passenger: LivingEntity): Float =
        (vehicle.getPassengerRidingPosition(passenger).y - vehicle.y).toFloat()

    private fun isStandbySitPlaying(maid: EntityMaid): Boolean {
        if (!maid.isMaidInSittingPose) return false
        val living: LivingEntity = vanilla(maid)
        return !living.isDeadOrDying
                && living.pose != Pose.SLEEPING
                && !living.isInWater
                && !living.onClimbable()
    }

    private fun vanilla(maid: EntityMaid): LivingEntity = maid
}
