package fi.dy.masa.tweakeroo.mixin.freecam;

import fi.dy.masa.tweakeroo.config.Configs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

@Mixin(value = Gui.class, priority = 1005)
public abstract class MixinInGameHud_freeCam
{
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_overridePlayerForRendering(CallbackInfoReturnable<Player> cir)
    {
        // Fix the hotbar rendering in the Free Camera mode by using the actual player
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			this.minecraft.player != null)
        {
            cir.setReturnValue(this.minecraft.player);
        }
    }

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    public void tweakeroo_overrideHotbarRendering(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci)
	{
		// This turns off rendering of the hotbar
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HOTBAR.getBooleanValue())
		{
            ci.cancel();
        }
    }

	@Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideHeldItemTooltipRendering(GuiGraphics context, CallbackInfo ci)
	{
		// This turns off rendering of the item "tooltips" when selecting hotbar items
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HOTBAR.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HANDS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideStatusBarRendering1(GuiGraphics context, CallbackInfo ci)
	{
		// This turns off all status bars
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideStatusBarRendering2(GuiGraphics context, CallbackInfo ci)
	{
		// This turns off the "mount health" status bar
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "nextContextualInfoState", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideExpBarRendering(CallbackInfoReturnable<Gui.ContextualInfo> cir)
	{
		// This turns off all status bars
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			cir.setReturnValue(Gui.ContextualInfo.EMPTY);
		}
	}
}
