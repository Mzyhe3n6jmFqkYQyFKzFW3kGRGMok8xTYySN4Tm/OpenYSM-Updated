@file:Suppress("unused")

package rip.ysm.compat

import com.elfmcys.yesstevemodel.Constants
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.service.MixinService

abstract class ModMixinConfigPlugin : IMixinConfigPlugin {
    abstract fun isModLoaded(): Boolean

    override fun onLoad(mixinPackage: String) {}

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (!isModLoaded()) return false
        val classNode =
            runCatching { MixinService.getService().bytecodeProvider.getClassNode(targetClassName) }.getOrNull()
        if (classNode == null) {
            Constants.LOGGER.warn("Mixin {} failed to apply to {}", mixinClassName, targetClassName)
            return false
        }
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {}

    override fun getMixins(): MutableList<String> = mutableListOf()

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }
}