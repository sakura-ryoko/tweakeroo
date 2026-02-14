package fi.dy.masa.tweakeroo.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.component.AttackRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.malilib.util.MathUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(AttackRange.class)
public class MixinAttackRange
{
	@WrapOperation(method = "defaultFor",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D")
	)
	private static double tweakeroo_overrideEntityAttackRange(LivingEntity instance, Holder<Attribute> holder, Operation<Double> original)
	{
		final double orig = instance.getAttributeValue(holder);
		double adj = orig;

		if (FeatureToggle.TWEAK_ENTITY_REACH_OVERRIDE.getBooleanValue())
		{
			final RangedAttribute attr = ((RangedAttribute) Attributes.ENTITY_INTERACTION_RANGE.value());
			final double maxOffset = 3.0D;

			if (Minecraft.getInstance().hasSingleplayerServer() || Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue() < orig)
			{
				adj = MathUtils.clamp(Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue(), attr.getMinValue(), attr.getMaxValue() + maxOffset);
			}
			else
			{
				// Calculate a "safe" range for servers
				adj = MathUtils.clamp(Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue(), attr.getMinValue(), orig + maxOffset);
			}
		}

		return adj;
	}
}
