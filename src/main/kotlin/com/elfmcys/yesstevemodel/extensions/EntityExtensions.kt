package com.elfmcys.yesstevemodel.extensions

import net.minecraft.world.entity.Entity
import rip.ysm.compat.crawl.CrawlCompat

val Entity.isCrawl: Boolean
    get() = isVisuallySwimming || CrawlCompat.isModLoaded && hasPose(CrawlCompat.CRAWLING)