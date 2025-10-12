package fi.dy.masa.tweakeroo.mixin.freecam;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = ExperienceBar.class, priority = 999)
public class MixinExperienceBar_freeCam
{
	@Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_disableExperienceBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
