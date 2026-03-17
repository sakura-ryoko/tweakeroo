package fi.dy.masa.tweakeroo.mixin.freecam;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = JumpableVehicleBarRenderer.class, priority = 999)
public class MixinJumpableVehicleBarRenderer_freeCam
{
	@Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_disableJumpBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
