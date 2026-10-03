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
import org.jetbrains.annotations.NotNull
import java.util.Locale
import java.util.function.Function

class MaidBinding {
    constructor() {
    }
    companion object {
        @JvmStatic fun registerBindings(binding: TLMBinding) {
            binding.livingEntityVar("is_begging", createMaidEvaluable(EntityMaid::isBegging))
            binding.livingEntityVar("is_sitting", createMaidEvaluable(EntityMaid::isMaidInSittingPose))
            binding.livingEntityVar("has_backpack", createMaidEvaluable(EntityMaid::hasBackpack))
            binding.livingEntityVar("favorability_point", createMaidEvaluable(EntityMaid::getFavorability))
            binding.livingEntityVar("favorability_level", createMaidEvaluable({ maid -> 
maid.getFavorabilityManager().getLevel()
 }))
            binding.livingEntityVar("task_id", createMaidEvaluable({ maid -> 
maid.getTask().getUid()
 }))
            binding.livingEntityVar("schedule", createMaidEvaluable({ maid -> 
maid.getSchedule().name.toLowerCase(Locale.ENGLISH)
 }))
            binding.livingEntityVar("activity", createMaidEvaluable({ maid -> 
maid.getScheduleDetail().getName()
 }))
            binding.livingEntityVar("gomoku_win_count", createMaidEvaluable({ maid -> 
maid.getGameRecordManager().getGomokuWinCount()
 }))
            binding.livingEntityVar("gomoku_rank", createMaidEvaluable(MaidGomokuAI::getRank))
            binding.livingEntityVar("game_statue", createMaidEvaluable(MaidBinding::getMaidTask))
            binding.livingEntityVar("backpack_type", createMaidEvaluable({ maid -> 
maid.getMaidBackpackType().getId().toString()
 }))
            binding.livingEntityVar("is_entity", createMaidEvaluable({ maid -> 
maid.renderState == MaidRenderState.ENTITY
 }))
            binding.livingEntityVar("is_statue", createMaidEvaluable({ maid -> 
maid.renderState == MaidRenderState.STATUE
 }))
            binding.livingEntityVar("is_garage_kit", createMaidEvaluable({ maid -> 
maid.renderState == MaidRenderState.GARAGE_KIT
 }))
            binding.livingEntityVar("show_item", createMaidEvaluable(MaidBinding::getMaidSchedule))
        }
        @JvmStatic fun createMaidEvaluable(function: Function<EntityMaid, Any>): IValueEvaluator<Any, IContext<LivingEntity>> {
            { ctx -> 
if (ctx.entity() is EntityMaid) { function.apply(maid) }
return 0
 }
        }
        @JvmStatic fun getMaidTask(maid: EntityMaid): String {
            if ((maid as LivingEntity).getVehicle() is EntitySit) {
                var gameRecordManager: MaidGameRecordManager = maid.getGameRecordManager()
                if (gameRecordManager.isWin()) {
                    "win"
                }
                if (gameRecordManager.isLost()) {
                    "lost"
                }
                return StringPool.EMPTY
            }
            return StringPool.EMPTY
        }
        @JvmStatic fun getMaidSchedule(entityMaid: EntityMaid): String {
            var backpackShowItem: ItemStack = entityMaid.getBackpackShowItem()
            if (backpackShowItem.isEmpty()) {
                StringPool.EMPTY
            }
            var key: Identifier = BuiltInRegistries.ITEM.getKey(backpackShowItem.getItem())
            return key.toString()
        }
    }
}