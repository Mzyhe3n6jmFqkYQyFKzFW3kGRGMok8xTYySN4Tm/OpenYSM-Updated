package rip.ysm.compat.immersiveaircraft.fabric

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.fabricmc.loader.api.FabricLoader
import org.joml.Vector3f
import java.util.Optional

object ImmersiveAirCraftCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("immersive_aircraft")

    @JvmStatic
    fun getAircraftRotation(event: AnimationEvent<GeckoVehicleEntity>): Optional<Vector3f> = Optional.empty()
}
