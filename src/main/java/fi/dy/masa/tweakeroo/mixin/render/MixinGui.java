package fi.dy.masa.tweakeroo.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = Gui.class, priority = 990)
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

	@WrapOperation(method = "renderCameraOverlays",
	               at = @At(value = "INVOKE",
					   target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
	private int tweakeroo$disableFreezeOverlay(LocalPlayer instance, Operation<Integer> original)
	{
		if (Configs.Disable.DISABLE_FREEZE_OVERLAY.getBooleanValue())
		{
			return 0;
		}

		return original.call(instance);
//		return instance.getTicksFrozen();
	}
}
