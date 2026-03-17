package fi.dy.masa.tweakeroo.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.malilib.util.MathUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.DataManager;

@Mixin(Player.class)
public class MixinPlayer_reachOverride
{
	@WrapOperation(method = "blockInteractionRange",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D")
	)
	private double tweakeroo_overrideBlockReachDistance(Player instance, Holder<Attribute> holder, Operation<Double> original)
	{
		if (FeatureToggle.TWEAK_BLOCK_REACH_OVERRIDE.getBooleanValue())
		{
			final double orig = instance.getAttributeValue(holder);
			double adj = orig;
			final RangedAttribute attr = ((RangedAttribute) Attributes.BLOCK_INTERACTION_RANGE.value());
			final double maxOffset = 1.0D;

			if (Minecraft.getInstance().hasSingleplayerServer() || Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue() < orig)
			{
				adj = MathUtils.clamp(Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue(), attr.getMinValue(), attr.getMaxValue() + maxOffset);
			}
			else
			{
				if (DataManager.getInstance().hasCarpetServer())
				{
					// When using Carpet server, the server-side reach check might be disabled.
					adj = MathUtils.clamp(Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue(), attr.getMinValue(), attr.getMaxValue() + maxOffset);
				}
				else
				{
					// Calculate a "safe" range for servers
					adj = MathUtils.clamp(Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue(), attr.getMinValue(), orig + maxOffset);
				}
			}

			return adj;
		}

		return original.call(instance,  holder);
	}

	@WrapOperation(method = "entityInteractionRange",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D")
	)
	private double tweakeroo_overrideEntityReachDistance(Player instance, Holder<Attribute> holder, Operation<Double> original)
	{
		if (FeatureToggle.TWEAK_ENTITY_REACH_OVERRIDE.getBooleanValue())
		{
			final double orig = instance.getAttributeValue(holder);
			double adj = orig;
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

			return adj;
		}

		return original.call(instance, holder);
	}

	@WrapOperation(method = "isWithinAttackRange",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/world/entity/player/Player;getAttackRangeWith(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/component/AttackRange;"))
	private AttackRange tweakeroo_overrideEntityAttackRangeComponent(Player instance, ItemStack itemStack, Operation<AttackRange> original)
	{
		if (FeatureToggle.TWEAK_ENTITY_REACH_OVERRIDE.getBooleanValue())
		{
			AttackRange comp = instance.getAttackRangeWith(itemStack);
			final double origDefault = comp.maxReach();
			final double origCreative = comp.maxCreativeReach();

			if (comp.maxReach() < Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue())
			{
				final RangedAttribute attr = ((RangedAttribute) Attributes.ENTITY_INTERACTION_RANGE.value());
				final double maxOffset = 3.0D;
				double adjDefault = origDefault;
				double adjCreative = origCreative;

				if (Minecraft.getInstance().hasSingleplayerServer() || Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue() < adjDefault)
				{
					adjDefault = MathUtils.clamp(Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue(), comp.minReach(), attr.getMaxValue());
				}
				else
				{
					// Calculate a "safe" range for servers
					adjDefault = MathUtils.clamp(Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue(), comp.minReach(), origDefault + maxOffset);
				}

				if (adjDefault > adjCreative)
				{
					adjCreative = MathUtils.clamp(adjDefault, comp.minCreativeReach(), origCreative + maxOffset);
				}

				return new AttackRange(comp.minReach(), (float) adjDefault, comp.minCreativeReach(), (float) adjCreative, comp.hitboxMargin(), comp.mobFactor());
			}

			return comp;
		}

		return original.call(instance, itemStack);
	}
}
