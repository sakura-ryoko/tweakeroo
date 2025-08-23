package fi.dy.masa.tweakeroo.mixin.freecam;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

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
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.SHOW_HOTBAR_IN_FREECAM.getBooleanValue())
		{
            ci.cancel();
        }
    }
}
