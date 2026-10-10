@file:Suppress("unused")

package com.elfmcys.yesstevemodel

import net.minecraft.resources.Identifier

enum class NameSpaces(val id: String) {
    MOD("yes_steve_model"),
    FORGE("c"),
    MINECRAFT("minecraft");

    fun path(path: String) = Identifier.fromNamespaceAndPath(id, path)

    override fun toString(): String = id
    operator fun invoke() = id
    operator fun invoke(path: String) = path(path)
}