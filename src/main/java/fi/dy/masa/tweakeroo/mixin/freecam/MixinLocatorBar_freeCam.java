package fi.dy.masa.tweakeroo.mixin.freecam;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;

@Mixin(value = LocatorBarRenderer.class, priority = 999)
public class MixinLocatorBar_freeCam
{
	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_disableLocatorBar(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_disableLocatorBarAddons(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
