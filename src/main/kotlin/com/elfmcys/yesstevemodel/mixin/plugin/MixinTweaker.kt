package com.elfmcys.yesstevemodel.mixin.plugin

import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class MixinTweaker : IMixinConfigPlugin {
    override fun onLoad(str: String) {
    }

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(str: String, str2: String): Boolean = true

    override fun acceptTargets(set: MutableSet<String>, set2: MutableSet<String>) {
    }

    override fun getMixins(): MutableList<String>? = null

    override fun preApply(str: String, classNode: ClassNode, str2: String, iMixinInfo: IMixinInfo) {
    }

    override fun postApply(str: String, classNode: ClassNode, str2: String, iMixinInfo: IMixinInfo) {
    }
}
