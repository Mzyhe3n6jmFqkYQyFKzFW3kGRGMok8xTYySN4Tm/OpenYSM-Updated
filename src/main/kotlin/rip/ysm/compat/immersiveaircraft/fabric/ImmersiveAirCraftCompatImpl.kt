package rip.ysm.compat.immersiveaircraft.fabric

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import rip.ysm.compat.immersiveaircraft.ImmersiveAirCraftCompat
import java.util.Optional

object ImmersiveAirCraftCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = ImmersiveAirCraftCompat.isModLoaded

    @JvmStatic
    fun getAircraftRotation(event: AnimationEvent<GeckoVehicleEntity>): Optional<Vector3f> = Optional.empty()
}
