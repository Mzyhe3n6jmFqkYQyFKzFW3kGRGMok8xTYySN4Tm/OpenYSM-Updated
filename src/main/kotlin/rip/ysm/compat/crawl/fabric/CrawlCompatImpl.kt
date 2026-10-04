package rip.ysm.compat.crawl.fabric

import net.minecraft.world.entity.Pose
import rip.ysm.compat.ModCompat
import ru.fewizz.crawl.Crawl

object CrawlCompatImpl : ModCompat("crawl") {
    val CRAWLING: Pose by lazy { Crawl.Shared.CRAWLING }
}