package rip.ysm.api.attribute

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import rip.ysm.api.attribute.fabric.ForgeAttributesImpl

object ForgeAttributes {
    @JvmStatic
    fun blockReach(): Attribute? {
        return ForgeAttributesImpl.blockReach()
    }

    @JvmStatic
    fun entityReach(): Attribute? {
        return ForgeAttributesImpl.entityReach()
    }

    @JvmStatic
    fun swimSpeed(): Attribute? {
        return ForgeAttributesImpl.swimSpeed()
    }

    @JvmStatic
    fun entityGravity(): Attribute? {
        return ForgeAttributesImpl.entityGravity()
    }

    @JvmStatic
    fun stepHeightAddition(): Attribute? {
        return ForgeAttributesImpl.stepHeightAddition()
    }

    @JvmStatic
    fun nametagDistance(): Attribute? {
        return ForgeAttributesImpl.nametagDistance()
    }

    @JvmStatic
    fun getValue(entity: LivingEntity, attribute: Attribute?, defaultValue: Double): Double {
        if (attribute == null) {
            return defaultValue
        }
        return entity.getAttributeValue(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute))
    }
}
