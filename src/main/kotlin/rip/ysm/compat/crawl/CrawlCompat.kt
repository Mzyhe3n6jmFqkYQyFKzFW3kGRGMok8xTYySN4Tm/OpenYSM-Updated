package rip.ysm.compat.crawl

import net.minecraft.world.entity.Pose
import rip.ysm.compat.ModCompat
import rip.ysm.compat.crawl.fabric.CrawlCompatImpl

object CrawlCompat : ModCompat("crawl") {
    val CRAWLING: Pose by lazy { if (!isModLoaded) Pose.SWIMMING else CrawlCompatImpl.CRAWLING }
}