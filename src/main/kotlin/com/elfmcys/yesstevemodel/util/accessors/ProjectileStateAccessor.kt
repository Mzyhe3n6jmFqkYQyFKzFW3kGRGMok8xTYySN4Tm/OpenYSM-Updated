package com.elfmcys.yesstevemodel.util.accessors

interface ProjectileStateAccessor {
    fun `ysm$isArrowInGround`(): Boolean
    fun `ysm$getInGroundTime`(): Int
    fun `ysm$getOwnerItemId`(): String?
}