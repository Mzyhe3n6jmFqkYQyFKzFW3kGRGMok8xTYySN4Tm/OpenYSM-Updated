@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block.BlockBehaviorVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block.BlockStateVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block.BlockVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity.*
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.item.ItemStackVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.item.ItemVariable
import com.elfmcys.yesstevemodel.molang.parser.ast.StringExpression
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.entity.projectile.arrow.Arrow
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

open class ContextBinding : ObjectBinding {
    val bindings: Object2ReferenceOpenHashMap<String, Any> = Object2ReferenceOpenHashMap()

    override fun getProperty(name: String): Any? = bindings[name]

    open val keys: MutableSet<String>
        get() = bindings.keys

    open fun function(name: String, function: Function) {
        bindings[name] = function
    }

    open fun constValue(name: String, obj: Any) {
        when (obj) {
            is String -> bindings[name] = StringExpression(obj)
            is Number -> bindings[name] = obj.toFloat()
            is Boolean -> bindings[name] = if (obj) 1.0f else 0.0f
            else -> bindings[name] = obj
        }
    }

    open fun `var`(name: String, evaluator: IValueEvaluator<*, IContext<Any>>) {
        bindings[name] = LambdaVariable(evaluator)
    }

    open fun entityVar(name: String, evaluator: IValueEvaluator<*, IContext<Entity>>) {
        bindings[name] = EntityVariable(evaluator)
    }

    open fun livingEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<LivingEntity>>) {
        bindings[name] = LivingEntityVariable(evaluator)
    }

    open fun mobEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<Mob>>) {
        bindings[name] = MobEntityVariable(evaluator)
    }

    open fun tamableEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<TamableAnimal>>) {
        bindings[name] = TamableEntityVariable(evaluator)
    }

    open fun playerEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<Player>>) {
        bindings[name] = PlayerEntityVariable(evaluator)
    }

    open fun clientPlayerEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<AbstractClientPlayer>>) {
        bindings[name] = ClientPlayerEntityVariable(evaluator)
    }

    open fun localPlayerEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<LocalPlayer>>) {
        bindings[name] = LocalPlayerEntityVariable(evaluator)
    }

    open fun projectileEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<Projectile>>) {
        bindings[name] = ProjectileEntityVariable(evaluator)
    }

    open fun throwableProjectileEntityVar(
        name: String,
        evaluator: IValueEvaluator<*, IContext<ThrowableItemProjectile>>
    ) {
        bindings[name] = ThrowableProjectileEntityVariable(evaluator)
    }

    open fun fishHookEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<FishingHook>>) {
        bindings[name] = FishingHookEntityVariable(evaluator)
    }

    open fun abstractArrowEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<AbstractArrow>>) {
        bindings[name] = AbstractArrowEntityVariable(evaluator)
    }

    open fun arrowEntityVar(name: String, evaluator: IValueEvaluator<*, IContext<Arrow>>) {
        bindings[name] = ArrowEntityVariable(evaluator)
    }

    open fun itemVar(name: String, evaluator: IValueEvaluator<*, IContext<Item>>) {
        bindings[name] = ItemVariable(evaluator)
    }

    open fun itemStackVar(name: String, evaluator: IValueEvaluator<*, IContext<ItemStack>>) {
        bindings[name] = ItemStackVariable(evaluator)
    }

    open fun blockStateVar(name: String, evaluator: IValueEvaluator<*, IContext<BlockState>>) {
        bindings[name] = BlockStateVariable(evaluator)
    }

    open fun blockVar(name: String, evaluator: IValueEvaluator<*, IContext<Block>>) {
        bindings[name] = BlockVariable(evaluator)
    }

    open fun blockBehaviourVar(name: String, evaluator: IValueEvaluator<*, IContext<BlockBehaviour>>) {
        bindings[name] = BlockBehaviorVariable(evaluator)
    }
}