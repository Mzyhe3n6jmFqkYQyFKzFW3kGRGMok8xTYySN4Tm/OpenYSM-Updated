package rip.ysm.compat.simpleplanes.fabric

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import rip.ysm.compat.ModCompat

object SimplePlanesCompatImpl : ModCompat("simpleplanes") {
    fun getSimplePlanesRotation(event: AnimationEvent<GeckoVehicleEntity>): Vector3f? = null
}
