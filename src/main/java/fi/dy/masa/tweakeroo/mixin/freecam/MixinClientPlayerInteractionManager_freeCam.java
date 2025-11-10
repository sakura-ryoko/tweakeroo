package fi.dy.masa.tweakeroo.mixin.freecam;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

@Mixin(value = MultiPlayerGameMode.class, priority = 1005)
public class MixinClientPlayerInteractionManager_freeCam
{
	@Inject(method = "hasExperience", at = @At("RETURN"), cancellable = true)
	private void tweakeroo_disableExpLevel(CallbackInfoReturnable<Boolean> cir)
	{
		// This disables the "Exp Level" number (We can't Mixin into an Interface class)
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			cir.setReturnValue(false);
		}
	}
}
