package rip.ysm.api.attribute

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import rip.ysm.api.attribute.fabric.ForgeAttributesImpl

object ForgeAttributes {
    fun blockReach(): Attribute? {
        return ForgeAttributesImpl.blockReach()
    }

    fun entityReach(): Attribute? {
        return ForgeAttributesImpl.entityReach()
    }

    fun swimSpeed(): Attribute? {
        return ForgeAttributesImpl.swimSpeed()
    }

    fun entityGravity(): Attribute? {
        return ForgeAttributesImpl.entityGravity()
    }

    fun stepHeightAddition(): Attribute? {
        return ForgeAttributesImpl.stepHeightAddition()
    }

    fun nametagDistance(): Attribute? {
        return ForgeAttributesImpl.nametagDistance()
    }

    fun getValue(entity: LivingEntity, attribute: Attribute?, defaultValue: Double): Double {
        if (attribute == null) {
            return defaultValue
        }
        return entity.getAttributeValue(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute))
    }
}
