package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidGomokuAI
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidGameRecordManager
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import java.util.*

object MaidBinding {

    fun registerBindings(binding: TLMBinding) {
        binding.livingEntityVar("is_begging", createMaidEvaluable(EntityMaid::isBegging))
        binding.livingEntityVar("is_sitting", createMaidEvaluable(EntityMaid::isMaidInSittingPose))
        binding.livingEntityVar("has_backpack", createMaidEvaluable(EntityMaid::hasBackpack))
        binding.livingEntityVar("favorability_point", createMaidEvaluable(EntityMaid::getFavorability))
        binding.livingEntityVar("favorability_level", createMaidEvaluable { maid -> maid.favorabilityManager.level })
        binding.livingEntityVar("task_id", createMaidEvaluable { maid -> maid.task.uid })
        binding.livingEntityVar(
            "schedule",
            createMaidEvaluable { maid -> maid.schedule.name.lowercase(Locale.ENGLISH) })
        binding.livingEntityVar("activity", createMaidEvaluable { maid -> maid.scheduleDetail.name })
        binding.livingEntityVar(
            "gomoku_win_count",
            createMaidEvaluable { maid -> maid.gameRecordManager.gomokuWinCount })
        binding.livingEntityVar("gomoku_rank", createMaidEvaluable(MaidGomokuAI::getRank))
        binding.livingEntityVar("game_statue", createMaidEvaluable(::getMaidTask))
        binding.livingEntityVar("backpack_type", createMaidEvaluable { maid -> maid.maidBackpackType.id.toString() })
        binding.livingEntityVar("is_entity", createMaidEvaluable { maid -> maid.renderState == MaidRenderState.ENTITY })
        binding.livingEntityVar("is_statue", createMaidEvaluable { maid -> maid.renderState == MaidRenderState.STATUE })
        binding.livingEntityVar(
            "is_garage_kit",
            createMaidEvaluable { maid -> maid.renderState == MaidRenderState.GARAGE_KIT })
        binding.livingEntityVar("show_item", createMaidEvaluable(::getMaidSchedule))
    }

    private fun createMaidEvaluable(function: (EntityMaid) -> Any): IValueEvaluator<Any, IContext<LivingEntity>> {
        return IValueEvaluator { ctx ->
            val entity = ctx.entity
            if (entity is EntityMaid) {
                function(entity)
            } else {
                0
            }
        }
    }

    private fun getMaidTask(maid: EntityMaid): String {
        if ((maid as LivingEntity).vehicle is EntitySit) {
            val gameRecordManager: MaidGameRecordManager = maid.gameRecordManager
            if (gameRecordManager.isWin) {
                return "win"
            }
            if (gameRecordManager.isLost) {
                return "lost"
            }
            return StringPool.EMPTY
        }
        return StringPool.EMPTY
    }

    private fun getMaidSchedule(entityMaid: EntityMaid): String {
        val backpackShowItem: ItemStack = entityMaid.backpackShowItem
        if (backpackShowItem.isEmpty) {
            return StringPool.EMPTY
        }
        val key: Identifier = BuiltInRegistries.ITEM.getKey(backpackShowItem.item)
        return key.toString()
    }
}
