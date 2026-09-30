package rip.ysm.compat.immersiveaircraft

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import rip.ysm.compat.immersiveaircraft.fabric.ImmersiveAirCraftCompatImpl
import java.util.Optional

object ImmersiveAirCraftCompat {
    @JvmStatic
    fun isLoaded(): Boolean = ImmersiveAirCraftCompatImpl.isLoaded()

    @JvmStatic
    fun getAircraftRotation(event: AnimationEvent<GeckoVehicleEntity>): Optional<Vector3f> =
        ImmersiveAirCraftCompatImpl.getAircraftRotation(event)
}
