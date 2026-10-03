package rip.ysm.compat.immersiveaircraft.fabric

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f

object ImmersiveAirCraftCompatImpl {
    // TODO: Implement aircraft rotation logic
    @JvmStatic
    fun getAircraftRotation(event: AnimationEvent<GeckoVehicleEntity>): Vector3f? = null
}
