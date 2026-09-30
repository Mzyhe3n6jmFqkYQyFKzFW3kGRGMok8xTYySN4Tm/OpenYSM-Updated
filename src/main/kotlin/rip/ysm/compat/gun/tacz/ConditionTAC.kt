package rip.ysm.compat.gun.tacz

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import org.apache.commons.lang3.StringUtils
import rip.ysm.compat.gun.swarfare.SWarfareCompat

class ConditionTAC {
    private val nameTest: ObjectOpenHashSet<String> = ObjectOpenHashSet()
    private val idTest: ObjectOpenHashSet<Identifier> = ObjectOpenHashSet()

    fun addTest(name: String) {
        if (!name.startsWith("tac:") || !name.contains("$")) {
            return
        }
        val strArrSplit = StringUtils.split(name, "$", 2)
        if (strArrSplit.size < 2) {
            return
        }
        val str2 = strArrSplit[1]
        if (Identifier.tryParse(str2) != null) {
            this.nameTest.add(name)
            this.idTest.add(Identifier.parse(str2))
        }
    }

    fun doTest(itemStack: ItemStack, str: String): String {
        if (itemStack.isEmpty) {
            return EMPTY
        }
        var gunId = TacCompat.getGunTexture(itemStack)
        if (gunId == null) {
            gunId = SWarfareCompat.getGunTexture(itemStack)
            if (gunId == null) {
                return EMPTY
            }
        }
        if (this.idTest.contains(gunId)) {
            val str2 = str.substring(0, str.length - 1) + "$" + gunId
            if (this.nameTest.contains(str2)) {
                return str2
            }
            return EMPTY
        }
        return EMPTY
    }

    companion object {
        private const val EMPTY = ""
    }
}
