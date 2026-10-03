package com.elfmcys.yesstevemodel.resource.models

import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.elfmcys.yesstevemodel.util.data.StringPair
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceLists

class Metadata(
    val name: String,
    val tips: String,
    val license: StringPair,
    authors: Array<AuthorInfo>,
    val link: OrderedStringMap<String, String>
) {
    val authors: List<AuthorInfo> = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(authors))
}
