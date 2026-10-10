package rip.ysm.compat.simpleplanes

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import rip.ysm.compat.ModCompat
import rip.ysm.compat.simpleplanes.fabric.SimplePlanesCompatImpl

object SimplePlanesCompat : ModCompat("simpleplanes") {
    fun getSimplePlanesRotation(event: AnimationEvent<GeckoVehicleEntity>): Vector3f? {
        if (!isModLoaded) return null
        return SimplePlanesCompatImpl.getSimplePlanesRotation(event)
    }
}
