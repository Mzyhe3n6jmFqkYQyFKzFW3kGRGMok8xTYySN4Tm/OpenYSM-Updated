package com.elfmcys.yesstevemodel.resource.models

import com.elfmcys.yesstevemodel.util.data.OrderedStringMap

data class AuthorInfo(
    val name: String,
    val role: String,
    val contact: OrderedStringMap<String, String>,
    val comment: String
)
