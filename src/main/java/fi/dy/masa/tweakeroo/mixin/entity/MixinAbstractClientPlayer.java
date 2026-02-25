package fi.dy.masa.tweakeroo.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer
{
	@WrapOperation(method = "getFieldOfViewModifier",
	               at = @At(value = "INVOKE",
					   target = "Lnet/minecraft/client/player/AbstractClientPlayer;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
	private double tweakeroo$disableFreezeFovChange(AbstractClientPlayer instance, Holder<Attribute> attribute, Operation<Double> original)
	{
		if (Configs.Disable.DISABLE_FREEZE_OVERLAY.getBooleanValue())
		{
			AttributeInstance attrInstance = instance.getAttribute(attribute);

			if (attrInstance != null)
			{
				Identifier powderSnowId = Identifier.withDefaultNamespace("powder_snow");
				AttributeModifier modifier = attrInstance.getModifier(powderSnowId);

				if (modifier != null)
				{
					attrInstance.removeModifier(powderSnowId);
					double cleanValue = attrInstance.getValue();
					attrInstance.addTransientModifier(modifier);
					return cleanValue;
				}
			}
		}

		return original.call(instance, attribute);
//		return instance.getAttributeValue(attribute);
	}
}
