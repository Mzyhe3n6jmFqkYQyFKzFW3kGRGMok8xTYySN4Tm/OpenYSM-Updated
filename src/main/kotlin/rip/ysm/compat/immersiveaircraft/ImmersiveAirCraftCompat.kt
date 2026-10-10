package rip.ysm.compat.immersiveaircraft

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import org.joml.Vector3f
import rip.ysm.compat.ModCompat
import rip.ysm.compat.immersiveaircraft.fabric.ImmersiveAirCraftCompatImpl

object ImmersiveAirCraftCompat : ModCompat("immersive_aircraft") {
    fun getAircraftRotation(event: AnimationEvent<GeckoVehicleEntity>): Vector3f? {
        if (!isModLoaded) return null
        return ImmersiveAirCraftCompatImpl.getAircraftRotation(event)
    }
}
