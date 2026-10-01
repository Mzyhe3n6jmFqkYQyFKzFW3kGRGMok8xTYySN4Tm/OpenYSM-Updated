package rip.ysm.compat.carryon.fabric

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnDataHelper.CarryType
import java.lang.reflect.Method

object CarryOnDataHelperImpl {
    private var initialized = false
    private var getCarryDataMethod: Method? = null
    private var isCarryingMethod: Method? = null
    private var getNbtMethod: Method? = null

    private fun initReflection() {
        if (initialized) return
        initialized = true
        try {
            val dataManagerClass = Class.forName("tschipp.carryon.common.carry.CarryOnDataManager")
            getCarryDataMethod = dataManagerClass.getMethod("getCarryData", Player::class.java)

            val carryDataClass = Class.forName("tschipp.carryon.common.carry.CarryOnData")
            isCarryingMethod = carryDataClass.getMethod("isCarrying")
            getNbtMethod = try {
                carryDataClass.getMethod("getNbt")
            } catch (_: NoSuchMethodException) {
                null
            }
        } catch (_: Throwable) {
            // CarryOn classes not found or failed to load
        }
    }

    @JvmStatic
    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean {
        if (livingEntity !is Player) return false
        return getCarryType(livingEntity) != CarryType.NONE
    }

    @JvmStatic
    fun getCarryType(player: Player): CarryType {
        initReflection()
        val getCarryData = getCarryDataMethod ?: return CarryType.NONE
        try {
            val carryData = getCarryData.invoke(null, player) ?: return CarryType.NONE
            val isCarrying = isCarryingMethod?.invoke(carryData) as? Boolean ?: false
            if (!isCarrying) return CarryType.NONE

            val nbt = getNbtMethod?.invoke(carryData) as? CompoundTag
            if (nbt != null) {
                val typeStr = nbt.getStringOr("type", "").uppercase()
                return when (typeStr) {
                    "BLOCK" -> CarryType.BLOCK
                    "ENTITY" -> CarryType.ENTITY
                    "PLAYER" -> CarryType.PLAYER
                    else -> CarryType.NONE
                }
            }

            val str = carryData.toString().uppercase()
            return when {
                str.contains("BLOCK") -> CarryType.BLOCK
                str.contains("ENTITY") -> CarryType.ENTITY
                str.contains("PLAYER") -> CarryType.PLAYER
                else -> CarryType.NONE
            }
        } catch (_: Throwable) {
            return CarryType.NONE
        }
    }

    @JvmStatic
    fun isPrincess(player: Player): Boolean {
        val vehicle = player.vehicle
        if (vehicle is Player) {
            return getCarryType(vehicle) == CarryType.PLAYER
        }
        return false
    }
}
