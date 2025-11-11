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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(value = InGameHud.class, priority = 1005)
public abstract class MixinInGameHud_freeCam
{
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_overridePlayerForRendering(CallbackInfoReturnable<PlayerEntity> cir)
    {
        // Fix the hotbar rendering in the Free Camera mode by using the actual player
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			this.client.player != null)
        {
            cir.setReturnValue(this.client.player);
        }
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void tweakeroo_overrideHotbarRendering(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
	{
		// This turns off rendering of the hotbar
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HOTBAR.getBooleanValue())
		{
            ci.cancel();
        }
    }

	@Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideHeldItemTooltipRendering(DrawContext context, CallbackInfo ci)
	{
		// This turns off rendering of the item "tooltips" when selecting hotbar items
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HOTBAR.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HANDS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideStatusBarRendering1(DrawContext context, CallbackInfo ci)
	{
		// This turns off all status bars
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideStatusBarRendering2(DrawContext context, CallbackInfo ci)
	{
		// This turns off the "mount health" status bar
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "getCurrentBarType", at = @At("HEAD"), cancellable = true)
	public void tweakeroo_overrideExpBarRendering(CallbackInfoReturnable<InGameHud.BarType> cir)
	{
		// This turns off all status bars
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_STATUS_BARS.getBooleanValue())
		{
			cir.setReturnValue(InGameHud.BarType.EMPTY);
		}
	}
}
