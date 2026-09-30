package rip.ysm.compat.simpleplanes.fabric

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.fabricmc.loader.api.FabricLoader
import org.joml.Vector3f
import java.util.Optional

object SimplePlanesCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("simpleplanes")

    @JvmStatic
    fun getSimplePlanesRotation(event: AnimationEvent<GeckoVehicleEntity>): Optional<Vector3f> = Optional.empty()
}
