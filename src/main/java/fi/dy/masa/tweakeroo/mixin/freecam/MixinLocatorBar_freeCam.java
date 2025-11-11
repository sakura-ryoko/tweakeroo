package fi.dy.masa.tweakeroo.mixin.freecam;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(value = LocatorBar.class, priority = 999)
public class MixinLocatorBar_freeCam
{
	@Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_disableLocatorBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "renderAddons", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_disableLocatorBarAddons(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
