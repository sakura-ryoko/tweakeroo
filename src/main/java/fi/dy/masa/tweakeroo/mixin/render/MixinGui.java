package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = Gui.class, priority = 1001)
public class MixinGui
{
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void tweakeroo$disableVanillaCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_F3_CURSOR.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
