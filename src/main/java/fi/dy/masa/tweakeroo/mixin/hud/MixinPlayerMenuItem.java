package fi.dy.masa.tweakeroo.mixin.hud;

import net.minecraft.client.gui.spectator.PlayerMenuItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(PlayerMenuItem.class)
public class MixinPlayerMenuItem
{
	@Inject(method = "isEnabled()Z", at = @At("HEAD"), cancellable = true)
	public void allowSpectatorTeleport(CallbackInfoReturnable<Boolean> cir)
	{
		if (FeatureToggle.TWEAK_SPECTATOR_TELEPORT.getBooleanValue())
		{
			cir.setReturnValue(true);
		}
	}
}
