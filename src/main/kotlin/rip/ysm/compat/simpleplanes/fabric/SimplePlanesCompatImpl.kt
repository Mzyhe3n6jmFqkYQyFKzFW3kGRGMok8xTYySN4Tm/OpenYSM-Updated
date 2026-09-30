package rip.ysm.compat.simpleplanes.fabric

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import rip.ysm.compat.simpleplanes.SimplePlanesCompat
import java.util.Optional

object SimplePlanesCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = SimplePlanesCompat.isModLoaded

    @JvmStatic
    fun getSimplePlanesRotation(event: AnimationEvent<GeckoVehicleEntity>): Optional<Vector3f> = Optional.empty()
}
