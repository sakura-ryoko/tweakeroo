package fi.dy.masa.tweakeroo.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(value = Camera.class)
public class MixinCamera
{
	@Shadow @Final private Minecraft minecraft;

	@WrapOperation(method = "update",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/client/Camera;calculateFov(F)F"))
	private float tweakeroo_calculateFov(Camera instance, float partialTicks, Operation<Float> original)
	{
		if (MiscUtils.isZoomActive())
		{
			return (float) Configs.Generic.ZOOM_FOV.getDoubleValue();
		}
		else if (FeatureToggle.TWEAK_SPYGLASS_USES_TWEAK_ZOOM.getBooleanValue() &&
				 this.minecraft.player != null && this.minecraft.player.isScoping())
		{
			return (float) Configs.Generic.ZOOM_FOV.getDoubleValue();
		}

		return original.call(instance, partialTicks);
	}
}
