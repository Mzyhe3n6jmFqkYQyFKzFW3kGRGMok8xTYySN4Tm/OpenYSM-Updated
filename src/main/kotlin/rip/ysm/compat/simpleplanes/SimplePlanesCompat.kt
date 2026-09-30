package rip.ysm.compat.simpleplanes

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import rip.ysm.compat.simpleplanes.fabric.SimplePlanesCompatImpl
import java.util.Optional

object SimplePlanesCompat {
    @JvmStatic
    fun isLoaded(): Boolean = SimplePlanesCompatImpl.isLoaded()

    @JvmStatic
    fun getSimplePlanesRotation(event: AnimationEvent<GeckoVehicleEntity>): Optional<Vector3f> =
        SimplePlanesCompatImpl.getSimplePlanesRotation(event)
}
