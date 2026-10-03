package rip.ysm.gui.molang

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SRequestExecuteMolangPacket
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.math.NumberUtils
import rip.ysm.gui.Option
import kotlin.math.roundToInt

object MolangOption {
    @JvmStatic
    fun ofBoolean(title: String, description: String?, animatable: AnimatableEntity<*>, expr: String): Option<Boolean> {
        var cache = false
        evaluate(animatable, expr) { s -> cache = toFloat(s) > 0.0f }
        return LiveOption(title, description, { cache }) { v ->
            cache = v
            execute(animatable, "$expr=${if (v) "1" else "0"}")
        }
    }

    @JvmStatic
    fun ofDouble(title: String, description: String?, animatable: AnimatableEntity<*>, expr: String): Option<Double> {
        var cache = 0.0
        evaluate(animatable, expr) { s -> cache = toFloat(s).toDouble() }
        return LiveOption(title, description, { cache }) { v ->
            cache = v
            execute(animatable, "$expr=$v")
        }
    }

    @JvmStatic
    fun ofIndex(
        title: String,
        description: String?,
        animatable: AnimatableEntity<*>,
        readExpr: String,
        writeExprs: Array<String>
    ): Option<Int> {
        var cache = 0
        evaluate(animatable, readExpr) { s -> cache = toFloat(s).roundToInt() }
        return LiveOption(title, description, { cache }) { v ->
            val idx = v.coerceIn(0, writeExprs.size - 1)
            cache = idx
            execute(animatable, writeExprs[idx])
        }
    }

    private fun toFloat(s: String?): Float {
        if (s == null || s == "null") {
            return 0.0f
        }
        if (NumberUtils.isParsable(s)) {
            return s.toFloat()
        }
        val b = BooleanUtils.toBooleanObject(s)
        return if (b != null && b) 1.0f else 0.0f
    }

    private fun evaluate(animatable: AnimatableEntity<*>, expr: String, consumer: (String) -> Unit) {
        runCatching {
            animatable.executeExpression(GeckoLibCache.parseSimpleExpression(expr), true, false, consumer)
        }.onFailure { e ->
            Constants.LOGGER.error(e)
        }
    }

    private fun execute(animatable: AnimatableEntity<*>, expr: String) {
        runCatching {
            animatable.executeExpression(GeckoLibCache.parseSimpleExpression(expr), true, false, null)
            if (!GeckoLibCache.isRoamingVariableAssignment(expr) && NetworkHandler.isClientConnected() && !ServerConfig.LOW_BANDWIDTH_USAGE.get()) {
                NetworkHandler.sendToServer(C2SRequestExecuteMolangPacket(expr, animatable.getEntity().id))
            }
        }.onFailure { e ->
            Constants.LOGGER.error(e)
        }
    }
}