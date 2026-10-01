package rip.ysm.compat.immersiveaircraft.fabric

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import java.util.*

object ImmersiveAirCraftCompatImpl {
    @JvmStatic
    fun getAircraftRotation(event: AnimationEvent<GeckoVehicleEntity>): Optional<Vector3f> = Optional.empty()
}
