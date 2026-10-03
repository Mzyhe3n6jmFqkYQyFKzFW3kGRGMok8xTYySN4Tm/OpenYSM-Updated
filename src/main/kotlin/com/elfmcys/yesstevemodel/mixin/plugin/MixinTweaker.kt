package com.elfmcys.yesstevemodel.mixin.plugin

import com.elfmcys.yesstevemodel.util.obfuscate.Keep
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class MixinTweaker : IMixinConfigPlugin {
    @Keep
    override fun onLoad(str: String) {
    }

    @Keep
    override fun getRefMapperConfig(): String? = null

    @Keep
    override fun shouldApplyMixin(str: String, str2: String): Boolean = true

    @Keep
    override fun acceptTargets(set: MutableSet<String>, set2: MutableSet<String>) {
    }

    @Keep
    override fun getMixins(): MutableList<String>? = null

    @Keep
    override fun preApply(str: String, classNode: ClassNode, str2: String, iMixinInfo: IMixinInfo) {
    }

    @Keep
    override fun postApply(str: String, classNode: ClassNode, str2: String, iMixinInfo: IMixinInfo) {
    }
}
