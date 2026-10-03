package rip.ysm.gui.molang

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.molang.parser.ParseException
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SRequestExecuteMolangPacket
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.math.NumberUtils
import rip.ysm.gui.Option
import java.util.function.Consumer

class MolangOption {
    constructor() {
    }
    companion object {
        @JvmStatic fun ofBoolean(title: String, description: String, animatable: AnimatableEntity<*>, expr: String): Option<Boolean> {
            var cache: BooleanArray = booleanArrayOf(false)
            evaluate(animatable, expr, { s -> cache[0] = toFloat(s) > 0.0f })
            return LiveOption(title, description, {  -> cache[0] }, { v -> cache[0] = v
execute(animatable, expr + "=" + if (v) "1" else "0") })
        }
        @JvmStatic fun ofDouble(title: String, description: String, animatable: AnimatableEntity<*>, expr: String): Option<Double> {
            var cache: DoubleArray = doubleArrayOf(0.0)
            evaluate(animatable, expr, { s -> cache[0] = toFloat(s) })
            return LiveOption(title, description, {  -> cache[0] }, { v -> cache[0] = v
execute(animatable, expr + "=" + v) })
        }
        @JvmStatic fun ofIndex(title: String, description: String, animatable: AnimatableEntity<*>, readExpr: String, writeExprs: Array<String>): Option<Integer> {
            var cache: IntArray = intArrayOf(0)
            evaluate(animatable, readExpr, { s -> cache[0] = Math.round(toFloat(s)) })
            return LiveOption(title, description, {  -> cache[0] }, { v -> cache[0] = idx
execute(animatable, writeExprs[idx]) })
        }
        @JvmStatic fun toFloat(s: String): Float {
            if (s == null || "null".equals(s)) {
                return 0.0f
            }
            if (NumberUtils.isParsable(s)) {
                return Float.parseFloat(s)
            }
            var b: Boolean = BooleanUtils.toBooleanObject(s)
            return if (b != null && b) 1.0f else 0.0f
        }
        @JvmStatic fun evaluate(animatable: AnimatableEntity<*>, expr: String, consumer: Consumer<String>) {
            try {
                animatable.executeExpression(GeckoLibCache.parseSimpleExpression(expr), true, false, consumer)
            } catch (e: ParseException) {
                Constants.LOGGER.error(e)
            }
        }
        @JvmStatic fun execute(animatable: AnimatableEntity<*>, expr: String) {
            try {
                animatable.executeExpression(GeckoLibCache.parseSimpleExpression(expr), true, false, null)
                if (!GeckoLibCache.isRoamingVariableAssignment(expr) && NetworkHandler.isClientConnected() && !ServerConfig.LOW_BANDWIDTH_USAGE.get()) {
                    NetworkHandler.sendToServer(C2SRequestExecuteMolangPacket(expr, animatable.getEntity().getId()))
                }
            } catch (e: ParseException) {
                Constants.LOGGER.error(e)
            }
        }
    }
}