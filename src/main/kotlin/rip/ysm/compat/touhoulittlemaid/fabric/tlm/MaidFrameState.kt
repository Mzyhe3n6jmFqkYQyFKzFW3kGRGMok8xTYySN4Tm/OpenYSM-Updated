package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.client.entity.LivingEntityFrameState
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid

open class MaidFrameState : LivingEntityFrameState<EntityMaid> {
    constructor(entityMaid: EntityMaid) {
    }
    open fun reset() {
        super.reset()
    }
}