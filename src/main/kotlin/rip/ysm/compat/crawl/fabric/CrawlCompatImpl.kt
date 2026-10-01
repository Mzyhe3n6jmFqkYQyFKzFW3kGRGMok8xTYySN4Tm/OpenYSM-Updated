package rip.ysm.compat.crawl.fabric

import net.minecraft.world.entity.Pose
import ru.fewizz.crawl.Crawl

object CrawlCompatImpl {
    val CRAWLING: Pose by lazy { Crawl.Shared.CRAWLING }
}